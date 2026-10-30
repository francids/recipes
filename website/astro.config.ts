import { defineConfig } from "astro/config";
import tailwindcss from "@tailwindcss/vite";
import cloudflare from "@astrojs/cloudflare";

export default defineConfig({
  output: "server",
  session: false,
  i18n: {
    defaultLocale: "en",
    locales: ["de", "en", "es", "fr", "it", "ja", "ko", "pt", "zh"],
    routing: {
      prefixDefaultLocale: true,
      redirectToDefaultLocale: false,
    },
  },
  vite: {
    plugins: [
      // @ts-ignore
      tailwindcss(),
    ],
  },
  adapter: cloudflare({
    imageService: "passthrough",
  }),
});
