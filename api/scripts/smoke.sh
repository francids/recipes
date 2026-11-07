#!/usr/bin/env bash
# End-to-end smoke test against a running local Worker started with `cf dev`.
#
#   npm run db:migrate:local
#   npm run dev
#   ./scripts/smoke.sh
set -euo pipefail

DB_ID="${D1_DATABASE_ID:-8015c8dc-26d3-4a63-95d9-f35166653df7}"
BASE="${API_BASE_URL:-http://localhost:8787}"
STATE_DIR="${CF_LOCAL_STATE_DIR:-.wrangler/state}"
TOKEN="smoke-session-token"
HASH="$(printf %s "$TOKEN" | sha256sum | cut -d' ' -f1)"
NOW="$(date -u +%Y-%m-%dT%H:%M:%S.000Z)"
EXPIRES="$(( $(date +%s) * 1000 + 86400000 ))"

cf d1 raw "$DB_ID" --local --persist-to "$STATE_DIR" --sql \
  "INSERT OR REPLACE INTO users (id, google_sub, email, name, picture, created_at, accessed_at) VALUES ('smoke-user','google-smoke','smoke@example.com','Smoke User',NULL,'$NOW','$NOW'); INSERT OR REPLACE INTO sessions (token_hash, user_id, created_at, expires_at) VALUES ('$HASH','smoke-user','$NOW',$EXPIRES);" \
  >/dev/null

auth=(-H "Authorization: Bearer $TOKEN")
image="$(printf '\xff\xd8\xff\xe0\x00\x10JFIF\x00\x01' | base64 -w0)"

curl -fsS "$BASE/health" >/dev/null
[ "$(curl -sS -o /dev/null -w '%{http_code}' "$BASE/recipes")" = "401" ]

created="$(curl -fsS -X POST "${auth[@]}" -H 'Content-Type: application/json' \
  -d "{\"title\":\"Smoke Soup\",\"description\":\"Smoke test\",\"ingredients\":[\"water\"],\"directions\":[\"boil\"],\"preparationTime\":60,\"image\":\"$image\"}" \
  "$BASE/recipes")"
id="$(printf %s "$created" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')"

[ "$(curl -sS -o /dev/null -w '%{http_code}' "$BASE/files/$id")" = "401" ]
[ "$(curl -sS -o /dev/null -w '%{http_code}' "${auth[@]}" "$BASE/files/$id")" = "200" ]

curl -fsS -X PATCH "${auth[@]}" -H 'Content-Type: application/json' \
  -d '{"isPublic":true}' "$BASE/recipes/$id" >/dev/null

curl -fsS "$BASE/recipes/public?query=smoke" | grep -q "$id"
curl -fsS "$BASE/recipes/public/$id" | grep -q "Smoke Soup"
[ "$(curl -sS -o /dev/null -w '%{http_code}' "$BASE/files/$id")" = "200" ]

[ "$(curl -sS -o /dev/null -w '%{http_code}' -X DELETE "${auth[@]}" "$BASE/recipes/$id")" = "200" ]
[ "$(curl -sS -o /dev/null -w '%{http_code}' -X DELETE "${auth[@]}" "$BASE/account")" = "200" ]
[ "$(curl -sS -o /dev/null -w '%{http_code}' "${auth[@]}" "$BASE/auth/me")" = "401" ]

echo "smoke test passed"
