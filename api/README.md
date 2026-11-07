# Recipes API

API independiente para **Recipes**, escrita en Rust y desplegada en
[Cloudflare Workers](https://workers.cloudflare.com/) con [`workers-rs`](https://github.com/cloudflare/workers-rs).

Proporciona:

- Inicio de sesión con Google OAuth.
- Sesiones de usuario con tokens opacos (solo se guarda su hash SHA-256).
- CRUD de recetas por usuario.
- Búsqueda y compartición de recetas públicas.
- Imágenes de recetas almacenadas en R2.
- Generación de recetas con IA (Gemini).
- Eliminación completa de la cuenta y sus datos.

## Arquitectura

| Recurso  | Cloudflare           | Uso                                        |
| -------- | -------------------- | ------------------------------------------ |
| Compute  | Worker `recipes-api` | Rutas HTTP en Rust/Wasm                    |
| Datos    | D1 `recipes-api`     | Usuarios, sesiones, OAuth states y recetas |
| Imágenes | R2 `recipes-images`  | Objetos `recipes/<recipe-id>`              |
| Secretos | Worker Secrets       | Google OAuth y Gemini                      |

### Estructura del código

| Archivo             | Responsabilidad                              |
| ------------------- | -------------------------------------------- |
| `src/lib.rs`        | Entrada del Worker, router y health check    |
| `src/auth.rs`       | Sesiones, logout y eliminación de cuenta     |
| `src/oauth.rs`      | Flujo OAuth de Google y alta de usuarios     |
| `src/recipes.rs`    | CRUD de recetas, búsqueda pública e imágenes |
| `src/ai.rs`         | Generación de recetas con Gemini             |
| `src/models.rs`     | Filas y DTOs de la API                       |
| `src/validation.rs` | Validación de recetas e idiomas              |
| `src/images.rs`     | Decodificación y detección de imágenes       |
| `src/http.rs`       | Respuestas JSON y CORS                       |
| `src/runtime.rs`    | Bindings, tiempo y URLs públicas             |
| `src/error.rs`      | Errores HTTP tipados                         |

La API pública está desplegada en:

```text
https://api.recipes.francids.com
```

La configuración del Worker vive en `cloudflare.config.ts` y se gestiona con la
CLI `cf` (`npm run dev`, `npm run build`, `npm run deploy`). `wrangler.config.ts`
solo conserva el comando de compilación de Rust para el bundler de Wrangler que
usa `cf` internamente; no hay `wrangler.jsonc`.

La app Flutter usa esa URL por defecto. Para apuntar a otra instancia:

```sh
flutter run --dart-define=API_BASE_URL=http://localhost:8787
```

## Endpoints

| Método   | Ruta                     | Auth     | Descripción                                           |
| -------- | ------------------------ | -------- | ----------------------------------------------------- |
| `GET`    | `/health`                | No       | Estado del servicio                                   |
| `GET`    | `/auth/google`           | No       | Inicia el flujo OAuth de Google                       |
| `GET`    | `/auth/google/callback`  | No       | Callback OAuth; redirige a `recipes://auth?token=...` |
| `POST`   | `/auth/google`           | No       | Login con `id_token` de Google                        |
| `GET`    | `/auth/me`               | Bearer   | Usuario actual                                        |
| `POST`   | `/auth/logout`           | Bearer   | Cierra la sesión actual                               |
| `DELETE` | `/account`               | Bearer   | Elimina cuenta, sesiones, recetas e imágenes          |
| `GET`    | `/recipes`               | Bearer   | Recetas del usuario                                   |
| `POST`   | `/recipes`               | Bearer   | Crea una receta                                       |
| `GET`    | `/recipes/:id`           | Bearer   | Obtiene una receta propia                             |
| `PATCH`  | `/recipes/:id`           | Bearer   | Actualiza campos o visibilidad                        |
| `DELETE` | `/recipes/:id`           | Bearer   | Elimina una receta y su imagen                        |
| `GET`    | `/recipes/public?query=` | No       | Busca recetas públicas                                |
| `GET`    | `/recipes/public/:id`    | No       | Obtiene una receta compartida                         |
| `GET`    | `/files/:id`             | Opcional | Imagen pública; privada solo para su dueño            |
| `POST`   | `/ai/recipes`            | Bearer   | Genera una receta desde una imagen                    |

Las imágenes se envían en el JSON de receta como base64 o como data URL
(`data:image/jpeg;base64,...`). El límite es de 5 MB por imagen.

### Ejemplo de receta

```json
{
  "title": "Sopa de tomate",
  "description": "Una sopa sencilla y caliente",
  "ingredients": ["tomates", "cebolla", "sal"],
  "directions": ["Sofríe la cebolla", "Añade el tomate", "Cocina 20 minutos"],
  "preparationTime": 1200,
  "image": "data:image/jpeg;base64,...",
  "isPublic": false
}
```

## Desarrollo local

Requisitos:

- Rust con el target `wasm32-unknown-unknown`.
- `worker-build` 0.8.x: `cargo install worker-build --version 0.8.7 --locked`
- Node.js 22.18+ para la CLI `cf`.

```sh
cd api
npm install
npm run db:migrate:local
npm run dev
```

Copia `.dev.vars.example` a `.dev.vars` para probar OAuth e IA en local. El
`.dev.vars` no se versiona.

## Secretos

En producción:

```sh
npx wrangler secret put GOOGLE_CLIENT_ID --name recipes-api
npx wrangler secret put GOOGLE_CLIENT_SECRET --name recipes-api
npx wrangler secret put GEMINI_API_KEY --name recipes-api
```

En Google Cloud Console, el URI de redirección autorizado debe ser exactamente:

```text
https://api.recipes.francids.com/auth/google/callback
```

Sin `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET`, los endpoints de login responden
`503`. Sin `GEMINI_API_KEY`, `/ai/recipes` responde `503`.

## Base de datos

Las migraciones están en `migrations/`.

```sh
# Local
npm run db:migrate:local

# Remoto
npm run db:migrate
```

Tablas:

- `users`: cuenta vinculada a Google (`google_sub` único).
- `sessions`: hash del token, usuario y expiración.
- `oauth_states`: estados OAuth de corta duración.
- `recipes`: recetas con propietario, visibilidad y referencias de imagen.

## Pruebas y despliegue

```sh
cd api
cargo test          # pruebas unitarias de validación, imágenes y JSON
npm run build       # compilación Wasm mediante cf + Wrangler
npm run deploy      # despliegue con cf
```

Prueba manual rápida:

```sh
curl https://api.recipes.francids.com/health
curl -i https://api.recipes.francids.com/recipes
# 401 sin bearer token
```
