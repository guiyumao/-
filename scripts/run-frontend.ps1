Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$frontendDir = Join-Path $projectRoot 'frontend'
. (Join-Path $projectRoot 'scripts\dev-env.ps1')
$config = Get-ProjectEnv -ProjectRoot $projectRoot
$frontendUrl = Get-RequiredEnvValue -Config $config -Key 'FRONTEND_PUBLIC_URL'
$frontendHost = Get-RequiredEnvValue -Config $config -Key 'FRONTEND_HOST'
$frontendPort = Get-RequiredEnvValue -Config $config -Key 'FRONTEND_PORT'

try {
    Set-Location $frontendDir
    $Host.UI.RawUI.WindowTitle = 'Lab Management Frontend'
    Write-Host "Starting Vue frontend on $frontendUrl ..." -ForegroundColor Cyan
    npm run dev -- --host=$frontendHost --port=$frontendPort
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
