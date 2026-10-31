param(
    [string]$DeviceIp = "",
    [int]$Port = 5555,
    [string]$ApkPath = "dist\mlhk-tvplus-chromecast-woonkamer.apk",
    [switch]$SetAsDefault = $false
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
    Write-Error "ADB niet gevonden. Zorg dat de Android SDK platform-tools geinstalleerd zijn."
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " MLHK TV+ (Woonkamer) — Chromecast with Google TV Deploy" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

if ([string]::IsNullOrWhiteSpace($DeviceIp)) {
    Write-Host "`nGeen IP-adres opgegeven. Zoeken naar apparaten..." -ForegroundColor Yellow
    $devices = & $adbPath devices | Select-String "device$"
    if ($devices.Count -eq 1) {
        $DeviceIp = ($devices[0] -split "\s+")[0] -replace ":5555",""
        Write-Host "Gevonden actief apparaat: $DeviceIp" -ForegroundColor Green
    } else {
        $DeviceIp = Read-Host "Voer het IP-adres van je Chromecast in (bijv. 192.168.2.65)"
    }
}

Write-Host "==> Verbinding maken met Chromecast ($DeviceIp:$Port)..." -ForegroundColor Cyan
& $adbPath connect "$DeviceIp`:$Port"

Start-Sleep -Seconds 2

$devicesOutput = & $adbPath devices
Write-Host $devicesOutput

if ($devicesOutput -notmatch "$DeviceIp`:$Port\s+device") {
    Write-Warning "Chromecast $DeviceIp:$Port is niet geautoriseerd of offline."
    Write-Host "`nStappen om ADB in te schakelen op je Chromecast with Google TV:" -ForegroundColor Yellow
    Write-Host "1. Ga op de TV naar: Instellingen -> Systeem -> Over."
    Write-Host "2. Scroll naar 'Build van Android TV OS' en druk 7x achter elkaar op de OK-knop."
    Write-Host "3. Ga terug naar Systeem -> Ontwikkelaarsopties -> Schakel 'Foutopsporing via USB / Netwerk (ADB)' in."
    Write-Host "4. Bevestig de RSA-sleutel melding ('Altijd toestaan') op het televisiescherm."
    exit 1
}

# 2. APK Controleren / Bouwen
$fullApkPath = Join-Path $ProjectRoot $ApkPath
if (-not (Test-Path $fullApkPath)) {
    Write-Host "Bouwen van Chromecast Woonkamer APK..." -ForegroundColor Cyan
    & (Join-Path $PSScriptRoot "build-apk.ps1") -Target "Chromecast"
}

Write-Host "==> Installeren van $fullApkPath op Chromecast Woonkamer..." -ForegroundColor Cyan
& $adbPath -s "$DeviceIp`:$Port" install -r -d $fullApkPath
if ($LASTEXITCODE -ne 0) {
    Write-Error "Installatie via ADB mislukt."
}

# 3. Start launcher
Write-Host "==> Starten van MLHK TV+ (Woonkamer)..." -ForegroundColor Cyan
& $adbPath -s "$DeviceIp`:$Port" shell am start -n "nl.mlhk.tvlauncher.chromecast.debug/nl.mlhk.tvlauncher.MainActivity"

# 4. Optioneel als standaard launcher instellen
if ($SetAsDefault) {
    Write-Host "==> Google TV stock launcher uitschakelen zodat MLHK de enige start-app is..." -ForegroundColor Cyan
    & $adbPath -s "$DeviceIp`:$Port" shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
    Write-Host "[OK] MLHK TV+ is nu de actieve standaard launcher op je Chromecast!" -ForegroundColor Green
} else {
    Write-Host "`n[TIP] Wil je MLHK TV+ als definitieve standaard startscherm instellen op Chromecast?" -ForegroundColor Yellow
    Write-Host "Voer dan uit: .\scripts\deploy-chromecast.ps1 -DeviceIp $DeviceIp -SetAsDefault" -ForegroundColor White
    Write-Host "Of handmatig via ADB:" -ForegroundColor Gray
    Write-Host "  adb -s $DeviceIp`:$Port shell pm disable-user --user 0 com.google.android.apps.tv.launcherx" -ForegroundColor Gray
}

Write-Host "`n[SUCCESS] MLHK TV+ (Woonkamer) is klaar voor gebruik!" -ForegroundColor Green
Write-Host "Nood-failback: Houd de Home-knop 2 sec ingedrukt om altijd terug te keren naar Google TV." -ForegroundColor Cyan
Write-Host "Herstelscript: .\scripts\restore-chromecast.ps1 -DeviceIp $DeviceIp" -ForegroundColor Yellow
