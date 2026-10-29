import { defineConfig } from "astro/config";
import tailwindcss from "@tailwindcss/vite";
import cloudflare from "@astrojs/cloudflare";

export default defineConfig({
  output: "server",
  session: false,
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
