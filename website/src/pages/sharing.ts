import type { APIRoute } from "astro";
import { getRelativeLocaleUrl } from "astro:i18n";
import { defaultLocale } from "../utils/i18n";

export const GET: APIRoute = ({ url }) => {
  const location = `${getRelativeLocaleUrl(defaultLocale, "sharing")}${url.search}`;

  return new Response(null, {
    status: 301,
    headers: { Location: location },
  });
};
