import { bindings, defineConfig } from "cf/config";

/**
 * Secrets (set with `npx wrangler secret put <NAME> --name recipes-api`):
 * - GOOGLE_CLIENT_ID
 * - GOOGLE_CLIENT_SECRET
 * - GEMINI_API_KEY
 */
export default defineConfig({
  worker: {
    name: "recipes-api",
    compatibilityDate: "2026-11-06",
    entrypoint: "build/worker/shim.mjs",
    observability: {
      enabled: true,
      traces: {
        enabled: true,
      },
    },
    domains: ["api.recipes.francids.com"],
    env: {
      PUBLIC_BASE_URL: bindings.text("https://api.recipes.francids.com"),
      DB: bindings.d1({
        name: "recipes-api",
        id: "8015c8dc-26d3-4a63-95d9-f35166653df7",
      }),
      IMAGES: bindings.r2({
        name: "recipes-images",
      }),
    },
  },
});
