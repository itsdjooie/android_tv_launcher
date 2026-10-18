param(
    [string]$BuildType = "Debug"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot

Write-Host "==> Bouwen van MLHK TV+ Launcher APK ($BuildType)..." -ForegroundColor Cyan

Set-Location $ProjectRoot
$task = if ($BuildType -eq "Release") { "assembleRelease" } else { "assembleDebug" }

& ".\gradlew.bat" $task
if ($LASTEXITCODE -ne 0) {
    Write-Error "Build mislukt met exit code $LASTEXITCODE"
}

$apkDir = Join-Path $ProjectRoot "app\build\outputs\apk\$($BuildType.ToLower())"
$apkFile = Get-ChildItem -Path $apkDir -Filter "*.apk" | Select-Object -First 1

if ($apkFile) {
    $distDir = Join-Path $ProjectRoot "dist"
    if (!(Test-Path $distDir)) {
        New-Item -ItemType Directory -Path $distDir | Out-Null
    }
    $targetApk = Join-Path $distDir "mlhk-tvplus-launcher.apk"
    Copy-Item $apkFile.FullName $targetApk -Force
    Write-Host "`n[SUCCESS] APK gereed:" -ForegroundColor Green
    Write-Host "Locatie: $targetApk" -ForegroundColor Yellow
    Write-Host "Grootte: $([math]::Round($apkFile.Length / 1MB, 2)) MB" -ForegroundColor Yellow
} else {
    Write-Error "Geen APK gevonden in $apkDir"
}
