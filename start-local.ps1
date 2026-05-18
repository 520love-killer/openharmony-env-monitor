$ErrorActionPreference = "Stop"

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$jar = Join-Path $projectDir "target\openharmony-env-monitor-platform-0.0.1-SNAPSHOT.jar"
$logsDir = Join-Path $projectDir "logs"
$outLog = Join-Path $logsDir "app.out.log"
$errLog = Join-Path $logsDir "app.err.log"

New-Item -ItemType Directory -Force $logsDir | Out-Null

$existing = Get-CimInstance Win32_Process | Where-Object {
    $_.Name -eq "java.exe" -and $_.CommandLine -like "*openharmony-env-monitor-platform-0.0.1-SNAPSHOT.jar*"
}

if ($existing) {
    Write-Host "Platform is already running:" -ForegroundColor Yellow
    $existing | Select-Object ProcessId, CommandLine | Format-Table -AutoSize
    Write-Host "URL: http://localhost:8080" -ForegroundColor Green
    exit 0
}

if (-not (Test-Path $jar)) {
    Write-Host "Jar not found, building with Maven..." -ForegroundColor Yellow
    Push-Location $projectDir
    mvn clean package
    Pop-Location
}

Start-Process -FilePath "java.exe" `
    -ArgumentList @("-jar", $jar) `
    -WorkingDirectory $projectDir `
    -RedirectStandardOutput $outLog `
    -RedirectStandardError $errLog `
    -WindowStyle Hidden

Start-Sleep -Seconds 5

$listening = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($listening) {
    Write-Host "Local deployment started successfully." -ForegroundColor Green
    Write-Host "URL: http://localhost:8080" -ForegroundColor Green
    Write-Host "Logs: $outLog"
} else {
    Write-Host "Local deployment may have failed. Check logs:" -ForegroundColor Red
    Write-Host $outLog
    Write-Host $errLog
}
