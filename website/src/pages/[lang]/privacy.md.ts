import type { APIRoute } from "astro";
import { resolveLocale } from "@/utils/i18n";
import { getPrivacyEntry } from "@/utils/privacy";

export const GET: APIRoute = async ({ params }) => {
  const locale = resolveLocale(params.lang);
  const entry = await getPrivacyEntry(locale);

  if (!entry?.body) {
    return new Response("Not found", { status: 404 });
  }

  return new Response(entry.body, {
    status: 200,
    headers: {
      "Content-Type": "text/markdown; charset=utf-8",
      "Content-Language": locale,
    },
  });
};
