1..254 | ForEach-Object {
    $ip = "192.168.2.$_"
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $ar = $client.BeginConnect($ip, 32400, $null, $null)
        if ($ar.AsyncWaitHandle.WaitOne(80, $false) -and $client.Connected) {
            Write-Host "FOUND PLEX PORT 32400 AT: $ip" -ForegroundColor Green
            $client.EndConnect($ar)
        }
        $client.Close()
    } catch {}
}
