use serde::Deserialize;
use serde_json::json;
use sha2::{Digest, Sha256};
use wasm_bindgen::JsValue;
use worker::d1::D1Database;
use worker::*;

use crate::error::{ApiError, ApiResult};
use crate::http::json_response;
use crate::models::{RecipeRow, UserResponse, UserRow};
use crate::runtime::{db, images, internal, iso_from_ms, now_iso, now_ms};

pub const SESSION_LIFETIME_MS: i64 = 30 * 24 * 60 * 60 * 1000;

#[derive(Debug, Clone)]
pub struct AuthUser {
    pub user: UserRow,
}

#[derive(Debug, Deserialize)]
struct SessionUserRow {
    id: String,
    email: String,
    name: String,
    picture: Option<String>,
    created_at: String,
    accessed_at: String,
}

impl From<SessionUserRow> for UserRow {
    fn from(row: SessionUserRow) -> Self {
        Self {
            id: row.id,
            email: row.email,
            name: row.name,
            picture: row.picture,
            created_at: row.created_at,
            accessed_at: row.accessed_at,
        }
    }
}

fn bearer_token(req: &Request) -> Option<String> {
    req.headers()
        .get("Authorization")
        .ok()
        .flatten()
        .and_then(|value| {
            value
                .strip_prefix("Bearer ")
                .map(str::trim)
                .map(str::to_string)
        })
        .filter(|value| !value.is_empty())
}

fn hash_token(token: &str) -> String {
    let digest = Sha256::digest(token.as_bytes());
    hex::encode(digest)
}

pub async fn optional_auth(req: &Request, env: &Env) -> ApiResult<Option<AuthUser>> {
    let Some(token) = bearer_token(req) else {
        return Ok(None);
    };
    let token_hash = hash_token(&token);
    let database = db(env)?;
    let now = now_ms();

    let row: Option<SessionUserRow> = database
        .prepare(
            "SELECT u.id AS id, u.email AS email, u.name AS name, u.picture AS picture, \
                    u.created_at AS created_at, u.accessed_at AS accessed_at \
             FROM sessions s JOIN users u ON u.id = s.user_id \
             WHERE s.token_hash = ?1 AND s.expires_at > ?2",
        )
        .bind(&[
            JsValue::from_str(&token_hash),
            JsValue::from_f64(now as f64),
        ])
        .map_err(internal)?
        .first(None)
        .await
        .map_err(internal)?;

    let Some(row) = row else {
        return Ok(None);
    };

    let accessed_at = now_iso();
    database
        .prepare("UPDATE users SET accessed_at = ?1 WHERE id = ?2")
        .bind(&[JsValue::from_str(&accessed_at), JsValue::from_str(&row.id)])
        .map_err(internal)?
        .run()
        .await
        .map_err(internal)?;

    let mut user: UserRow = row.into();
    user.accessed_at = accessed_at;
    Ok(Some(AuthUser { user }))
}

pub async fn require_auth(req: &Request, env: &Env) -> ApiResult<AuthUser> {
    optional_auth(req, env)
        .await?
        .ok_or_else(ApiError::unauthorized)
}

pub async fn auth_me(req: &Request, env: &Env) -> ApiResult<Response> {
    let auth = require_auth(req, env).await?;
    json_response(&UserResponse::from(auth.user), 200)
}

pub async fn auth_logout(req: &Request, env: &Env) -> ApiResult<Response> {
    let token = bearer_token(req).ok_or_else(ApiError::unauthorized)?;
    let database = db(env)?;
    database
        .prepare("DELETE FROM sessions WHERE token_hash = ?1")
        .bind(&[JsValue::from_str(&hash_token(&token))])
        .map_err(internal)?
        .run()
        .await
        .map_err(internal)?;
    json_response(&json!({ "message": "Signed out" }), 200)
}

pub async fn delete_account(req: &Request, env: &Env) -> ApiResult<Response> {
    let auth = require_auth(req, env).await?;
    let database = db(env)?;
    let bucket = images(env)?;

    let rows: Vec<RecipeRow> = database
        .prepare(
            "SELECT id, owner_id, title, description, image_key, image_content_type, \
                    ingredients, directions, preparation_time, is_public, created_at, updated_at \
             FROM recipes WHERE owner_id = ?1",
        )
        .bind(&[JsValue::from_str(&auth.user.id)])
        .map_err(internal)?
        .all()
        .await
        .map_err(internal)?
        .results()
        .map_err(internal)?;

    for recipe in &rows {
        if recipe.image_key.is_some() {
            let _ = bucket.delete(format!("recipes/{}", recipe.id)).await;
        }
    }

    for query in [
        "DELETE FROM sessions WHERE user_id = ?1",
        "DELETE FROM recipes WHERE owner_id = ?1",
        "DELETE FROM users WHERE id = ?1",
    ] {
        database
            .prepare(query)
            .bind(&[JsValue::from_str(&auth.user.id)])
            .map_err(internal)?
            .run()
            .await
            .map_err(internal)?;
    }

    json_response(&json!({ "message": "Account deleted" }), 200)
}

pub async fn create_session(database: &D1Database, user_id: &str) -> ApiResult<String> {
    let token = format!(
        "{}{}",
        uuid::Uuid::new_v4().simple(),
        uuid::Uuid::new_v4().simple()
    );
    let now = now_ms();
    database
        .prepare(
            "INSERT INTO sessions (token_hash, user_id, created_at, expires_at) \
             VALUES (?1, ?2, ?3, ?4)",
        )
        .bind(&[
            JsValue::from_str(&hash_token(&token)),
            JsValue::from_str(user_id),
            JsValue::from_str(&iso_from_ms(now)),
            JsValue::from_f64((now + SESSION_LIFETIME_MS) as f64),
        ])
        .map_err(internal)?
        .run()
        .await
        .map_err(internal)?;
    Ok(token)
}
