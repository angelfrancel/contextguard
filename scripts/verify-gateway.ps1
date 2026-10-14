param(
    [string]$GatewayUrl = 'http://127.0.0.1:8080/mcp'
)

$ErrorActionPreference = 'Stop'
$headers = @{ Accept = 'application/json, text/event-stream' }

function Send-McpMessage {
    param(
        [string]$Method,
        [object]$Parameters,
        [int]$Id
    )

    $message = @{ jsonrpc = '2.0'; method = $Method }
    if ($Id -gt 0) { $message.id = $Id }
    if ($null -ne $Parameters) { $message.params = $Parameters }

    $body = $message | ConvertTo-Json -Depth 30 -Compress
    $response = Invoke-WebRequest -UseBasicParsing -Uri $GatewayUrl -Method Post -ContentType 'application/json' -Headers $headers -Body $body

    if ($Id -eq 0 -or $Method -eq 'initialize') { return $response }

    $content = $response.Content
    if ($content -match '(?m)^data:(\{[^\r\n]+\})') {
        $content = $Matches[1]
    }
    return $content | ConvertFrom-Json
}

$initialize = Send-McpMessage -Method 'initialize' -Id 1 -Parameters @{
    protocolVersion = '2025-11-25'
    capabilities = @{}
    clientInfo = @{ name = 'contextguard-smoke'; version = '0.0.1' }
}
$sessionId = $initialize.Headers['Mcp-Session-Id'] | Select-Object -First 1
if (-not $sessionId) { throw 'MCP initialize did not return a session ID' }
$headers['Mcp-Session-Id'] = [string]$sessionId
$null = Send-McpMessage -Method 'notifications/initialized' -Id 0

$listed = Send-McpMessage -Method 'tools/list' -Id 2 -Parameters @{}
$names = @($listed.result.tools | ForEach-Object { $_.name })
if ('echo' -notin $names -or 'uppercase' -notin $names) {
    throw "Expected echo and uppercase, found: $($names -join ', ')"
}
Write-Output 'PASS: gateway lists echo and uppercase'

function Assert-ToolCall {
    param(
        [int]$Id,
        [string]$Name,
        [string]$Text,
        [string]$ExpectedText,
        [bool]$ExpectedError = $false
    )

    $response = Send-McpMessage -Method 'tools/call' -Id $Id -Parameters @{
        name = $Name
        arguments = @{ text = $Text }
    }
    if ([bool]$response.result.isError -ne $ExpectedError) {
        throw "Unexpected error status for $Name"
    }
    if ($PSBoundParameters.ContainsKey('ExpectedText') -and
            $response.result.content[0].text -ne $ExpectedText) {
        throw "Unexpected response text for $Name"
    }
}

Assert-ToolCall -Id 3 -Name 'echo' -Text 'hello' -ExpectedText 'hello'
Write-Output 'PASS: plain echo reaches upstream'
Assert-ToolCall -Id 4 -Name 'echo' -Text 'contact a@b.com' -ExpectedText 'contact [REDACTED]'
Write-Output 'PASS: email is redacted before echo'
Assert-ToolCall -Id 5 -Name 'uppercase' -Text 'contact a@b.com' -ExpectedText 'CONTACT [REDACTED]'
Write-Output 'PASS: second tool uses the same guard'
Assert-ToolCall -Id 6 -Name 'echo' -Text 'card 4111 1111 1111 1111' -ExpectedText 'card [REDACTED]'
Write-Output 'PASS: payment card is redacted'
Assert-ToolCall -Id 7 -Name 'uppercase' -Text 'Bearer demo_token' -ExpectedError $true
Write-Output 'PASS: token is blocked'
