param(
    [string]$DeviceIp = "",
    [int]$Port = 5555,
    [string]$GoogleTvPackage = "com.google.android.apps.tv.launcherx"
)

$ErrorActionPreference = "Stop"

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
    Write-Error "ADB niet gevonden."
}

if ([string]::IsNullOrWhiteSpace($DeviceIp)) {
    $devices = & $adbPath devices | Select-String "device$"
    if ($devices.Count -eq 1) {
        $DeviceIp = ($devices[0] -split "\s+")[0] -replace ":5555",""
    } else {
        $DeviceIp = Read-Host "Voer het IP-adres van je Chromecast in"
    }
}

Write-Host "==> Verbinding maken met Chromecast ($DeviceIp:$Port)..." -ForegroundColor Cyan
& $adbPath connect "$DeviceIp`:$Port"

Write-Host "==> Google TV stock launcher opnieuw inschakelen..." -ForegroundColor Cyan
& $adbPath -s "$DeviceIp`:$Port" shell pm enable $GoogleTvPackage

Write-Host "==> Google TV interface starten..." -ForegroundColor Cyan
& $adbPath -s "$DeviceIp`:$Port" shell am start -n "$GoogleTvPackage/.MainActivity"

Write-Host "`n[SUCCESS] Originele Google TV interface hersteld op Chromecast!" -ForegroundColor Green
