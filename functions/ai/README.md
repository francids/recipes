# Recipes AI (Function)

Esta función proporciona capacidades de inteligencia artificial a la aplicación Recipes, permitiendo generar recetas estructuradas a partir de imágenes de comida.

## Funcionalidades

- **Generación de Recetas (`/generate-recipe`)**:
  - Acepta una imagen en formato base64 y un código de idioma.
  - Utiliza **Gemini 2.5 Flash Lite** para analizar la imagen.
  - Devuelve un objeto JSON con información de la receta.

## Tecnologías utilizadas

- [uv](https://docs.astral.sh/uv/) para la gestión de dependencias.
- [Google GenAI SDK](https://pypi.org/project/google-genai/)
- [Pydantic](https://docs.pydantic.dev/) para la validación y estructuración de datos.

## Desarrollo local

Para desarrollar y probar esta función localmente:

- Asegúrate de tener instalado y ejecutándose [Docker](https://docker.com/).
- Instala la [Appwrite CLI](https://appwrite.io/docs/tooling/command-line/installation).
- Inicia sesión en la CLI: `appwrite login`.

Desde la raíz del proyecto, ejecuta: `appwrite run function --function-id the-recipes-ai --port 3000`

Asegúrate de configurar la variable de entorno `GEMINI_API_KEY`.
