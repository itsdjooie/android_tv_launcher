param(
    [string]$Url = "http://localhost:8080/api/status",
    [string]$ApiKey = "mlhk-secure-key"
)

Write-Host "==> Testen van MLHK Centrale Monitoring API ($Url)..." -ForegroundColor Cyan

try {
    $headers = @{
        "X-API-Key" = $ApiKey
    }
    $response = Invoke-RestMethod -Uri $Url -Headers $headers -TimeoutSec 5
    Write-Host "`n[Status Resultaat]" -ForegroundColor Green
    Write-Host "Status      : $($response.status.ToUpper())" -ForegroundColor $(if ($response.status -eq "green") { "Green" } elseif ($response.status -eq "orange") { "Yellow" } else { "Red" })
    Write-Host "Melding     : $($response.message)"
    Write-Host "Tijdstip    : $($response.timestamp)"
    Write-Host "`nDienstenoverzicht:" -ForegroundColor Cyan
    foreach ($svc in $response.services) {
        $color = if ($svc.status -eq "up") { "Green" } elseif ($svc.status -eq "degraded") { "Yellow" } else { "Red" }
        Write-Host ("  [{0,-4}] {1,-20} - {2} ({3} ms)" -f $svc.status.ToUpper(), $svc.name, $svc.message, $svc.latencyMs) -ForegroundColor $color
    }
} catch {
    Write-Error "Fout bij benaderen monitoring API: $_"
}
