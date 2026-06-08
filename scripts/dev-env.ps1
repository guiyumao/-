Set-StrictMode -Version Latest

function Get-ProjectEnv {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ProjectRoot
    )

    $envFile = Join-Path $ProjectRoot '.env'
    if (-not (Test-Path $envFile)) {
        throw "Missing environment file: $envFile"
    }

    $values = @{}
    foreach ($line in Get-Content $envFile) {
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith('#')) {
            continue
        }

        $parts = $trimmed -split '=', 2
        if ($parts.Length -ne 2) {
            continue
        }

        $key = $parts[0].Trim()
        $value = $parts[1].Trim()
        if ($key) {
            $values[$key] = $value
        }
    }

    return $values
}

function Get-RequiredEnvValue {
    param(
        [Parameter(Mandatory = $true)]
        [hashtable]$Config,
        [Parameter(Mandatory = $true)]
        [string]$Key
    )

    if (-not $Config.ContainsKey($Key) -or [string]::IsNullOrWhiteSpace($Config[$Key])) {
        throw "Missing required config key: $Key"
    }

    return $Config[$Key]
}

function Get-EnvInt {
    param(
        [Parameter(Mandatory = $true)]
        [hashtable]$Config,
        [Parameter(Mandatory = $true)]
        [string]$Key
    )

    return [int](Get-RequiredEnvValue -Config $Config -Key $Key)
}
