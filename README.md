<img src="./assets/icon.svg" width="100">

# Recipes

**Recipes** es una aplicación móvil enfocada en recetas. Permite revisar recetas, agregar nuevas, guardar favoritas y compartirlas.

## Funcionalidades de la aplicación

- [x] **Gestión de recetas**: El usuario puede agregar, visualizar y eliminar recetas.
- [x] **Funcionalidad local**: La aplicación permite gestionar recetas sin conexión a Internet.
- [x] **Modo oscuro**: Tema claro, oscuro o siguiendo el sistema.
- [x] **Soporte multi-idioma**: Alemán, Chino, Coreano, Español, Francés, Inglés, Italiano, Japonés y Portugués.
- [x] **Compartición de recetas**: Enlaces públicos, código QR y hoja de compartir del sistema.
- [x] **IA**: Generar recetas a partir de una foto de un plato.
- [x] **Búsqueda de recetas públicas**: Buscar recetas compartidas por la comunidad.
- [x] **Copia de seguridad**: Exportar e importar recetas (JSON + imágenes en ZIP).

## Tecnologías utilizadas

La aplicación móvil está desarrollada de forma nativa para Android con:

- **Kotlin**
- **Jetpack Compose** y **Material 3 Expressive**
- **Navigation Compose**
- **Coil** para la carga de imágenes
- **OkHttp** para la API
- **ZXing** para los códigos QR
- Persistencia local en JSON y almacenamiento privado de la aplicación

No se requiere Flutter ni un runtime multiplataforma para compilar o ejecutar la app.

El sistema de diseño, la paleta, la tipografía, los componentes expresivos elegidos y las
comprobaciones de contraste y accesibilidad se documentan en [`android/DESIGN.md`](android/DESIGN.md).

La API está desarrollada en [Rust](https://www.rust-lang.org/) con [`workers-rs`](https://github.com/cloudflare/workers-rs) y desplegada en [Cloudflare Workers](https://workers.cloudflare.com/). Usa [D1](https://developers.cloudflare.com/d1/) para los datos, [R2](https://developers.cloudflare.com/r2/) para las imágenes, Google OAuth para el inicio de sesión y Gemini para generar recetas a partir de fotos.

El sitio web está desarrollado en [Astro](https://astro.build/) con [Tailwind CSS](https://tailwindcss.com/).

## Estructura del proyecto

- **`android/`**: aplicación Android nativa en Kotlin y Jetpack Compose.
- **`api/`**: API independiente escrita en Rust y desplegada en Cloudflare Workers.
- **`website/`**: sitio web que presenta la aplicación y gestiona los enlaces de recetas compartidas.
- **`assets/`**: recursos de diseño compartidos (logo, tipografías, animaciones y traducciones).

Dentro de `android/app/src/main/kotlin/com/francids/recipes/`:

- **`data/`**: modelos, API, almacenamiento local, traducciones e importación/exportación.
- **`ui/`**: `AppViewModel`, navegación, tema y pantallas Compose.
- **`ui/components/`**: tarjetas de receta, campos de formulario y vista común de receta.
- **`ui/screens/`**: inicio, perfil, detalle, alta de receta, ajustes, búsqueda y recetas compartidas.

## Desarrollo

### Requisitos

- Android Studio (o el SDK de Android por línea de comandos).
- JDK 17 o superior.
- Android SDK 36.

El proyecto incluye el Gradle Wrapper, por lo que no hace falta instalar Gradle por separado.

### Compilar la aplicación

```sh
cd android
./gradlew assembleDebug
```

El APK se genera en:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

Para instalarlo en un emulador o dispositivo conectado:

```sh
./gradlew installDebug
```

### Apuntar a otra instancia de la API

La URL por defecto es `https://api.recipes.francids.com`. Se puede cambiar en tiempo de compilación:

```sh
./gradlew assembleDebug -PAPI_BASE_URL=http://10.0.2.2:8787
```

### Firma de la aplicación

Para generar una compilación de release, crea `android/key.properties` con las propiedades `storeFile`, `storePassword`, `keyAlias` y `keyPassword`. Si el archivo no existe, la compilación de debug sigue funcionando sin firma de release.

### API y sitio web

Para iniciar con el desarrollo de la API o del sitio web, revisa el archivo `README.md` correspondiente en cada carpeta. La API se puede ejecutar en local con `npm run dev` dentro de `api/`.

## Enlaces profundos (deep links)

La app responde a:

- `recipes://auth?token=...` para completar el inicio de sesión con Google.
- `recipes://sharing?id=...` y `https://recipes.francids.com/sharing?id=...` para abrir una receta compartida.

## Migración desde Flutter

La versión anterior estaba implementada con Flutter, Riverpod, Hive y paquetes Material/Cupertino. La migración conserva:

- Los mismos endpoints de la API y el mismo formato de datos.
- Las traducciones existentes en `assets/messages/`.
- La tipografía Montserrat y los recursos visuales.
- La compatibilidad con los enlaces de compartición y el callback de OAuth.
- El formato de exportación/importación de recetas.

El código Flutter anterior permanece disponible en el historial de Git del repositorio.
