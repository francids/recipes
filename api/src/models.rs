use serde::{Deserialize, Serialize};

use crate::error::ApiResult;
use crate::validation::validate_recipe;

#[derive(Debug, Clone, Deserialize)]
pub struct UserRow {
    pub id: String,
    pub email: String,
    pub name: String,
    pub picture: Option<String>,
    pub created_at: String,
    pub accessed_at: String,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct UserResponse {
    pub id: String,
    pub name: String,
    pub email: String,
    pub picture: Option<String>,
    pub email_verification: bool,
    pub registration: String,
    pub accessed_at: String,
}

impl From<UserRow> for UserResponse {
    fn from(user: UserRow) -> Self {
        Self {
            id: user.id,
            name: user.name,
            email: user.email,
            picture: user.picture,
            email_verification: true,
            registration: user.created_at,
            accessed_at: user.accessed_at,
        }
    }
}

#[derive(Debug, Clone, Deserialize)]
pub struct RecipeRow {
    pub id: String,
    pub owner_id: String,
    pub title: String,
    pub description: String,
    pub image_key: Option<String>,
    pub image_content_type: Option<String>,
    pub ingredients: String,
    pub directions: String,
    pub preparation_time: i64,
    pub is_public: i64,
    pub created_at: String,
    pub updated_at: String,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct RecipeResponse {
    pub id: String,
    pub title: String,
    pub description: String,
    pub image: Option<String>,
    pub ingredients: Vec<String>,
    pub directions: Vec<String>,
    pub preparation_time: i64,
    pub owner_id: String,
    pub is_public: bool,
    pub cloud_id: String,
    pub created_at: String,
    pub updated_at: String,
}

impl RecipeRow {
    pub fn ingredients(&self) -> Vec<String> {
        serde_json::from_str(&self.ingredients).unwrap_or_default()
    }

    pub fn directions(&self) -> Vec<String> {
        serde_json::from_str(&self.directions).unwrap_or_default()
    }

    pub fn to_response(&self, base_url: &str, include_private_image: bool) -> RecipeResponse {
        let image = if include_private_image || self.is_public != 0 {
            self.image_key
                .as_ref()
                .map(|_| format!("{base_url}/files/{}", self.id))
        } else {
            None
        };

        RecipeResponse {
            id: self.id.clone(),
            title: self.title.clone(),
            description: self.description.clone(),
            image,
            ingredients: self.ingredients(),
            directions: self.directions(),
            preparation_time: self.preparation_time,
            owner_id: self.owner_id.clone(),
            is_public: self.is_public != 0,
            cloud_id: self.id.clone(),
            created_at: self.created_at.clone(),
            updated_at: self.updated_at.clone(),
        }
    }
}

#[derive(Debug, Default, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RecipeInput {
    #[serde(default)]
    pub title: String,
    #[serde(default)]
    pub description: String,
    /// Base64 image data or a `data:image/...;base64,` URL.
    #[serde(default)]
    pub image: Option<String>,
    #[serde(default)]
    pub ingredients: Vec<String>,
    #[serde(default)]
    pub directions: Vec<String>,
    #[serde(default)]
    pub preparation_time: i64,
    #[serde(default)]
    pub is_public: Option<bool>,
}

impl RecipeInput {
    pub fn validate(&self) -> ApiResult<()> {
        validate_recipe(
            &self.title,
            &self.description,
            &self.ingredients,
            &self.directions,
            self.preparation_time,
        )
    }
}

#[derive(Debug, Default, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RecipePatch {
    pub title: Option<String>,
    pub description: Option<String>,
    /// Base64 image data. An empty string removes the stored image.
    pub image: Option<String>,
    pub ingredients: Option<Vec<String>>,
    pub directions: Option<Vec<String>>,
    pub preparation_time: Option<i64>,
    pub is_public: Option<bool>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct AiRecipeRequest {
    pub image: String,
    #[serde(default)]
    pub language: String,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct AiRecipe {
    #[serde(default)]
    pub title: String,
    #[serde(default)]
    pub description: String,
    #[serde(default)]
    pub ingredients: Vec<String>,
    #[serde(default)]
    pub directions: Vec<String>,
    #[serde(default)]
    pub preparation_time: i64,
}
