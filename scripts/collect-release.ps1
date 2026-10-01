param([string]$AndroidBuildDir = '')
$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
if (-not $AndroidBuildDir) { $AndroidBuildDir = Join-Path $projectRoot 'android' }
$releaseDir = Join-Path $projectRoot 'release'
$reportsDir = Join-Path $projectRoot 'docs/build-reports'
New-Item -ItemType Directory -Force -Path $releaseDir,$reportsDir | Out-Null
$apk = Join-Path $AndroidBuildDir 'app/build/outputs/apk/debug/app-debug.apk'
$jar = Join-Path $projectRoot 'backend/target/pairstudy-1.0.0.jar'
if (-not (Test-Path -LiteralPath $apk)) { throw 'Build Android first' }
if (-not (Test-Path -LiteralPath $jar)) { throw 'Build backend first' }
Copy-Item -LiteralPath $apk -Destination (Join-Path $releaseDir 'PairStudy-debug.apk') -Force
Copy-Item -LiteralPath $jar -Destination (Join-Path $releaseDir 'pairstudy-backend.jar') -Force
# Keep the conventional output path up to date when building from an ASCII mirror.
$standardDir = Join-Path $projectRoot 'android/app/build/outputs/apk/debug'
New-Item -ItemType Directory -Force -Path $standardDir | Out-Null
if ((Resolve-Path $AndroidBuildDir).Path -ne (Join-Path $projectRoot 'android')) {
    Copy-Item -LiteralPath $apk -Destination (Join-Path $standardDir 'app-debug.apk') -Force
}
Copy-Item -LiteralPath (Join-Path $AndroidBuildDir 'app/build/reports/lint-results-debug.xml') -Destination (Join-Path $reportsDir 'android-lint.xml') -Force
Copy-Item -LiteralPath (Join-Path $AndroidBuildDir 'app/build/test-results/testDebugUnitTest/TEST-com.pairstudy.app.NetworkContractTest.xml') -Destination (Join-Path $reportsDir 'android-network-tests.xml') -Force
Get-ChildItem -LiteralPath (Join-Path $projectRoot 'backend/target/surefire-reports') -Filter '*.txt' | Copy-Item -Destination $reportsDir -Force
$hashLines = Get-ChildItem -LiteralPath $releaseDir -File | Where-Object { $_.Extension -in @('.apk','.jar') } | ForEach-Object {
    $checksum = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    "$checksum  $($_.Name)"
}
$hashLines | Set-Content -LiteralPath (Join-Path $releaseDir 'SHA256SUMS.txt') -Encoding utf8
Get-ChildItem -LiteralPath $releaseDir | Select-Object Name,Length
