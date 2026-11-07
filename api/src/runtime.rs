use js_sys::Date as JsDate;
use wasm_bindgen::JsValue;
use worker::d1::D1Database;
use worker::*;

use crate::error::{ApiError, ApiResult};

pub const SERVICE_NAME: &str = "recipes-api";
pub const SERVICE_VERSION: &str = env!("CARGO_PKG_VERSION");

pub fn now_ms() -> i64 {
    JsDate::now() as i64
}

pub fn iso_from_ms(milliseconds: i64) -> String {
    JsDate::new(&JsValue::from_f64(milliseconds as f64))
        .to_iso_string()
        .as_string()
        .unwrap_or_default()
}

pub fn now_iso() -> String {
    iso_from_ms(now_ms())
}

pub fn public_base_url(req: &Request, env: &Env) -> String {
    if let Ok(value) = env.var("PUBLIC_BASE_URL") {
        let value = value.to_string();
        if !value.trim().is_empty() {
            return value.trim_end_matches('/').to_string();
        }
    }

    if let Ok(url) = req.url() {
        let mut origin = format!("{}://{}", url.scheme(), url.host_str().unwrap_or_default());
        if let Some(port) = url.port() {
            origin.push_str(&format!(":{port}"));
        }
        return origin;
    }

    String::new()
}

pub fn config_value(env: &Env, name: &str) -> Option<String> {
    env.var(name)
        .ok()
        .map(|value| value.to_string())
        .filter(|value| !value.trim().is_empty())
}

pub fn db(env: &Env) -> ApiResult<D1Database> {
    env.d1("DB")
        .map_err(|error| ApiError::internal(format!("D1 binding unavailable: {error}")))
}

pub fn images(env: &Env) -> ApiResult<Bucket> {
    env.bucket("IMAGES")
        .map_err(|error| ApiError::internal(format!("R2 binding unavailable: {error}")))
}

pub fn internal(error: impl std::fmt::Display) -> ApiError {
    ApiError::internal(error.to_string())
}
