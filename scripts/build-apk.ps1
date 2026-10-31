param(
    [string]$Target = "All", # "All", "Chromecast", "Kpn"
    [string]$BuildType = "Debug"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot

Write-Host "==> Bouwen van MLHK TV+ Launcher APK's (Target: $Target, Type: $BuildType)..." -ForegroundColor Cyan

Set-Location $ProjectRoot

$tasks = @()
if ($Target -eq "Chromecast" -or $Target -eq "All") {
    $tasks += if ($BuildType -eq "Release") { "assembleChromecastRelease" } else { "assembleChromecastDebug" }
}
if ($Target -eq "Kpn" -or $Target -eq "All") {
    $tasks += if ($BuildType -eq "Release") { "assembleKpnRelease" } else { "assembleKpnDebug" }
}

& ".\gradlew.bat" $tasks
if ($LASTEXITCODE -ne 0) {
    Write-Error "Build mislukt met exit code $LASTEXITCODE"
}

$distDir = Join-Path $ProjectRoot "dist"
if (!(Test-Path $distDir)) {
    New-Item -ItemType Directory -Path $distDir | Out-Null
}

$chromecastApk = Join-Path $ProjectRoot "app\build\outputs\apk\chromecast\$($BuildType.ToLower())\app-chromecast-$($BuildType.ToLower()).apk"
if (Test-Path $chromecastApk) {
    $targetChromecast = Join-Path $distDir "mlhk-tvplus-chromecast-woonkamer.apk"
    Copy-Item $chromecastApk $targetChromecast -Force
    Write-Host "[SUCCESS] Chromecast Woonkamer APK gereed:" -ForegroundColor Green
    Write-Host "  $targetChromecast ($([math]::Round((Get-Item $targetChromecast).Length / 1MB, 2)) MB)" -ForegroundColor Yellow
}

$kpnApk = Join-Path $ProjectRoot "app\build\outputs\apk\kpn\$($BuildType.ToLower())\app-kpn-$($BuildType.ToLower()).apk"
if (Test-Path $kpnApk) {
    $targetKpn = Join-Path $distDir "mlhk-tvplus-launcher.apk"
    Copy-Item $kpnApk $targetKpn -Force
    Write-Host "[SUCCESS] KPN TV+ Box APK gereed:" -ForegroundColor Green
    Write-Host "  $targetKpn ($([math]::Round((Get-Item $targetKpn).Length / 1MB, 2)) MB)" -ForegroundColor Yellow
}
