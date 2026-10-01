param(
    [Parameter(Mandatory=$true)][string]$JavaHome,
    [string]$Maven = 'mvn',
    [string]$MySqlBin = 'C:\Program Files\MySQL\MySQL Server 8.0\bin',
    [string]$TestRoot = '',
    [switch]$JarSmoke
)
$ErrorActionPreference = 'Stop'
$env:JAVA_HOME = $JavaHome
$env:PATH = "$JavaHome\bin;$env:PATH"
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
if (-not $TestRoot) { $TestRoot = Join-Path $projectRoot 'backend/target/mysql-verification' }
$testData = Join-Path $testRoot 'data'
New-Item -ItemType Directory -Path $testRoot -Force | Out-Null
if (-not (Test-Path -LiteralPath $testData)) {
    & (Join-Path $MySqlBin 'mysqld.exe') --no-defaults --initialize-insecure "--datadir=$testData" --console
    if ($LASTEXITCODE -ne 0) { throw 'Isolated MySQL initialization failed' }
}
# An isolated process on a dedicated loopback port; never uses the system service.
$serverArgs = @('--no-defaults', ('--datadir="' + $testData + '"'), '--port=33307', '--bind-address=127.0.0.1', '--mysqlx=OFF', '--console')
$server = Start-Process -FilePath (Join-Path $MySqlBin 'mysqld.exe') -ArgumentList $serverArgs -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $testRoot 'server.out.log') -RedirectStandardError (Join-Path $testRoot 'server.err.log')
try {
    $ready = $false
    for ($i=0; $i -lt 30; $i++) {
        if ($server.HasExited) { throw 'Isolated MySQL exited; inspect target/mysql-verification/server.err.log' }
        & (Join-Path $MySqlBin 'mysqladmin.exe') --no-defaults --protocol=TCP --host=127.0.0.1 --port=33307 --user=root --password= --connect-timeout=2 --silent ping 2>$null
        if ($LASTEXITCODE -eq 0) { $ready = $true; break }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) { throw 'Isolated MySQL not ready' }
    Get-Content -LiteralPath (Join-Path $projectRoot 'database/schema.sql') -Raw -Encoding UTF8 | & (Join-Path $MySqlBin 'mysql.exe') --no-defaults --protocol=TCP --host=127.0.0.1 --port=33307 --user=root --password= --connect-timeout=3 --default-character-set=utf8mb4
    if ($LASTEXITCODE -ne 0) { throw 'Production schema SQL failed' }
    Push-Location (Join-Path $projectRoot 'backend')
    try {
        & $Maven '-B' '-ntp' 'test' '-Dtest.db.url=jdbc:mysql://127.0.0.1:33307/pairstudy?serverTimezone=Asia/Shanghai' '-Dtest.db.user=root'
        if ($LASTEXITCODE -ne 0) { throw 'MySQL integration tests failed' }
    } finally { Pop-Location }
    if ($JarSmoke) {
        & (Join-Path $PSScriptRoot 'verify-jar.ps1') -JavaHome $JavaHome
    }
} finally {
    if (-not $server.HasExited) {
        & (Join-Path $MySqlBin 'mysqladmin.exe') --no-defaults --protocol=TCP --host=127.0.0.1 --port=33307 --user=root --password= --connect-timeout=3 shutdown 2>$null
        if (-not $server.WaitForExit(10000)) { Stop-Process -Id $server.Id }
    }
}
