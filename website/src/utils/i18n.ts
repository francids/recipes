import { i18n } from "astro:config/client";
import { toCodes } from "astro:i18n";

export type Locale = "de" | "en" | "es" | "fr" | "it" | "ja" | "ko" | "pt" | "zh";

export interface Translations {
  [key: string]: string | Translations;
}

export const defaultLocale = i18n!.defaultLocale as Locale;
export const supportedLocales = toCodes(i18n!.locales) as Locale[];

export function isLocale(value: string | undefined): value is Locale {
  return value !== undefined && supportedLocales.includes(value as Locale);
}

export function resolveLocale(locale: string | undefined): Locale {
  return isLocale(locale) ? locale : defaultLocale;
}

export function resolveLocaleFromAcceptLanguage(
  header: string | null | undefined,
): Locale | undefined {
  if (!header) {
    return undefined;
  }

  const candidates = header
    .split(",")
    .map((part) => {
      const [tag, ...params] = part.trim().split(";");
      const qualityParam = params.find((param) => param.trim().startsWith("q="));
      const quality = qualityParam ? Number.parseFloat(qualityParam.split("=")[1]) : 1;

      return {
        tag: tag.trim().toLowerCase(),
        quality: Number.isFinite(quality) ? quality : 1,
      };
    })
    .filter(({ tag, quality }) => tag && tag !== "*" && quality > 0)
    .sort((a, b) => b.quality - a.quality);

  for (const { tag } of candidates) {
    if (isLocale(tag)) {
      return tag;
    }

    const base = tag.split("-")[0];
    if (isLocale(base)) {
      return base;
    }
  }

  return undefined;
}

export function resolvePreferredLocale(
  cookieLocale: string | undefined,
  acceptLanguage: string | null | undefined,
): Locale {
  return (
    (isLocale(cookieLocale) ? cookieLocale : undefined) ??
    resolveLocaleFromAcceptLanguage(acceptLanguage) ??
    defaultLocale
  );
}

const translationsCache = new Map<Locale, Translations>();

async function loadTranslations(locale: Locale): Promise<Translations> {
  if (translationsCache.has(locale)) {
    return translationsCache.get(locale)!;
  }

  try {
    const translations = await import(`../../messages/${locale}.json`);
    translationsCache.set(locale, translations.default);
    return translations.default;
  } catch {
    console.warn(`Could not load locale ${locale}, falling back to ${defaultLocale}`);
    if (locale !== defaultLocale) {
      return await loadTranslations(defaultLocale);
    }
    return {};
  }
}

function getNestedTranslation(obj: Translations, path: string): string | undefined {
  const result = path
    .split(".")
    .reduce((current: Translations | string | undefined, key: string) => {
      if (current && typeof current === "object" && key in current) {
        return current[key];
      }
      return undefined;
    }, obj);

  return typeof result === "string" ? result : undefined;
}

export async function t(key: string, locale: Locale = defaultLocale): Promise<string> {
  const translations = await loadTranslations(locale);
  return getNestedTranslation(translations, key) || key;
}

export async function createTranslationHelper(
  locale: Locale,
): Promise<(key: string, fallback?: string, params?: Record<string, string>) => string> {
  const translations = await loadTranslations(locale);

  return function translate(
    key: string,
    fallback: string = key,
    params?: Record<string, string>,
  ): string {
    let result = getNestedTranslation(translations, key) || fallback;

    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        result = result.replace(new RegExp(`{{${key}}}`, "g"), value);
      });
    }

    return result;
  };
}
