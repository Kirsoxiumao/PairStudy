param([string]$JavaHome, [string]$SdkRoot, [string]$BaseUrl)
$ErrorActionPreference = 'Stop'
if ($JavaHome) { $env:JAVA_HOME = $JavaHome; $env:PATH = "$JavaHome\bin;$env:PATH" }
if ($SdkRoot) { $env:ANDROID_HOME = $SdkRoot }
Push-Location (Join-Path $PSScriptRoot '../android')
try {
    $buildArgs = @('--no-daemon','--console=plain','assembleDebug','testDebugUnitTest','lintDebug')
    if ($BaseUrl) { $buildArgs += "-PPAIR_STUDY_BASE_URL=$BaseUrl" }
    & './gradlew.bat' @buildArgs
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed: $LASTEXITCODE" }
} finally { Pop-Location }
