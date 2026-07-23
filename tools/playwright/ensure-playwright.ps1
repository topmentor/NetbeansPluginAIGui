<#
.SYNOPSIS
    Detect a usable Playwright/Chromium runtime and, if absent, install one.

.DESCRIPTION
    Bundle-first, npm-fallback resolver for the "playwright-ui" agent skill.

    1. Looks for the NetBeans-bundled Playwright driver (known cache path first,
       then the newest playwright-driver-*-win32_x64 folder under the cache).
       Verifies node.exe, package/index.js, package/package.json,
       package/browsers.json and the matching Chromium chrome.exe.
    2. If the bundle is missing or incomplete, installs `playwright` locally into
       tools/playwright/.pw-runtime via npm and downloads the Chromium browser.

    On success it prints a single JSON object (mode "bundled" or "npm") to STDOUT
    with the resolved node / playwright package / browser executable paths.
    All human-readable progress goes to STDERR so STDOUT stays machine-parseable.
    On failure it prints a JSON object with mode "error" and exits non-zero.

.PARAMETER ForceNpm
    Skip the bundled driver and go straight to the npm install path.

.PARAMETER Quiet
    Suppress progress messages on STDERR.

.EXAMPLE
    $cfg = powershell -ExecutionPolicy Bypass -File tools\playwright\ensure-playwright.ps1 | ConvertFrom-Json
#>
[CmdletBinding()]
param(
    [switch]$ForceNpm,
    [switch]$Quiet
)

$ErrorActionPreference = 'Stop'
$scriptDir  = Split-Path -Parent $MyInvocation.MyCommand.Path
$runtimeDir = Join-Path $scriptDir '.pw-runtime'
# Pin to the version verified with this project (NetBeans bundle == Playwright Core 1.57).
$PlaywrightVersion = '1.57.0'
if (-not $ForceNpm -and $env:SOX_PLAYWRIGHT_FORCE_NPM -match '^(1|true|yes)$') {
    $ForceNpm = $true
}

function Write-Log([string]$msg) {
    if (-not $Quiet) { [Console]::Error.WriteLine("[ensure-playwright] $msg") }
}

function Emit($obj) {
    # Compact but readable; single JSON object is the contract with callers.
    $obj | ConvertTo-Json -Depth 6
}

function Resolve-BundledConfig {
    $known = Join-Path $env:LOCALAPPDATA 'NetBeans\Cache\28\chromium-web-preview\playwright-driver-1.57.0-win32_x64'
    $driver = $null
    if (Test-Path -LiteralPath $known) {
        $driver = $known
    } else {
        $cache = Join-Path $env:LOCALAPPDATA 'NetBeans\Cache'
        if (Test-Path -LiteralPath $cache) {
            $driver = Get-ChildItem $cache -Recurse -Directory -ErrorAction SilentlyContinue |
                Where-Object { $_.Name -like 'playwright-driver-*-win32_x64' } |
                Sort-Object LastWriteTime -Descending |
                Select-Object -First 1 -ExpandProperty FullName
        }
    }
    if (-not $driver) { Write-Log 'No NetBeans-bundled Playwright driver found.'; return $null }

    $package      = Join-Path $driver 'package'
    $node         = Join-Path $driver 'node.exe'
    $indexJs      = Join-Path $package 'index.js'
    $packageJson  = Join-Path $package 'package.json'
    $browsersJson = Join-Path $package 'browsers.json'
    foreach ($p in @($node, $indexJs, $packageJson, $browsersJson)) {
        if (-not (Test-Path -LiteralPath $p)) { Write-Log "Bundled driver incomplete, missing: $p"; return $null }
    }

    $coreVersion = (Get-Content -Raw $packageJson  | ConvertFrom-Json).version
    $browsers    = (Get-Content -Raw $browsersJson | ConvertFrom-Json).browsers
    $chromium    = $browsers | Where-Object { $_.name -eq 'chromium' } | Select-Object -First 1
    $rev         = $chromium.revision

    $chromiumRoot = Join-Path $env:LOCALAPPDATA "ms-playwright\chromium-$rev"
    $exe = $null
    if (Test-Path -LiteralPath $chromiumRoot) {
        $exe = Get-ChildItem $chromiumRoot -Recurse -Filter chrome.exe -ErrorAction SilentlyContinue |
            Select-Object -First 1 -ExpandProperty FullName
    }
    if (-not $exe) { Write-Log "Bundled Chromium (revision $rev) not downloaded yet."; return $null }

    return [ordered]@{
        mode              = 'bundled'
        driver            = $driver
        node              = $node
        playwrightPackage = $package
        browserExecutable = $exe
        coreVersion       = $coreVersion
        chromiumRevision  = $rev
    }
}

function Resolve-NpmConfig {
    $node = (Get-Command node -ErrorAction SilentlyContinue).Source
    $npm  = (Get-Command npm  -ErrorAction SilentlyContinue).Source
    if (-not $node -or -not $npm) {
        throw 'Node.js/npm not found on PATH. Install Node.js (https://nodejs.org) or provide the NetBeans-bundled Playwright driver.'
    }

    if (-not (Test-Path -LiteralPath $runtimeDir)) {
        New-Item -ItemType Directory -Force -Path $runtimeDir | Out-Null
    }
    $pkgDir = Join-Path $runtimeDir 'node_modules\playwright'

    if (-not (Test-Path -LiteralPath (Join-Path $pkgDir 'package.json'))) {
        Write-Log "Installing playwright@$PlaywrightVersion into $runtimeDir ..."
        Push-Location $runtimeDir
        try {
            if (-not (Test-Path -LiteralPath (Join-Path $runtimeDir 'package.json'))) {
                & npm init -y | Out-Null
            }
            & npm install "playwright@$PlaywrightVersion" --no-audit --no-fund *> $null
            if ($LASTEXITCODE -ne 0) { throw "npm install playwright failed (exit $LASTEXITCODE)" }
        } finally { Pop-Location }
    } else {
        Write-Log "playwright already installed in $runtimeDir"
    }

    Write-Log 'Ensuring Chromium browser is downloaded (playwright install chromium) ...'
    Push-Location $runtimeDir
    try {
        & npx --yes playwright install chromium *> $null
        if ($LASTEXITCODE -ne 0) { throw "playwright install chromium failed (exit $LASTEXITCODE)" }
        $coreVersion = (Get-Content -Raw (Join-Path $pkgDir 'package.json') | ConvertFrom-Json).version
        $exe = (& $node -e "process.stdout.write(require('playwright').chromium.executablePath())")
        if ($LASTEXITCODE -ne 0 -or -not $exe) { throw 'Could not resolve Chromium executable path from the npm install.' }
    } finally { Pop-Location }

    return [ordered]@{
        mode              = 'npm'
        driver            = $runtimeDir
        node              = $node
        playwrightPackage = $pkgDir
        browserExecutable = $exe
        coreVersion       = $coreVersion
        chromiumRevision  = $null
    }
}

try {
    $config = $null
    if (-not $ForceNpm) { $config = Resolve-BundledConfig }
    if (-not $config) {
        Write-Log 'Falling back to a local npm Playwright install.'
        $config = Resolve-NpmConfig
    }
    Write-Log "Ready (mode=$($config.mode), core=$($config.coreVersion))."
    Emit $config
    exit 0
} catch {
    Emit ([ordered]@{ mode = 'error'; error = $_.Exception.Message })
    exit 1
}
