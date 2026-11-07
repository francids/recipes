mod ai;
mod auth;
mod error;
mod http;
mod images;
mod models;
mod oauth;
mod recipes;
mod runtime;
mod validation;

use serde_json::json;
use worker::*;

use crate::error::ApiResult;
use crate::http::{add_cors, error_response, json_response};
use crate::runtime::{SERVICE_NAME, SERVICE_VERSION};

#[event(fetch)]
async fn fetch(req: Request, env: Env, _ctx: Context) -> worker::Result<Response> {
    if req.method() == Method::Options {
        return Ok(add_cors(Response::empty()?.with_status(204)));
    }

    let response = match route(req, env).await {
        Ok(response) => response,
        Err(error) => error_response(&error),
    };

    Ok(add_cors(response))
}

async fn route(mut req: Request, env: Env) -> ApiResult<Response> {
    let method = req.method();
    let path = req.path();
    let segments: Vec<&str> = path
        .trim_matches('/')
        .split('/')
        .filter(|segment| !segment.is_empty())
        .collect();

    match (method, segments.as_slice()) {
        (Method::Get, ["health"]) => health(),
        (Method::Get, ["auth", "google"]) => oauth::google_start(&req, &env).await,
        (Method::Post, ["auth", "google"]) => oauth::google_id_token(&mut req, &env).await,
        (Method::Get, ["auth", "google", "callback"]) => oauth::google_callback(&req, &env).await,
        (Method::Get, ["auth", "me"]) => auth::auth_me(&req, &env).await,
        (Method::Post, ["auth", "logout"]) => auth::auth_logout(&req, &env).await,
        (Method::Delete, ["account"]) => auth::delete_account(&req, &env).await,
        (Method::Get, ["recipes"]) => recipes::list_recipes(&req, &env).await,
        (Method::Post, ["recipes"]) => recipes::create_recipe(&mut req, &env).await,
        (Method::Get, ["recipes", "public"]) => recipes::search_public_recipes(&req, &env).await,
        (Method::Get, ["recipes", "public", id]) => {
            recipes::get_public_recipe(&req, &env, id).await
        }
        (Method::Get, ["recipes", id]) => recipes::get_recipe(&req, &env, id).await,
        (Method::Patch, ["recipes", id]) => recipes::update_recipe(&mut req, &env, id).await,
        (Method::Delete, ["recipes", id]) => recipes::delete_recipe(&req, &env, id).await,
        (Method::Get, ["files", id]) => recipes::get_file(&req, &env, id).await,
        (Method::Post, ["ai", "recipes"]) => ai::generate_recipe(&mut req, &env).await,
        _ => Err(error::ApiError::not_found("Route not found")),
    }
}

fn health() -> ApiResult<Response> {
    json_response(
        &json!({
            "status": "ok",
            "service": SERVICE_NAME,
            "version": SERVICE_VERSION,
        }),
        200,
    )
}
