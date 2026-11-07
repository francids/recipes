use serde::Serialize;
use serde_json::json;
use worker::*;

use crate::error::{ApiError, ApiResult};

pub fn add_cors(mut response: Response) -> Response {
    let headers = response.headers_mut();
    let _ = headers.set("Access-Control-Allow-Origin", "*");
    let _ = headers.set(
        "Access-Control-Allow-Headers",
        "Authorization, Content-Type",
    );
    let _ = headers.set(
        "Access-Control-Allow-Methods",
        "GET, POST, PATCH, DELETE, OPTIONS",
    );
    let _ = headers.set("Access-Control-Max-Age", "86400");
    let _ = headers.set("X-Content-Type-Options", "nosniff");
    response
}

pub fn json_response<T: Serialize>(value: &T, status: u16) -> ApiResult<Response> {
    Response::from_json(value)
        .map(|response| response.with_status(status))
        .map_err(|error| ApiError::internal(error.to_string()))
}

pub fn error_response(error: &ApiError) -> Response {
    let body = json!({
        "error": error.message,
        "code": error.code,
    });
    Response::from_json(&body)
        .unwrap_or_else(|_| Response::error("Internal error", 500).unwrap())
        .with_status(error.status)
}
