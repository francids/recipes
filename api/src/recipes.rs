use serde_json::json;
use wasm_bindgen::JsValue;
use worker::d1::D1Database;
use worker::*;

use crate::auth::{optional_auth, require_auth};
use crate::error::{ApiError, ApiResult};
use crate::http::json_response;
use crate::images::decode_image;
use crate::models::{RecipeInput, RecipePatch, RecipeResponse, RecipeRow};
use crate::runtime::{db, images, internal, now_iso, public_base_url};
use crate::validation::validate_recipe;

const RECIPE_COLUMNS: &str = "id, owner_id, title, description, image_key, image_content_type, \
     ingredients, directions, preparation_time, is_public, created_at, updated_at";

async fn get_recipe_row(database: &D1Database, id: &str) -> ApiResult<Option<RecipeRow>> {
    database
        .prepare(format!(
            "SELECT {RECIPE_COLUMNS} FROM recipes WHERE id = ?1"
        ))
        .bind(&[JsValue::from_str(id)])
        .map_err(internal)?
        .first(None)
        .await
        .map_err(internal)
}

pub async fn list_recipes(req: &Request, env: &Env) -> ApiResult<Response> {
    let auth = require_auth(req, env).await?;
    let database = db(env)?;
    let base_url = public_base_url(req, env);
    let rows: Vec<RecipeRow> = database
        .prepare(format!(
            "SELECT {RECIPE_COLUMNS} FROM recipes WHERE owner_id = ?1 ORDER BY updated_at DESC"
        ))
        .bind(&[JsValue::from_str(&auth.user.id)])
        .map_err(internal)?
        .all()
        .await
        .map_err(internal)?
        .results()
        .map_err(internal)?;

    let recipes: Vec<RecipeResponse> = rows
        .iter()
        .map(|row| row.to_response(&base_url, true))
        .collect();
    json_response(&json!({ "recipes": recipes, "total": recipes.len() }), 200)
}

pub async fn create_recipe(req: &mut Request, env: &Env) -> ApiResult<Response> {
    let auth = require_auth(req, env).await?;
    let input: RecipeInput = req
        .json()
        .await
        .map_err(|_| ApiError::bad_request("Invalid recipe JSON"))?;
    input.validate()?;

    let database = db(env)?;
    let bucket = images(env)?;
    let id = uuid::Uuid::new_v4().to_string();
    let now = now_iso();
    let is_public = input.is_public.unwrap_or(false);

    let mut image_content_type: Option<String> = None;
    if let Some(image) = input
        .image
        .as_deref()
        .filter(|value| !value.trim().is_empty())
    {
        let decoded = decode_image(image)?;
        bucket
            .put(format!("recipes/{id}"), decoded.bytes)
            .http_metadata(HttpMetadata {
                content_type: Some(decoded.content_type.clone()),
                cache_control: Some("public, max-age=31536000, immutable".to_string()),
                ..Default::default()
            })
            .execute()
            .await
            .map_err(internal)?;
        image_content_type = Some(decoded.content_type);
    }

    let ingredients = serde_json::to_string(&input.ingredients).map_err(internal)?;
    let directions = serde_json::to_string(&input.directions).map_err(internal)?;
    let insert = database
        .prepare(
            "INSERT INTO recipes (id, owner_id, title, description, image_key, image_content_type, \
             ingredients, directions, preparation_time, is_public, created_at, updated_at) \
             VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12)",
        )
        .bind(&[
            JsValue::from_str(&id),
            JsValue::from_str(&auth.user.id),
            JsValue::from_str(input.title.trim()),
            JsValue::from_str(input.description.trim()),
            if image_content_type.is_some() {
                JsValue::from_str(&format!("recipes/{id}"))
            } else {
                JsValue::NULL
            },
            match &image_content_type {
                Some(value) => JsValue::from_str(value),
                None => JsValue::NULL,
            },
            JsValue::from_str(&ingredients),
            JsValue::from_str(&directions),
            JsValue::from_f64(input.preparation_time as f64),
            JsValue::from_f64(if is_public { 1.0 } else { 0.0 }),
            JsValue::from_str(&now),
            JsValue::from_str(&now),
        ])
        .map_err(internal)?
        .run()
        .await;

    if let Err(error) = insert {
        if image_content_type.is_some() {
            let _ = bucket.delete(format!("recipes/{id}")).await;
        }
        return Err(internal(error));
    }

    let row = get_recipe_row(&database, &id)
        .await?
        .ok_or_else(|| ApiError::internal("Recipe was not persisted"))?;
    let base_url = public_base_url(req, env);
    json_response(&row.to_response(&base_url, true), 201)
}

pub async fn get_recipe(req: &Request, env: &Env, id: &str) -> ApiResult<Response> {
    let auth = require_auth(req, env).await?;
    let database = db(env)?;
    let row = get_recipe_row(&database, id)
        .await?
        .ok_or_else(|| ApiError::not_found("Recipe not found"))?;
    if row.owner_id != auth.user.id {
        return Err(ApiError::forbidden("You do not own this recipe"));
    }
    let base_url = public_base_url(req, env);
    json_response(&row.to_response(&base_url, true), 200)
}

pub async fn update_recipe(req: &mut Request, env: &Env, id: &str) -> ApiResult<Response> {
    let auth = require_auth(req, env).await?;
    let database = db(env)?;
    let existing = get_recipe_row(&database, id)
        .await?
        .ok_or_else(|| ApiError::not_found("Recipe not found"))?;
    if existing.owner_id != auth.user.id {
        return Err(ApiError::forbidden("You do not own this recipe"));
    }

    let patch: RecipePatch = req
        .json()
        .await
        .map_err(|_| ApiError::bad_request("Invalid recipe JSON"))?;

    let title = patch.title.unwrap_or(existing.title.clone());
    let description = patch.description.unwrap_or(existing.description.clone());
    let ingredients = patch.ingredients.unwrap_or_else(|| existing.ingredients());
    let directions = patch.directions.unwrap_or_else(|| existing.directions());
    let preparation_time = patch.preparation_time.unwrap_or(existing.preparation_time);
    let is_public = patch.is_public.unwrap_or(existing.is_public != 0);
    validate_recipe(
        &title,
        &description,
        &ingredients,
        &directions,
        preparation_time,
    )?;

    let bucket = images(env)?;
    let mut image_key = existing.image_key.clone();
    let mut image_content_type = existing.image_content_type.clone();

    if let Some(image) = patch.image {
        let key = format!("recipes/{id}");
        if image.trim().is_empty() {
            let _ = bucket.delete(key).await;
            image_key = None;
            image_content_type = None;
        } else {
            let decoded = decode_image(&image)?;
            bucket
                .put(key, decoded.bytes)
                .http_metadata(HttpMetadata {
                    content_type: Some(decoded.content_type.clone()),
                    cache_control: Some("public, max-age=31536000, immutable".to_string()),
                    ..Default::default()
                })
                .execute()
                .await
                .map_err(internal)?;
            image_key = Some(format!("recipes/{id}"));
            image_content_type = Some(decoded.content_type);
        }
    }

    let now = now_iso();
    let ingredients = serde_json::to_string(&ingredients).map_err(internal)?;
    let directions = serde_json::to_string(&directions).map_err(internal)?;
    database
        .prepare(
            "UPDATE recipes SET title = ?1, description = ?2, image_key = ?3, \
             image_content_type = ?4, ingredients = ?5, directions = ?6, \
             preparation_time = ?7, is_public = ?8, updated_at = ?9 WHERE id = ?10",
        )
        .bind(&[
            JsValue::from_str(title.trim()),
            JsValue::from_str(description.trim()),
            match &image_key {
                Some(value) => JsValue::from_str(value),
                None => JsValue::NULL,
            },
            match &image_content_type {
                Some(value) => JsValue::from_str(value),
                None => JsValue::NULL,
            },
            JsValue::from_str(&ingredients),
            JsValue::from_str(&directions),
            JsValue::from_f64(preparation_time as f64),
            JsValue::from_f64(if is_public { 1.0 } else { 0.0 }),
            JsValue::from_str(&now),
            JsValue::from_str(id),
        ])
        .map_err(internal)?
        .run()
        .await
        .map_err(internal)?;

    let row = get_recipe_row(&database, id)
        .await?
        .ok_or_else(|| ApiError::not_found("Recipe not found"))?;
    let base_url = public_base_url(req, env);
    json_response(&row.to_response(&base_url, true), 200)
}

pub async fn delete_recipe(req: &Request, env: &Env, id: &str) -> ApiResult<Response> {
    let auth = require_auth(req, env).await?;
    let database = db(env)?;
    let row = get_recipe_row(&database, id)
        .await?
        .ok_or_else(|| ApiError::not_found("Recipe not found"))?;
    if row.owner_id != auth.user.id {
        return Err(ApiError::forbidden("You do not own this recipe"));
    }

    if row.image_key.is_some() {
        let _ = images(env)?.delete(format!("recipes/{id}")).await;
    }
    database
        .prepare("DELETE FROM recipes WHERE id = ?1")
        .bind(&[JsValue::from_str(id)])
        .map_err(internal)?
        .run()
        .await
        .map_err(internal)?;

    json_response(&json!({ "message": "Recipe deleted" }), 200)
}

pub async fn search_public_recipes(req: &Request, env: &Env) -> ApiResult<Response> {
    let url = req.url().map_err(internal)?;
    let query = url
        .query_pairs()
        .find(|(key, _)| key == "q" || key == "query")
        .map(|(_, value)| value.trim().to_string())
        .unwrap_or_default();
    let auth = optional_auth(req, env).await?;
    let database = db(env)?;
    let base_url = public_base_url(req, env);

    let rows: Vec<RecipeRow> = if query.is_empty() {
        database
            .prepare(format!(
                "SELECT {RECIPE_COLUMNS} FROM recipes WHERE is_public = 1 \
                 ORDER BY created_at DESC LIMIT 20"
            ))
            .all()
            .await
            .map_err(internal)?
            .results()
            .map_err(internal)?
    } else {
        database
            .prepare(format!(
                "SELECT {RECIPE_COLUMNS} FROM recipes \
                 WHERE is_public = 1 AND instr(lower(title), lower(?1)) > 0 \
                 ORDER BY created_at DESC LIMIT 20"
            ))
            .bind(&[JsValue::from_str(&query)])
            .map_err(internal)?
            .all()
            .await
            .map_err(internal)?
            .results()
            .map_err(internal)?
    };

    let recipes: Vec<RecipeResponse> = rows
        .into_iter()
        .filter(|row| {
            auth.as_ref()
                .map(|auth| row.owner_id != auth.user.id)
                .unwrap_or(true)
        })
        .map(|row| row.to_response(&base_url, true))
        .collect();

    json_response(&json!({ "recipes": recipes, "total": recipes.len() }), 200)
}

pub async fn get_public_recipe(req: &Request, env: &Env, id: &str) -> ApiResult<Response> {
    let database = db(env)?;
    let row = get_recipe_row(&database, id)
        .await?
        .filter(|row| row.is_public != 0)
        .ok_or_else(|| ApiError::not_found("Shared recipe not found"))?;
    let base_url = public_base_url(req, env);
    json_response(&row.to_response(&base_url, true), 200)
}

pub async fn get_file(req: &Request, env: &Env, id: &str) -> ApiResult<Response> {
    let database = db(env)?;
    let row = get_recipe_row(&database, id)
        .await?
        .ok_or_else(|| ApiError::not_found("Image not found"))?;
    let key = row
        .image_key
        .clone()
        .ok_or_else(|| ApiError::not_found("Image not found"))?;

    if row.is_public == 0 {
        let auth = optional_auth(req, env)
            .await?
            .ok_or_else(ApiError::unauthorized)?;
        if row.owner_id != auth.user.id {
            return Err(ApiError::forbidden("Image is private"));
        }
    }

    let bucket = images(env)?;
    let object = bucket
        .get(key)
        .execute()
        .await
        .map_err(internal)?
        .ok_or_else(|| ApiError::not_found("Image not found"))?;
    let body = object
        .body()
        .ok_or_else(|| ApiError::not_found("Image body unavailable"))?
        .response_body()
        .map_err(internal)?;

    let headers = Headers::new();
    headers
        .set(
            "Content-Type",
            row.image_content_type
                .as_deref()
                .unwrap_or("application/octet-stream"),
        )
        .map_err(internal)?;
    headers
        .set(
            "Cache-Control",
            if row.is_public != 0 {
                "public, max-age=31536000, immutable"
            } else {
                "private, no-store"
            },
        )
        .map_err(internal)?;

    Ok(Response::from_body(body)
        .map_err(internal)?
        .with_headers(headers))
}
