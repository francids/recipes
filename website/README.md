# Recipes Site

Este sitio web sirve como punto de entrada para la aplicación Recipes, presentando sus características y gestionando la redirección de enlaces para compartir recetas.

## Funcionalidades

- **Landing Page**: Página principal que presenta la aplicación, sus características y enlaces para acceder a descargar la aplicación.
- **Redirección de recetas compartidas (`/[locale]/sharing`)**: Gestiona los enlaces de recetas compartidas, intentando abrir la aplicación móvil directamente mediante Deep Links. La antigua ruta `/sharing` redirige a `/en/sharing` para mantener compatibilidad con enlaces ya compartidos.
- **Política de Privacidad (`/[locale]/privacy`)**: Página dedicada a mostrar la política de privacidad y los términos de uso de la aplicación. La versión en Markdown está disponible en `/[locale]/privacy.md`.
- **Idiomas**: El sitio usa el enrutado i18n nativo de Astro. Todas las páginas están siempre bajo un prefijo de idioma (`/en`, `/es`, `/de`, etc.). La raíz `/` redirige al idioma por defecto.

## Tecnologías utilizadas

- [Astro](https://astro.build/) como framework web.
- [Tailwind CSS](https://tailwindcss.com/) para el diseño y estilos.
- [Cloudflare Workers](https://workers.cloudflare.com/) como plataforma de despliegue.

## Desarrollo

Para desarrollar o modificar el sitio web:

1. Navega a la carpeta: `cd website`
2. Instala las dependencias: `npm install`
3. Inicia el servidor de desarrollo: `npm run dev`
4. Visita [localhost:4321](http://localhost:4321) para ver el sitio web en funcionamiento.

## Despliegue en Cloudflare Workers

El sitio usa el adaptador `@astrojs/cloudflare`. Los idiomas se gestionan con la configuración `i18n` nativa de Astro definida en `astro.config.ts`, por lo que cada página tiene siempre un prefijo de idioma.

1. Inicia sesión en Cloudflare (solo la primera vez): `npx wrangler login`
2. Compila y despliega: `npm run deploy`

También puedes previsualizar el Worker localmente con el runtime de Cloudflare:

```sh
npm run build
npm run preview
```

La configuración del Worker está en `wrangler.jsonc`. Para publicar en el dominio de producción, añade una sección `routes` con el dominio. Si el proyecto usa otro nombre de Worker, cambia el campo `name`.

> El adaptador configura automáticamente el binding de assets (`ASSETS`). El sitio no usa sesiones ni optimización de imágenes en tiempo de ejecución, por lo que no provisiona KV ni el binding de Cloudflare Images.
