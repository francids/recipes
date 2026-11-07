use serde::Deserialize;
use serde_json::{json, Value};
use url::form_urlencoded;
use wasm_bindgen::JsValue;
use worker::*;

use crate::auth::create_session;
use crate::error::{ApiError, ApiResult};
use crate::http::json_response;
use crate::models::{UserResponse, UserRow};
use crate::runtime::{config_value, db, internal, now_iso, now_ms, public_base_url};

const OAUTH_STATE_LIFETIME_MS: i64 = 10 * 60 * 1000;

#[derive(Debug, Deserialize)]
struct GoogleClaims {
    #[serde(default)]
    sub: String,
    #[serde(default)]
    email: String,
    #[serde(default)]
    name: String,
    #[serde(default)]
    picture: Option<String>,
    #[serde(default)]
    aud: String,
    #[serde(default)]
    email_verified: Value,
}

fn email_verified(claims: &GoogleClaims) -> bool {
    match &claims.email_verified {
        Value::Bool(value) => *value,
        Value::String(value) => value == "true",
        _ => false,
    }
}

#[derive(Debug, Deserialize)]
struct GoogleTokenResponse {
    access_token: Option<String>,
    error_description: Option<String>,
}

#[derive(Debug, Deserialize)]
struct IdTokenRequest {
    #[serde(alias = "idToken")]
    id_token: String,
}

pub async fn google_start(req: &Request, env: &Env) -> ApiResult<Response> {
    let client_id = config_value(env, "GOOGLE_CLIENT_ID")
        .ok_or_else(|| ApiError::unavailable("GOOGLE_CLIENT_ID is not configured on the API"))?;
    let url = req.url().map_err(internal)?;
    let return_to = url
        .query_pairs()
        .find(|(key, _)| key == "return_to" || key == "returnTo")
        .map(|(_, value)| value.to_string())
        .unwrap_or_else(|| "recipes://auth".to_string());

    if !is_allowed_return_to(&return_to) {
        return Err(ApiError::bad_request("Invalid return_to URL"));
    }

    let state = format!(
        "{}{}",
        uuid::Uuid::new_v4().simple(),
        uuid::Uuid::new_v4().simple()
    );
    let database = db(env)?;
    database
        .prepare("INSERT INTO oauth_states (state, return_to, expires_at) VALUES (?1, ?2, ?3)")
        .bind(&[
            JsValue::from_str(&state),
            JsValue::from_str(&return_to),
            JsValue::from_f64((now_ms() + OAUTH_STATE_LIFETIME_MS) as f64),
        ])
        .map_err(internal)?
        .run()
        .await
        .map_err(internal)?;

    let callback = format!("{}/auth/google/callback", public_base_url(req, env));
    let query = form_urlencoded::Serializer::new(String::new())
        .append_pair("client_id", &client_id)
        .append_pair("redirect_uri", &callback)
        .append_pair("response_type", "code")
        .append_pair("scope", "openid email profile")
        .append_pair("state", &state)
        .append_pair("access_type", "online")
        .append_pair("prompt", "select_account")
        .finish();
    let authorize_url = format!("https://accounts.google.com/o/oauth2/v2/auth?{query}");
    let target = Url::parse(&authorize_url).map_err(internal)?;
    Response::redirect(target).map_err(internal)
}

fn is_allowed_return_to(value: &str) -> bool {
    value.starts_with("recipes://")
        || value.starts_with("https://recipes.francids.com")
        || value.starts_with("http://localhost")
        || value.starts_with("http://127.0.0.1")
}

pub async fn google_callback(req: &Request, env: &Env) -> ApiResult<Response> {
    let url = req.url().map_err(internal)?;
    let params: std::collections::HashMap<String, String> =
        url.query_pairs().into_owned().collect();

    let state = params
        .get("state")
        .ok_or_else(|| ApiError::bad_request("Missing OAuth state"))?
        .clone();

    #[derive(Debug, Deserialize)]
    struct StateRow {
        return_to: String,
        expires_at: i64,
    }

    let database = db(env)?;
    let state_row: Option<StateRow> = database
        .prepare("SELECT return_to, expires_at FROM oauth_states WHERE state = ?1")
        .bind(&[JsValue::from_str(&state)])
        .map_err(internal)?
        .first(None)
        .await
        .map_err(internal)?;
    let Some(state_row) = state_row else {
        return Err(ApiError::bad_request("OAuth state is invalid or expired"));
    };
    let _ = database
        .prepare("DELETE FROM oauth_states WHERE state = ?1")
        .bind(&[JsValue::from_str(&state)])
        .map_err(internal)?
        .run()
        .await;

    if state_row.expires_at < now_ms() {
        return Err(ApiError::bad_request("OAuth state has expired"));
    }

    if let Some(error) = params.get("error") {
        return redirect_with_error(&state_row.return_to, error);
    }

    let code = params
        .get("code")
        .ok_or_else(|| ApiError::bad_request("Missing OAuth code"))?
        .clone();
    let client_id = config_value(env, "GOOGLE_CLIENT_ID")
        .ok_or_else(|| ApiError::unavailable("GOOGLE_CLIENT_ID is not configured on the API"))?;
    let client_secret = config_value(env, "GOOGLE_CLIENT_SECRET").ok_or_else(|| {
        ApiError::unavailable("GOOGLE_CLIENT_SECRET is not configured on the API")
    })?;
    let callback = format!("{}/auth/google/callback", public_base_url(req, env));

    let form = form_urlencoded::Serializer::new(String::new())
        .append_pair("code", &code)
        .append_pair("client_id", &client_id)
        .append_pair("client_secret", &client_secret)
        .append_pair("redirect_uri", &callback)
        .append_pair("grant_type", "authorization_code")
        .finish();

    let token_response = post_form("https://oauth2.googleapis.com/token", &form).await?;
    let access_token = token_response.access_token.ok_or_else(|| {
        ApiError::new(
            502,
            "upstream_error",
            token_response
                .error_description
                .unwrap_or_else(|| "Google did not return an access token".to_string()),
        )
    })?;

    let user_info = fetch_google_user(&access_token).await?;
    let user = upsert_google_user(&database, user_info).await?;
    let session_token = create_session(&database, &user.id).await?;

    let mut target = Url::parse(&state_row.return_to)
        .map_err(|_| ApiError::bad_request("Invalid return_to URL"))?;
    target
        .query_pairs_mut()
        .append_pair("token", &session_token)
        .append_pair("user_id", &user.id);
    Response::redirect(target).map_err(internal)
}

fn redirect_with_error(return_to: &str, error: &str) -> ApiResult<Response> {
    let mut target =
        Url::parse(return_to).map_err(|_| ApiError::bad_request("Invalid return_to URL"))?;
    target.query_pairs_mut().append_pair("error", error);
    Response::redirect(target).map_err(internal)
}

async fn post_form(url: &str, form: &str) -> ApiResult<GoogleTokenResponse> {
    let headers = Headers::new();
    headers
        .set("Content-Type", "application/x-www-form-urlencoded")
        .map_err(internal)?;
    let mut init = RequestInit::new();
    init.with_method(Method::Post)
        .with_headers(headers)
        .with_body(Some(JsValue::from_str(form)));
    let request = Request::new_with_init(url, &init).map_err(internal)?;
    let mut response = Fetch::Request(request).send().await.map_err(internal)?;
    if response.status_code() >= 400 {
        let body = response.text().await.unwrap_or_default();
        return Err(ApiError::new(
            502,
            "upstream_error",
            format!("Google token exchange failed: {body}"),
        ));
    }
    response.json().await.map_err(internal)
}

async fn fetch_google_user(access_token: &str) -> ApiResult<GoogleClaims> {
    let headers = Headers::new();
    headers
        .set("Authorization", &format!("Bearer {access_token}"))
        .map_err(internal)?;
    let mut init = RequestInit::new();
    init.with_method(Method::Get).with_headers(headers);
    let request = Request::new_with_init("https://www.googleapis.com/oauth2/v3/userinfo", &init)
        .map_err(internal)?;
    let mut response = Fetch::Request(request).send().await.map_err(internal)?;
    if response.status_code() >= 400 {
        let body = response.text().await.unwrap_or_default();
        return Err(ApiError::new(
            502,
            "upstream_error",
            format!("Google userinfo failed: {body}"),
        ));
    }
    response.json().await.map_err(internal)
}

pub async fn google_id_token(req: &mut Request, env: &Env) -> ApiResult<Response> {
    let client_id = config_value(env, "GOOGLE_CLIENT_ID")
        .ok_or_else(|| ApiError::unavailable("GOOGLE_CLIENT_ID is not configured on the API"))?;
    let input: IdTokenRequest = req
        .json()
        .await
        .map_err(|_| ApiError::bad_request("Expected a JSON body with id_token"))?;

    let encoded = form_urlencoded::Serializer::new(String::new())
        .append_pair("id_token", &input.id_token)
        .finish();
    let headers = Headers::new();
    headers
        .set("Content-Type", "application/x-www-form-urlencoded")
        .map_err(internal)?;
    let mut init = RequestInit::new();
    init.with_method(Method::Get).with_headers(headers);
    let request = Request::new_with_init(
        &format!("https://oauth2.googleapis.com/tokeninfo?{encoded}"),
        &init,
    )
    .map_err(internal)?;
    let mut response = Fetch::Request(request).send().await.map_err(internal)?;
    if response.status_code() >= 400 {
        return Err(ApiError::new(
            401,
            "unauthorized",
            "Google ID token is invalid or expired",
        ));
    }
    let claims: GoogleClaims = response.json().await.map_err(internal)?;
    if claims.aud != client_id || claims.sub.is_empty() || claims.email.is_empty() {
        return Err(ApiError::new(
            401,
            "unauthorized",
            "Google token audience mismatch",
        ));
    }
    if !email_verified(&claims) {
        return Err(ApiError::new(
            401,
            "unauthorized",
            "Google email is not verified",
        ));
    }

    let database = db(env)?;
    let user = upsert_google_user(&database, claims).await?;
    let token = create_session(&database, &user.id).await?;
    json_response(
        &json!({
            "token": token,
            "user": UserResponse::from(user),
        }),
        200,
    )
}

async fn upsert_google_user(
    database: &worker::d1::D1Database,
    claims: GoogleClaims,
) -> ApiResult<UserRow> {
    let existing: Option<UserRow> = database
        .prepare(
            "SELECT id, email, name, picture, created_at, accessed_at \
             FROM users WHERE google_sub = ?1",
        )
        .bind(&[JsValue::from_str(&claims.sub)])
        .map_err(internal)?
        .first(None)
        .await
        .map_err(internal)?;

    let now = now_iso();
    let name = if claims.name.trim().is_empty() {
        claims.email.clone()
    } else {
        claims.name
    };

    if let Some(mut user) = existing {
        database
            .prepare(
                "UPDATE users SET email = ?1, name = ?2, picture = ?3, accessed_at = ?4 \
                 WHERE id = ?5",
            )
            .bind(&[
                JsValue::from_str(&claims.email),
                JsValue::from_str(&name),
                match &claims.picture {
                    Some(picture) => JsValue::from_str(picture),
                    None => JsValue::NULL,
                },
                JsValue::from_str(&now),
                JsValue::from_str(&user.id),
            ])
            .map_err(internal)?
            .run()
            .await
            .map_err(internal)?;
        user.email = claims.email;
        user.name = name;
        user.picture = claims.picture;
        user.accessed_at = now;
        return Ok(user);
    }

    let user = UserRow {
        id: uuid::Uuid::new_v4().to_string(),
        email: claims.email,
        name,
        picture: claims.picture,
        created_at: now.clone(),
        accessed_at: now,
    };
    database
        .prepare(
            "INSERT INTO users (id, google_sub, email, name, picture, created_at, accessed_at) \
             VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)",
        )
        .bind(&[
            JsValue::from_str(&user.id),
            JsValue::from_str(&claims.sub),
            JsValue::from_str(&user.email),
            JsValue::from_str(&user.name),
            match &user.picture {
                Some(picture) => JsValue::from_str(picture),
                None => JsValue::NULL,
            },
            JsValue::from_str(&user.created_at),
            JsValue::from_str(&user.accessed_at),
        ])
        .map_err(internal)?
        .run()
        .await
        .map_err(internal)?;
    Ok(user)
}

#[cfg(test)]
mod tests {
    use super::is_allowed_return_to;

    #[test]
    fn allows_app_and_web_return_targets() {
        assert!(is_allowed_return_to("recipes://auth"));
        assert!(is_allowed_return_to("https://recipes.francids.com/sharing"));
    }

    #[test]
    fn rejects_external_return_targets() {
        assert!(!is_allowed_return_to("https://evil.example/auth"));
        assert!(!is_allowed_return_to("javascript:alert(1)"));
    }
}
