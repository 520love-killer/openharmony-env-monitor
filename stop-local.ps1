$processes = Get-CimInstance Win32_Process | Where-Object {
    $_.Name -eq "java.exe" -and $_.CommandLine -like "*openharmony-env-monitor-platform-0.0.1-SNAPSHOT.jar*"
}

if (-not $processes) {
    Write-Host "Platform is not running." -ForegroundColor Yellow
    exit 0
}

foreach ($process in $processes) {
    Stop-Process -Id $process.ProcessId -Force
    Write-Host "Stopped process $($process.ProcessId)." -ForegroundColor Green
}
