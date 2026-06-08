Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

. (Join-Path $PSScriptRoot 'scripts\dev-env.ps1')

function Test-PortListening {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port
    )

    try {
        return $null -ne (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction Stop | Select-Object -First 1)
    }
    catch {
        return $false
    }
}

$projectRoot = $PSScriptRoot
$backendScript = Join-Path $projectRoot 'scripts\run-backend.ps1'
$frontendScript = Join-Path $projectRoot 'scripts\run-frontend.ps1'
$config = Get-ProjectEnv -ProjectRoot $projectRoot
$backendPort = Get-EnvInt -Config $config -Key 'BACKEND_PORT'
$frontendPort = Get-EnvInt -Config $config -Key 'FRONTEND_PORT'
$frontendUrl = Get-RequiredEnvValue -Config $config -Key 'FRONTEND_PUBLIC_URL'
$backendUrl = Get-RequiredEnvValue -Config $config -Key 'BACKEND_PUBLIC_URL'
$swaggerPath = Get-RequiredEnvValue -Config $config -Key 'SWAGGER_PATH'
$swaggerUrl = $backendUrl.TrimEnd('/') + $swaggerPath
$mysqlContainerName = Get-RequiredEnvValue -Config $config -Key 'MYSQL_CONTAINER_NAME'

Write-Host 'Step 1/3: starting Docker dependencies ...' -ForegroundColor Cyan
docker compose up -d

Write-Host 'Step 2/3: waiting for MySQL health check ...' -ForegroundColor Cyan
$mysqlReady = $false
for ($i = 0; $i -lt 24; $i++) {
    $status = docker inspect --format "{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}" $mysqlContainerName 2>$null
    if ($LASTEXITCODE -eq 0 -and $status -eq 'healthy') {
        $mysqlReady = $true
        break
    }
    Start-Sleep -Seconds 5
}

if (-not $mysqlReady) {
    Write-Warning 'MySQL is not healthy yet. Backend may need a little longer to finish starting.'
}

Write-Host 'Step 3/3: opening backend and frontend windows ...' -ForegroundColor Cyan

if (Test-PortListening -Port $backendPort) {
    Write-Host "Backend is already listening on $backendPort, skip opening another backend window." -ForegroundColor Yellow
}
else {
    Start-Process powershell -ArgumentList @(
        '-NoExit',
        '-ExecutionPolicy',
        'Bypass',
        '-File',
        ('"' + $backendScript + '"')
    ) -WorkingDirectory $projectRoot
}

if (Test-PortListening -Port $frontendPort) {
    Write-Host "Frontend is already listening on $frontendPort, skip opening another frontend window." -ForegroundColor Yellow
}
else {
    Start-Process powershell -ArgumentList @(
        '-NoExit',
        '-ExecutionPolicy',
        'Bypass',
        '-File',
        ('"' + $frontendScript + '"')
    ) -WorkingDirectory $projectRoot
}

Write-Host ''
Write-Host 'Project startup command has been issued.' -ForegroundColor Green
Write-Host "Frontend: $frontendUrl"
Write-Host "Backend : $backendUrl"
Write-Host "Swagger : $swaggerUrl"
