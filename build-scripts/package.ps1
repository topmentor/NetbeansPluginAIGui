param(
    [switch]$SkipTests,
    [switch]$Clean,
    [string]$OutputDir = "dist"
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = Resolve-Path (Join-Path $ScriptDir "..")
Set-Location $RootDir

$mvnArgs = @()
if ($Clean) {
    $mvnArgs += "clean"
}
$mvnArgs += "package"
if ($SkipTests) {
    $mvnArgs += "-DskipTests"
}

Write-Host "Packaging NetBeans plugin..."
Write-Host "Command: mvn $($mvnArgs -join ' ')"

& mvn @mvnArgs
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$distPath = Join-Path $RootDir $OutputDir
New-Item -ItemType Directory -Force -Path $distPath | Out-Null

$nbmFiles = Get-ChildItem -Path (Join-Path $RootDir "target") -Filter "*.nbm" -File -Recurse |
    Sort-Object { $_.FullName.Length } |
    Group-Object Name |
    ForEach-Object { $_.Group[0] }
if (-not $nbmFiles) {
    Write-Error "No .nbm file found under target."
}

foreach ($file in $nbmFiles) {
    Copy-Item -LiteralPath $file.FullName -Destination $distPath -Force
    Write-Host "Packaged: $OutputDir/$($file.Name)"
}

Write-Host ""
Write-Host "Package completed."
