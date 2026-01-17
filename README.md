<img src="./assets/Logo.svg" width="100">

# Recipes

**Recipes** es una aplicación móvil enfocada en recetas. Recipes le permite al usuario revisar sus recetas, agregar nuevas y eliminar algunas.

## Funcionalidades de la aplicación

- [x] **Gestión de recetas**: El usuario puede agregar, visualizar y eliminar recetas.
- [x] **Funcionalidad local**: La aplicación permite al usuario gestionar sus recetas sin necesidad de conexión a Internet.
- [x] **Modo oscuro**: La aplicación cuenta con un modo oscuro para mejorar la experiencia del usuario en condiciones de poca luz.
- [x] **Soporte multi-idioma**: La aplicación está disponible en varios idiomas (Alemán, Chino, Coreano, Español, Francés, Inglés, Italiano, Japonés, Portugués).
- [x] **Compartición de recetas**: El usuario puede compartir sus recetas con otros usuarios a través de un enlace único.
- [ ] **IA**: La aplicación utiliza inteligencia artificial para:
  - [x] Generar recetas basadas en una foto de un plato.
  - [ ] Sugerir recetas basadas en los ingredientes que el usuario tiene en casa.
  - [ ] Sugerir recetas basadas en las preferencias del usuario.

## Estructura del proyecto

El repositorio está organizado en tres carpetas principales:

- **`lib/`**: Contiene el código fuente de la aplicación móvil, incluyendo controladores, modelos, vistas y configuraciones de la app.
- **`website/`**: Sitio web que presenta la aplicación y maneja la funcionalidad de compartir recetas a través de enlaces únicos.
- **`functions/`**: Funciones serverless de Appwrite que proporcionan servicios de backend para la gestión de cuentas y funcionalidades de IA.

## Tecnologías utilizadas

- La aplicación móvil está desarrollada en [Flutter](https://flutter.dev/) con [Hive CE](https://pub.dev/packages/hive_ce) y [Riverpod](https://pub.dev/packages/flutter_riverpod).
- El sitio web está desarrollado en [Astro](https://astro.build/) con [Tailwind CSS](https://tailwindcss.com/).
- Las funciones serverless están desarrolladas en [Python](https://www.python.org/) y se ejecutan en [Appwrite Functions](https://appwrite.io/products/functions). Las funciones de IA utilizan el [SDK de Google Gen AI](https://pypi.org/project/google-genai/) y [Gemini](https://ai.google.dev/).
- La aplicación móvil usa [Appwrite](https://appwrite.io/) para la autenticación de usuarios, almacenamiento de recetas y demás.

## Desarrollo

> [!NOTE]
> Para iniciar con el desarrollo del sitio web o las funciones, revisar el archivo `README.md` correspondiente en cada carpeta.

Para iniciar con el desarrollo de la aplicación móvil, se debe tener [Flutter](https://flutter.dev/) instalado.

1. Clona el repositorio.
2. Instala las dependencias: `flutter pub get`
3. Genera los archivos necesarios con build_runner: `dart run build_runner build --delete-conflicting-outputs`
4. Inicia el emulador o conecta un dispositivo físico.
5. Corre la aplicación: `flutter run`

Para correr la aplicación en Android necesitarás firmar la aplicación. Puedes seguir las instrucciones oficiales de Flutter para [firmar la aplicación](https://docs.flutter.dev/deployment/android#sign-the-app).

Asegúrate de agregar el archivo `key.properties` en la carpeta `android/` con las propiedades `storeFile`, `storePassword`, `keyAlias` y `keyPassword` correspondientes a tu archivo de firma.
