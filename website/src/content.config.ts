import { defineCollection, z } from "astro:content";
import { glob } from "astro/loaders";

const privacyCollection = defineCollection({
  loader: glob({ pattern: "**/*.md", base: "./src/content/privacy" }),
  schema: z.object({
    title: z.string(),
    lastUpdated: z.date(),
  }),
});

export const collections = {
  privacy: privacyCollection,
};
