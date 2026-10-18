param(
    [string]$DeviceIp = "192.168.2.26",
    [int]$Port = 5555,
    [string]$KpnPackage = "com.kpn.tvlauncher"
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

Write-Host "==> Verbinding maken met KPN TV+ Box ($DeviceIp:$Port)..." -ForegroundColor Cyan
& $adbPath connect "$DeviceIp`:$Port"

Write-Host "==> Originele KPN TV+ Launcher starten..." -ForegroundColor Cyan
& $adbPath -s "$DeviceIp`:$Port" shell am start -n "$KpnPackage/.MainActivity"

Write-Host "==> Standaard launcher voorkeur herstellen..." -ForegroundColor Cyan
& $adbPath -s "$DeviceIp`:$Port" shell cmd package set-home-activity $KpnPackage

Write-Host "`n[SUCCESS] Originele KPN TV+ interface hersteld!" -ForegroundColor Green
