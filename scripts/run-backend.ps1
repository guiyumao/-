Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $projectRoot 'backend'

try {
    Set-Location $backendDir
    $Host.UI.RawUI.WindowTitle = 'Lab Management Backend'
    Write-Host 'Starting Spring Boot backend on http://127.0.0.1:8080 ...' -ForegroundColor Cyan
    .\mvnw.cmd spring-boot:run
}
catch {
    Write-Host ''
    Write-Host 'Backend startup failed:' -ForegroundColor Red
    Write-Host $_ -ForegroundColor Red
}
finally {
    Write-Host ''
    Read-Host 'Backend window will stay open. Press Enter to close'
}
