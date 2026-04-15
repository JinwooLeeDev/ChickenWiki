$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontend = Join-Path $root "frontend"

if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    $env:DB_PASSWORD = Read-Host "Supabase DB password"
}

if ([string]::IsNullOrWhiteSpace($env:JWT_SECRET)) {
    $env:JWT_SECRET = "local-dev-secret-change-me"
}

$backendCommand = "Set-Location -LiteralPath '$root'; .\gradlew.bat bootRun"
$frontendCommand = "Set-Location -LiteralPath '$frontend'; npm run dev"

Write-Host ""
Write-Host "Starting ChickenWiki backend and frontend..."
Write-Host "Backend : http://localhost:8080"
Write-Host "Frontend: http://localhost:5173"
Write-Host ""

Start-Process powershell.exe -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-Command", $backendCommand
Start-Sleep -Seconds 3
Start-Process powershell.exe -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-Command", $frontendCommand

Write-Host "Two PowerShell windows were opened."
Write-Host "Close those windows to stop each server."
