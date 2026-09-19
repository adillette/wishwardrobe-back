$out = "D:\msaprocject\wishwardrobe-back\inflight_$(Get-Date -Format 'yyyyMMdd_HHmmss').csv"
"timestamp,inflight" | Out-File $out -Encoding utf8
while ($true) {
    $ts = Get-Date -Format o
    $text = (Invoke-WebRequest -Uri "http://localhost:9091/actuator/prometheus" -UseBasicParsing).Content
    $line = $text -split "`n" | Where-Object { $_ -match '^broadcast_inflight_messages\s' }
    $val = ($line -split '\s+')[1]
    "$ts,$val" | Out-File $out -Append -Encoding utf8
    Start-Sleep -Milliseconds 200
}
