# Recipes Site

Este sitio web sirve como punto de entrada para la aplicación Recipes, presentando sus características y gestionando la redirección de enlaces para compartir recetas.

## Funcionalidades

- **Landing Page**: Página principal que presenta la aplicación, sus características y enlaces para acceder a descargar la aplicación.
- **Redirección de recetas compartidas (`/sharing`)**: Gestiona los enlaces de recetas compartidas, intentando abrir la aplicación móvil directamente mediante Deep Links.
- **Política de Privacidad (`/privacy`)**: Página dedicada a mostrar la política de privacidad y los términos de uso de la aplicación.

## Tecnologías utilizadas

- [Astro](https://astro.build/) como framework web.
- [Tailwind CSS](https://tailwindcss.com/) para el diseño y estilos.
- [Node.js](https://nodejs.org/) como entorno de ejecución.

## Desarrollo

Para desarrollar o modificar el sitio web:

1. Navega a la carpeta: `cd website`
2. Instala las dependencias: `npm install`
3. Inicia el servidor de desarrollo: `npm run dev`
4. Visita [localhost:4321](http://localhost:4321) para ver el sitio web en funcionamiento.
