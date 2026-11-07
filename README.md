<img src="./assets/icon.svg" width="100">

# Recipes

**Recipes** es una aplicación móvil enfocada en recetas. Recipes le permite al usuario revisar sus recetas, agregar nuevas y eliminar algunas.

## Funcionalidades de la aplicación

- [x] **Gestión de recetas**: El usuario puede agregar, visualizar y eliminar recetas.
- [x] **Funcionalidad local**: La aplicación permite al usuario gestionar sus recetas sin necesidad de conexión a Internet.
- [x] **Modo oscuro**: La aplicación cuenta con un modo oscuro para mejorar la experiencia del usuario en condiciones de poca luz.
- [x] **Soporte multi-idioma**: La aplicación está disponible en varios idiomas (Alemán, Chino, Coreano, Español, Francés, Inglés, Italiano, Japonés, Portugués).
- [x] **Compartición de recetas**: El usuario puede compartir sus recetas con otros usuarios a través de un enlace único.
- [x] **IA**: Generar recetas basadas en una foto de un plato.
- [x] **Búsqueda de recetas públicas**: El usuario puede buscar recetas públicas en la plataforma.

## Estructura del proyecto

El repositorio está organizado en tres carpetas principales:

- **`lib/`**: Contiene el código fuente de la aplicación móvil, incluyendo controladores, modelos, vistas y configuraciones de la app.
- **`api/`**: API independiente escrita en Rust y desplegada en [Cloudflare Workers](https://workers.cloudflare.com/). Gestiona autenticación, recetas, imágenes y generación de recetas con IA.
- **`website/`**: Sitio web que presenta la aplicación y maneja la funcionalidad de compartir recetas a través de enlaces únicos.

## Tecnologías utilizadas

- La aplicación móvil está desarrollada en [Flutter](https://flutter.dev/) con [Riverpod](https://pub.dev/packages/flutter_riverpod), [Hive CE](https://pub.dev/packages/hive_ce) y los paquetes de diseño [material_ui](https://pub.dev/packages/material_ui) y [cupertino_ui](https://pub.dev/packages/cupertino_ui).
- El sitio web está desarrollado en [Astro](https://astro.build/) con [Tailwind CSS](https://tailwindcss.com/).
- La API está desarrollada en [Rust](https://www.rust-lang.org/) con [`workers-rs`](https://github.com/cloudflare/workers-rs). Usa [D1](https://developers.cloudflare.com/d1/) para los datos, [R2](https://developers.cloudflare.com/r2/) para las imágenes, Google OAuth para el inicio de sesión y Gemini para generar recetas a partir de fotos.

## Desarrollo

> [!NOTE]
> Para iniciar con el desarrollo de la API o del sitio web, revisar el archivo `README.md` correspondiente en cada carpeta.

Para iniciar con el desarrollo de la aplicación móvil, se debe tener [Flutter](https://flutter.dev/) 3.47 o superior instalado.

1. Clona el repositorio.
2. Instala las dependencias: `flutter pub get`
3. Genera los archivos necesarios con build_runner: `dart run build_runner build`
4. Inicia el emulador o conecta un dispositivo físico.
5. Corre la aplicación: `flutter run`

La aplicación apunta por defecto a la API desplegada. Para usar otra instancia:

```sh
flutter run --dart-define=API_BASE_URL=http://localhost:8787
```

Para correr la aplicación en Android necesitarás firmar la aplicación. Puedes seguir las instrucciones oficiales de Flutter para [firmar la aplicación](https://docs.flutter.dev/deployment/android#sign-the-app).

Asegúrate de agregar el archivo `key.properties` en la carpeta `android/` con las propiedades `storeFile`, `storePassword`, `keyAlias` y `keyPassword` correspondientes a tu archivo de firma.
