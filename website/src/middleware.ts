import { defineMiddleware } from "astro:middleware";
import { isLocale, resolvePreferredLocale } from "./utils/i18n";

const IGNORED_PREFIXES = ["/_astro/", "/.well-known/"];

export const onRequest = defineMiddleware((context, next) => {
  const { pathname, search } = context.url;

  if (IGNORED_PREFIXES.some((prefix) => pathname.startsWith(prefix))) {
    return next();
  }

  const firstSegment = pathname.split("/")[1];
  if (isLocale(firstSegment)) {
    return next();
  }

  const locale = resolvePreferredLocale(
    context.cookies.get("locale")?.value,
    context.request.headers.get("accept-language"),
  );

  const target = `/${locale}${pathname === "/" ? "" : pathname}${search}`;

  return new Response(null, {
    status: 302,
    headers: {
      Location: target,
      Vary: "Accept-Language, Cookie",
    },
  });
});
