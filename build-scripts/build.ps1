param(
    [switch]$SkipTests,
    [switch]$Clean
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = Resolve-Path (Join-Path $ScriptDir "..")
Set-Location $RootDir

$mvnArgs = @()
if ($Clean) {
    $mvnArgs += "clean"
}
$mvnArgs += "verify"
if ($SkipTests) {
    $mvnArgs += "-DskipTests"
}

Write-Host "Building NetBeans plugin..."
Write-Host "Command: mvn $($mvnArgs -join ' ')"

& mvn @mvnArgs
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "Build completed."
