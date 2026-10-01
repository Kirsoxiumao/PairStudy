param([Parameter(Mandatory=$true)][string]$JavaHome)
$ErrorActionPreference = 'Stop'
$backendRoot = (Resolve-Path (Join-Path $PSScriptRoot '../backend')).Path
$jar = Join-Path $backendRoot 'target/pairstudy-1.0.0.jar'
if (-not (Test-Path -LiteralPath $jar)) { throw 'Run mvn package first' }
# This script is called while verify-mysql.ps1 owns the isolated 33307 database.
$oldSecret = $env:JWT_SECRET
$oldUrl = $env:DB_URL
$oldUser = $env:DB_USER
$oldPassword = $env:DB_PASSWORD
$appProcess = $null
try {
    $random = New-Object byte[] 48
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($random)
    $env:JWT_SECRET = [Convert]::ToBase64String($random)
    $env:DB_URL = 'jdbc:mysql://127.0.0.1:33307/pairstudy?serverTimezone=Asia/Shanghai'
    $env:DB_USER = 'root'; $env:DB_PASSWORD = ''
    $javaArgs = @('-jar', ('"' + $jar + '"'), '--server.port=18080', '--server.address=127.0.0.1', '--app.upload-dir=target/http-test-uploads')
    $appProcess = Start-Process -FilePath (Join-Path $JavaHome 'bin/java.exe') -ArgumentList $javaArgs -WorkingDirectory $backendRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $backendRoot 'target/http-test.out.log') -RedirectStandardError (Join-Path $backendRoot 'target/http-test.err.log')
    $ready = $false
    for ($i=0; $i -lt 30; $i++) {
        if ($appProcess.HasExited) { throw 'Packaged JAR exited unexpectedly' }
        try { $health = Invoke-RestMethod -Uri 'http://127.0.0.1:18080/api/health' -TimeoutSec 2; $ready = ($health.code -eq 200) } catch { }
        if ($ready) { break }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) { throw 'HTTP health check did not become ready' }
    $body = @{ username = ('smoke' + [Guid]::NewGuid().ToString('N').Substring(0,16)); password = 'SmokeTest12345'; nickname = 'HTTP Smoke' } | ConvertTo-Json -Compress
    $auth = Invoke-RestMethod -Uri 'http://127.0.0.1:18080/api/auth/register' -Method Post -ContentType 'application/json; charset=utf-8' -Body $body -TimeoutSec 5
    if ($auth.code -ne 200 -or -not $auth.data.token) { throw 'HTTP registration failed' }
    $pair = Invoke-RestMethod -Uri 'http://127.0.0.1:18080/api/pair/info' -Headers @{ Authorization = ('Bearer ' + $auth.data.token) } -TimeoutSec 5
    if ($pair.code -ne 200 -or -not $pair.data.effectiveDate) { throw 'Authenticated HTTP request failed' }
    Write-Output 'JAR_HTTP_SMOKE_PASS: real Tomcat health, registration, JWT and MySQL-backed pair lookup'
} finally {
    if ($appProcess -and -not $appProcess.HasExited) { Stop-Process -Id $appProcess.Id; $appProcess.WaitForExit() }
    $env:JWT_SECRET = $oldSecret; $env:DB_URL = $oldUrl; $env:DB_USER = $oldUser; $env:DB_PASSWORD = $oldPassword
}
