#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
USERNAME="${BANK_USERNAME:-alice}"
PASSWORD="${BANK_PASSWORD:-Bank@12345}"

echo "[1/4] health"
curl -fsS "$BASE_URL/actuator/health" | grep -q '"status":"UP"'

echo "[2/4] login"
LOGIN_JSON=$(curl -fsS -X POST "$BASE_URL/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")
TOKEN=$(printf '%s' "$LOGIN_JSON" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
[ -n "$TOKEN" ]

echo "[3/4] accounts"
curl -fsS "$BASE_URL/api/v1/accounts" -H "Authorization: Bearer $TOKEN" | grep -q 'accountNumber'

echo "[4/4] current user"
curl -fsS "$BASE_URL/api/v1/users/me" -H "Authorization: Bearer $TOKEN" | grep -q "$USERNAME"

echo "Smoke test passed."
