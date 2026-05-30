Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$frontendDir = Join-Path $projectRoot 'frontend'

try {
    Set-Location $frontendDir
    $Host.UI.RawUI.WindowTitle = 'Lab Management Frontend'
    Write-Host 'Starting Vue frontend on http://127.0.0.1:5173 ...' -ForegroundColor Cyan
    npm run dev -- --host=127.0.0.1 --port=5173
}
catch {
    Write-Host ''
    Write-Host 'Frontend startup failed:' -ForegroundColor Red
    Write-Host $_ -ForegroundColor Red
}
finally {
    Write-Host ''
    Read-Host 'Frontend window will stay open. Press Enter to close'
}
