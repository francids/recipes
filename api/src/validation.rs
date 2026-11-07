use crate::error::{ApiError, ApiResult};

pub const VALID_LANGUAGES: &[&str] = &["de", "en", "es", "fr", "it", "ja", "ko", "pt", "zh"];

pub fn validate_recipe(
    title: &str,
    description: &str,
    ingredients: &[String],
    directions: &[String],
    preparation_time: i64,
) -> ApiResult<()> {
    let title = title.trim();
    if title.is_empty() {
        return Err(ApiError::bad_request("Title is required"));
    }
    if title.chars().count() > 100 {
        return Err(ApiError::bad_request(
            "Title must be at most 100 characters",
        ));
    }
    if description.chars().count() > 250 {
        return Err(ApiError::bad_request(
            "Description must be at most 250 characters",
        ));
    }
    if ingredients.is_empty() || ingredients.iter().all(|item| item.trim().is_empty()) {
        return Err(ApiError::bad_request("At least one ingredient is required"));
    }
    if directions.is_empty() || directions.iter().all(|item| item.trim().is_empty()) {
        return Err(ApiError::bad_request("At least one direction is required"));
    }
    if preparation_time < 0 {
        return Err(ApiError::bad_request("Preparation time cannot be negative"));
    }
    Ok(())
}

pub fn normalize_language(language: &str) -> ApiResult<String> {
    let language = language
        .split(['_', '-'])
        .next()
        .unwrap_or_default()
        .to_ascii_lowercase();
    if VALID_LANGUAGES.contains(&language.as_str()) {
        Ok(language)
    } else {
        Err(ApiError::bad_request(format!(
            "Unsupported language. Expected one of: {}",
            VALID_LANGUAGES.join(", ")
        )))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn accepts_supported_languages_and_normalizes_locales() {
        assert_eq!(normalize_language("es_MX").unwrap(), "es");
        assert_eq!(normalize_language("PT-br").unwrap(), "pt");
        assert!(normalize_language("xx").is_err());
    }

    #[test]
    fn validates_recipe_fields() {
        assert!(validate_recipe("Soup", "Warm", &["water".into()], &["boil".into()], 10).is_ok());
        assert!(validate_recipe("", "", &["water".into()], &["boil".into()], 10).is_err());
        assert!(validate_recipe("Soup", "Warm", &[], &["boil".into()], 10).is_err());
        assert!(validate_recipe("Soup", "Warm", &["water".into()], &[], 10).is_err());
        assert!(validate_recipe("Soup", "Warm", &["water".into()], &["boil".into()], -1).is_err());
    }
}
