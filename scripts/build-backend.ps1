param([string]$JavaHome, [string]$Maven = 'mvn', [switch]$SkipTests)
$ErrorActionPreference = 'Stop'
if ($JavaHome) { $env:JAVA_HOME = $JavaHome; $env:PATH = "$JavaHome\bin;$env:PATH" }
Push-Location (Join-Path $PSScriptRoot '../backend')
try {
  $buildArgs = @('-B','-ntp','package')
  if ($SkipTests) { $buildArgs += '-DskipTests' }
  & $Maven @buildArgs
  if ($LASTEXITCODE -ne 0) { throw "Maven failed: $LASTEXITCODE" }
} finally { Pop-Location }
