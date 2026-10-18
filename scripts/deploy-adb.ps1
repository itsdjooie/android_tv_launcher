param(
    [string]$DeviceIp = "192.168.2.26",
    [int]$Port = 5555,
    [string]$ApkPath = "dist\mlhk-tvplus-launcher.apk"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot

$adbCandidates = @(
    "adb",
    "C:\Users\joey\AppData\Local\Packages\Claude_pzs8sxrjxfjjc\LocalCache\Local\Android\Sdk\platform-tools\adb.exe",
    "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
)

$adbPath = $null
foreach ($c in $adbCandidates) {
    if (Get-Command $c -ErrorAction SilentlyContinue) {
        $adbPath = $c
        break
    } elseif (Test-Path $c) {
        $adbPath = $c
        break
    }
}

if (-not $adbPath) {
    Write-Error "ADB niet gevonden. Zorg dat de Android SDK platform-tools geïnstalleerd zijn."
}

Write-Host "==> Gebruikt ADB: $adbPath" -ForegroundColor Cyan

# 1. Connectie controleren
Write-Host "==> Verbinding maken met KPN TV+ Box ($DeviceIp:$Port)..." -ForegroundColor Cyan
& $adbPath connect "$DeviceIp`:$Port"

Start-Sleep -Seconds 2

$devicesOutput = & $adbPath devices
Write-Host $devicesOutput

if ($devicesOutput -notmatch "$DeviceIp`:$Port\s+device") {
    Write-Warning "Apparaat $DeviceIp:$Port is niet geautoriseerd of offline."
    Write-Host "`nStappen om ADB in te schakelen op je KPN TV+ Box (DIW7022):" -ForegroundColor Yellow
    Write-Host "1. Ga op de TV naar: Instellingen -> Apparaatvoorkeuren -> Info."
    Write-Host "2. Druk 7x op 'Build' om Ontwikkelaarsopties te ontgrendelen."
    Write-Host "3. Ga naar Ontwikkelaarsopties -> Schakel 'Foutopsporing via USB / Netwerk (ADB)' in."
    Write-Host "4. Bevestig de RSA-sleutelpop-up op het televisiescherm."
    exit 1
}

# 2. APK Installeren
$fullApkPath = Join-Path $ProjectRoot $ApkPath
if (-not (Test-Path $fullApkPath)) {
    Write-Host "Bouwen van APK..." -ForegroundColor Cyan
    & (Join-Path $PSScriptRoot "build-apk.ps1")
}

Write-Host "==> Installeren van $fullApkPath op $DeviceIp..." -ForegroundColor Cyan
& $adbPath -s "$DeviceIp`:$Port" install -r -d $fullApkPath
if ($LASTEXITCODE -ne 0) {
    Write-Error "Installatie via ADB mislukt."
}

# 3. Start de launcher
Write-Host "==> Starten van MLHK TV+ Launcher..." -ForegroundColor Cyan
& $adbPath -s "$DeviceIp`:$Port" shell am start -n "nl.mlhk.tvlauncher.debug/nl.mlhk.tvlauncher.MainActivity"

Write-Host "`n[SUCCESS] MLHK TV+ Launcher succesvol geïnstalleerd en gestart op je KPN TV+ Box!" -ForegroundColor Green
Write-Host "Controleer het televisiescherm. Om terug te keren naar de originele KPN launcher voer je uit:"
Write-Host ".\scripts\restore-kpn.ps1 -DeviceIp $DeviceIp" -ForegroundColor Yellow
