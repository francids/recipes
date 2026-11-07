use base64::Engine;
use serde_json::{json, Value};
use wasm_bindgen::JsValue;
use worker::*;

use crate::auth::require_auth;
use crate::error::{ApiError, ApiResult};
use crate::http::json_response;
use crate::images::decode_image;
use crate::models::{AiRecipe, AiRecipeRequest};
use crate::runtime::{config_value, internal};
use crate::validation::normalize_language;

fn ai_prompt(language: &str) -> &'static str {
    match language {
        "de" => "Generiere das Rezept dieses Bildes auf Deutsch. Der Titel des Rezepts sollte ein kurzer Name des Gerichts sein, gefolgt von einer einfachen Textbeschreibung. Liste dann die Zutaten, die Zubereitungsanweisungen und die geschätzte Zubereitungszeit in Sekunden auf.",
        "es" => "Genera la receta de esta imagen en español. El título de la receta debe ser un nombre corto del plato, seguido de una descripción de texto plano. Luego, enumera los ingredientes, las instrucciones para prepararlo y el tiempo estimado de preparación en segundos.",
        "fr" => "Génère la recette de cette image en français. Le titre de la recette doit être un nom court du plat, suivi d'une description en texte brut. Ensuite, liste les ingrédients, les instructions de préparation et le temps de préparation estimé en secondes.",
        "it" => "Genera la ricetta da questa immagine in italiano. Il titolo della ricetta dovrebbe essere un nome breve del piatto, seguito da una descrizione in testo semplice. Quindi, elenca gli ingredienti, le istruzioni per la preparazione e il tempo di preparazione stimato in secondi.",
        "ja" => "この画像からレシピを日本語で生成してください。レシピのタイトルは料理の短い名前で、その後にプレーンテキストの説明を続けてください。次に、材料、準備手順、および推定準備時間を秒単位でリストアップしてください。",
        "ko" => "이 이미지에서 레시피를 한국어로 생성해 주세요. 레시피 제목은 요리의 짧은 이름이어야 하며, 이어서 일반 텍스트 설명이 와야 합니다. 그 다음 재료, 준비 지침, 그리고 예상 준비 시간을 초 단위로 나열해 주세요.",
        "pt" => "Gere a receita desta imagem em português. O título da receita deve ser um nome curto do prato, seguido de uma descrição em texto simples. Em seguida, liste os ingredientes, as instruções de preparo e o tempo estimado de preparação em segundos.",
        "zh" => "请根据此图片生成中文食谱。食谱标题应为菜肴的简称，后跟纯文本描述。然后，列出食材、烹饪说明和预计准备时间（以秒为单位）。",
        _ => "Generate the recipe from this image in English. The recipe title should be a short name of the dish, followed by a plain text description. Then, list the ingredients, preparation instructions, and estimated preparation time in seconds. Return only a JSON object with the fields title, description, ingredients, directions and preparationTime.",
    }
}

pub async fn generate_recipe(req: &mut Request, env: &Env) -> ApiResult<Response> {
    let _auth = require_auth(req, env).await?;
    let api_key = config_value(env, "GEMINI_API_KEY")
        .ok_or_else(|| ApiError::unavailable("GEMINI_API_KEY is not configured on the API"))?;
    let input: AiRecipeRequest = req
        .json()
        .await
        .map_err(|_| ApiError::bad_request("Invalid JSON format"))?;
    let language = normalize_language(&input.language)?;
    let decoded = decode_image(&input.image)?;
    let encoded = base64::engine::general_purpose::STANDARD.encode(&decoded.bytes);

    let prompt = format!(
        "{}\nReturn only a JSON object with exactly these fields: \
         title (string), description (string), ingredients (array of strings), \
         directions (array of strings), preparationTime (integer seconds).",
        ai_prompt(&language)
    );

    let body = json!({
        "contents": [{
            "parts": [
                { "inline_data": { "mime_type": decoded.content_type, "data": encoded } },
                { "text": prompt }
            ]
        }],
        "generationConfig": {
            "responseMimeType": "application/json",
            "temperature": 0.4
        }
    });

    let headers = Headers::new();
    headers
        .set("Content-Type", "application/json")
        .map_err(internal)?;
    let mut init = RequestInit::new();
    init.with_method(Method::Post)
        .with_headers(headers)
        .with_body(Some(JsValue::from_str(&body.to_string())));
    let url = format!(
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key={api_key}"
    );
    let request = Request::new_with_init(&url, &init).map_err(internal)?;
    let mut response = Fetch::Request(request).send().await.map_err(internal)?;
    let status = response.status_code();
    let response_body = response.text().await.unwrap_or_default();

    if status == 429 {
        return Err(ApiError::new(
            429,
            "rate_limit_exceeded",
            "AI rate limit exceeded. Try again later.",
        ));
    }
    if status >= 400 {
        return Err(ApiError::new(
            502,
            "upstream_error",
            format!("Gemini request failed with status {status}"),
        ));
    }

    let payload: Value = serde_json::from_str(&response_body).map_err(|error| {
        ApiError::new(
            502,
            "upstream_error",
            format!("Gemini returned invalid JSON: {error}"),
        )
    })?;
    let text = payload
        .pointer("/candidates/0/content/parts/0/text")
        .and_then(Value::as_str)
        .ok_or_else(|| ApiError::new(502, "upstream_error", "Gemini returned no recipe content"))?;
    let json_text = extract_json_object(text).ok_or_else(|| {
        ApiError::new(
            502,
            "upstream_error",
            "Gemini response did not contain a JSON object",
        )
    })?;
    let mut recipe: AiRecipe = serde_json::from_str(json_text).map_err(|error| {
        ApiError::new(
            502,
            "upstream_error",
            format!("Gemini returned an invalid recipe: {error}"),
        )
    })?;
    if recipe.preparation_time < 0 {
        recipe.preparation_time = 0;
    }
    if recipe.ingredients.is_empty() || recipe.directions.is_empty() {
        return Err(ApiError::new(
            502,
            "upstream_error",
            "Gemini returned an incomplete recipe",
        ));
    }

    json_response(&recipe, 200)
}

/// Extracts a JSON object from an LLM response that may be wrapped in markdown.
fn extract_json_object(text: &str) -> Option<&str> {
    let start = text.find('{')?;
    let end = text.rfind('}')?;
    if end > start {
        Some(&text[start..=end])
    } else {
        None
    }
}

#[cfg(test)]
mod tests {
    use super::extract_json_object;

    #[test]
    fn extracts_json_from_markdown() {
        assert_eq!(
            extract_json_object("Here:\n```json\n{\"title\":\"Soup\"}\n```"),
            Some("{\"title\":\"Soup\"}")
        );
        assert_eq!(extract_json_object("no json"), None);
    }
}
