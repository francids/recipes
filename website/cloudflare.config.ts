import { defineConfig } from "cf/config";

export default defineConfig({
  worker: {
    name: "recipes",
    compatibilityDate: "2026-10-23",
    observability: {
      enabled: true,
    },
    domains: ["recipes.francids.com"],
  },
});
