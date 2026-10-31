import { getEntry } from "astro:content";
import { defaultLocale, type Locale } from "./i18n";

export async function getPrivacyEntry(locale: Locale) {
  return (await getEntry("privacy", locale)) ?? (await getEntry("privacy", defaultLocale));
}
