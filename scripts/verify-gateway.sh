#!/usr/bin/env bash
set -euo pipefail

gateway_url="${1:-http://127.0.0.1:18080/mcp}"
for command in curl jq; do
  command -v "$command" >/dev/null || { echo "Missing required command: $command" >&2; exit 1; }
done

headers_file="$(mktemp)"
trap 'rm -f "$headers_file"' EXIT
accept='Accept: application/json, text/event-stream'

curl --fail --silent --show-error -D "$headers_file" -o /dev/null \
  -H "$accept" -H 'Content-Type: application/json' \
  --data '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"contextguard-smoke","version":"0.0.1"}}}' \
  "$gateway_url"
session_id="$(sed -n 's/^[Mm][Cc][Pp]-[Ss]ession-[Ii]d:[[:space:]]*//p' "$headers_file" | tr -d '\r' | head -n 1)"
[[ -n "$session_id" ]] || { echo 'MCP initialize returned no session ID' >&2; exit 1; }

send() {
  curl --fail --silent --show-error \
    -H "$accept" -H 'Content-Type: application/json' \
    -H "Mcp-Session-Id: $session_id" --data "$1" "$gateway_url"
}

json_result() {
  if [[ "$1" == data:* || "$1" == event:* ]]; then
    printf '%s\n' "$1" | sed -n 's/^data:[[:space:]]*//p' | head -n 1
  else
    printf '%s\n' "$1"
  fi
}

send '{"jsonrpc":"2.0","method":"notifications/initialized"}' >/dev/null

echo_result="$(json_result "$(send '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"echo","arguments":{"text":"contact a@b.com"}}}')")"
[[ "$(printf '%s' "$echo_result" | jq -r '.result.content[0].text')" == 'contact [REDACTED]' ]] || { echo 'FAIL: email redaction' >&2; exit 1; }
echo 'PASS: email is redacted before echo'

internal_result="$(json_result "$(send '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"internal_note","arguments":{"text":"contact a@b.com"}}}')")"
[[ "$(printf '%s' "$internal_result" | jq -r '.result.content[0].text')" == 'contact a@b.com' ]] || { echo 'FAIL: tool-specific email rule' >&2; exit 1; }
echo 'PASS: approved internal tool receives the email'

blocked_result="$(json_result "$(send '{"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"internal_note","arguments":{"text":"Bearer demo_token"}}}')")"
[[ "$(printf '%s' "$blocked_result" | jq -r '.result.isError')" == true ]] || { echo 'FAIL: token blocking' >&2; exit 1; }
echo 'PASS: approved internal tool still blocks a token'
