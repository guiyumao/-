Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $projectRoot 'backend'
. (Join-Path $projectRoot 'scripts\dev-env.ps1')
$config = Get-ProjectEnv -ProjectRoot $projectRoot
$backendUrl = Get-RequiredEnvValue -Config $config -Key 'BACKEND_PUBLIC_URL'
$javaHome = Get-RequiredEnvValue -Config $config -Key 'APP_JAVA_HOME'

try {
    Set-Location $backendDir
    $Host.UI.RawUI.WindowTitle = 'Lab Management Backend'
    foreach ($entry in $config.GetEnumerator()) {
        Set-Item -Path ("Env:" + $entry.Key) -Value $entry.Value
    }
    $env:JAVA_HOME = $javaHome
    $env:Path = "$javaHome\bin;" + [System.Environment]::GetEnvironmentVariable('Path', 'Machine') + ';' + [System.Environment]::GetEnvironmentVariable('Path', 'User')
    Write-Host "Starting Spring Boot backend on $backendUrl ..." -ForegroundColor Cyan
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
