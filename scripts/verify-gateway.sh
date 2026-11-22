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

listed="$(json_result "$(send '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}')")"
printf '%s' "$listed" | jq -e '([.result.tools[].name] | index("echo") != null and index("uppercase") != null and index("internal_note") != null)' >/dev/null || { echo 'FAIL: expected echo, uppercase, and internal_note' >&2; exit 1; }
echo 'PASS: gateway lists echo, uppercase, and internal_note'

assert_tool_call() {
  local id="$1" name="$2" input="$3" expected_text="$4" expected_error="$5"
  local request response actual_error actual_text
  request="$(jq -nc --argjson id "$id" --arg name "$name" --arg text "$input" '{jsonrpc:"2.0",id:$id,method:"tools/call",params:{name:$name,arguments:{text:$text}}}')"
  response="$(json_result "$(send "$request")")"
  actual_error="$(printf '%s' "$response" | jq -r '.result.isError // false')"
  [[ "$actual_error" == "$expected_error" ]] || { echo "FAIL: unexpected error status for $name" >&2; exit 1; }
  if [[ "$expected_error" == false ]]; then
    actual_text="$(printf '%s' "$response" | jq -r '.result.content[0].text')"
    [[ "$actual_text" == "$expected_text" ]] || { echo "FAIL: unexpected response text for $name" >&2; exit 1; }
  fi
}

assert_tool_call 3 echo 'hello' 'hello' false
echo 'PASS: plain echo reaches upstream'
assert_tool_call 4 echo 'contact a@b.com' 'contact [REDACTED]' false
echo 'PASS: email is redacted before echo'
assert_tool_call 5 internal_note 'contact a@b.com' 'contact a@b.com' false
echo 'PASS: approved internal tool receives the email'
assert_tool_call 6 uppercase 'contact a@b.com' 'CONTACT [REDACTED]' false
echo 'PASS: second tool uses the same guard'
assert_tool_call 7 echo 'card 4111 1111 1111 1111' 'card [REDACTED]' false
echo 'PASS: payment card is redacted'
assert_tool_call 8 uppercase 'Bearer demo_token' '' true
echo 'PASS: token is blocked'
assert_tool_call 9 internal_note 'Bearer demo_token' '' true
echo 'PASS: approved internal tool still blocks a token'
