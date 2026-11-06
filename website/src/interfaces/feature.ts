import type { ImageMetadata } from "astro";

export default interface Feature {
  icon?: string;
  title: string;
  description: string;
  lightImage?: ImageMetadata | string;
  darkImage?: ImageMetadata | string;
}
