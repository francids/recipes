# Recipes Accounts (Function)

Esta función se encarga de la gestión de usuarios para la aplicación Recipes, específicamente permitiendo la eliminación de cuentas.

## Funcionalidades

- **Eliminación de cuenta (`/delete`)**: Permite a un usuario autenticado eliminar su propia cuenta y datos asociados.

## Tecnologías utilizadas

- [uv](https://docs.astral.sh/uv/) para la gestión de dependencias.
- [Appwrite SDK](https://github.com/appwrite/sdk-for-python)

## Desarrollo local

Para desarrollar y probar esta función localmente:

- Asegúrate de tener instalado y ejecutándose [Docker](https://docker.com/).
- Instala la [Appwrite CLI](https://appwrite.io/docs/tooling/command-line/installation).
- Inicia sesión en la CLI: `appwrite login`.

Desde la raíz del proyecto, ejecuta: `appwrite run function --function-id the-recipes-accounts --port 3000`

Esto iniciará la función en un entorno local.
