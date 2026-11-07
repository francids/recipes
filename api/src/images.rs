use base64::Engine;

use crate::error::{ApiError, ApiResult};

pub const MAX_IMAGE_BYTES: usize = 5 * 1024 * 1024;

#[derive(Debug, Clone)]
pub struct DecodedImage {
    pub bytes: Vec<u8>,
    pub content_type: String,
}

pub fn decode_image(image: &str) -> ApiResult<DecodedImage> {
    let trimmed = image.trim();
    let (payload, declared_type) = if let Some((header, data)) = trimmed.split_once(',') {
        if !header.starts_with("data:") || !header.contains(";base64") {
            return Err(ApiError::bad_request("Invalid image data URL"));
        }
        let content_type = header
            .trim_start_matches("data:")
            .split(';')
            .next()
            .unwrap_or("application/octet-stream")
            .to_string();
        (data, Some(content_type))
    } else {
        (trimmed, None)
    };

    if payload.is_empty() {
        return Err(ApiError::bad_request("Image is empty"));
    }

    let bytes = base64::engine::general_purpose::STANDARD
        .decode(payload)
        .map_err(|_| ApiError::bad_request("Image is not valid base64"))?;

    if bytes.len() > MAX_IMAGE_BYTES {
        return Err(ApiError::bad_request("Image too large (>5 MB)"));
    }

    let detected = detect_image_mime(&bytes)
        .ok_or_else(|| ApiError::bad_request("Unsupported image format. Use JPEG, PNG or WebP"))?;

    if let Some(declared) = declared_type {
        if declared != detected && declared != "application/octet-stream" {
            return Err(ApiError::bad_request(
                "Image type does not match its contents",
            ));
        }
    }

    Ok(DecodedImage {
        bytes,
        content_type: detected.to_string(),
    })
}

pub fn detect_image_mime(bytes: &[u8]) -> Option<&'static str> {
    if bytes.starts_with(&[0xFF, 0xD8, 0xFF]) {
        Some("image/jpeg")
    } else if bytes.starts_with(&[0x89, b'P', b'N', b'G', 0x0D, 0x0A, 0x1A, 0x0A]) {
        Some("image/png")
    } else if bytes.len() >= 12 && &bytes[0..4] == b"RIFF" && &bytes[8..12] == b"WEBP" {
        Some("image/webp")
    } else {
        None
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn base64(bytes: &[u8]) -> String {
        base64::engine::general_purpose::STANDARD.encode(bytes)
    }

    #[test]
    fn decodes_plain_and_data_url_images() {
        let jpeg = [0xFF, 0xD8, 0xFF, 0xE0, 0x00];
        let plain = decode_image(&base64(&jpeg)).unwrap();
        assert_eq!(plain.content_type, "image/jpeg");

        let data_url = format!("data:image/jpeg;base64,{}", base64(&jpeg));
        let with_header = decode_image(&data_url).unwrap();
        assert_eq!(with_header.bytes, jpeg);
    }

    #[test]
    fn rejects_unknown_image_formats() {
        assert!(decode_image(&base64(b"not an image")).is_err());
    }

    #[test]
    fn detects_image_headers() {
        assert_eq!(detect_image_mime(&[0xFF, 0xD8, 0xFF]), Some("image/jpeg"));
        assert_eq!(
            detect_image_mime(&[0x89, b'P', b'N', b'G', 0x0D, 0x0A, 0x1A, 0x0A]),
            Some("image/png")
        );
        assert_eq!(
            detect_image_mime(b"RIFF\x00\x00\x00\x00WEBP"),
            Some("image/webp")
        );
        assert_eq!(detect_image_mime(b"GIF89a"), None);
    }
}
