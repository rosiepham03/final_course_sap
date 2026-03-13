#!/usr/bin/env pwsh

# Equipment Inspection System - Development Startup Script

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Equipment Inspection Report System" -ForegroundColor Cyan
Write-Host "Development Startup Script (PowerShell)" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Check if running from correct directory
if (-not (Test-Path "pom.xml")) {
    Write-Host "Error: Please run this script from the project root directory" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Start Backend
Write-Host "Starting Backend API Server..." -ForegroundColor Green
Write-Host "Location: srv/" -ForegroundColor Gray
Write-Host ""

$backendProcess = Start-Process -FilePath "powershell" -ArgumentList "-NoExit", "-Command", "cd srv; mvn spring-boot:run" -PassThru -WindowStyle Normal
$backendPID = $backendProcess.Id

Write-Host "Backend process started with PID: $backendPID" -ForegroundColor Green

# Wait for backend to start
Write-Host ""
Write-Host "Waiting 15 seconds for backend to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Start Frontend
Write-Host ""
Write-Host "Starting Frontend Application..." -ForegroundColor Green
Write-Host "Location: app/" -ForegroundColor Gray
Write-Host ""

if (Test-Path "app") {
    # Try Python first, then fallback to Node
    $pythonPath = (Get-Command python -ErrorAction SilentlyContinue).Source
    
    if ($pythonPath) {
        Write-Host "Starting with Python http.server..." -ForegroundColor Gray
        $frontendProcess = Start-Process -FilePath "powershell" -ArgumentList "-NoExit", "-Command", "cd app; python -m http.server 3000" -PassThru -WindowStyle Normal
    } else {
        Write-Host "Starting with Node.js http-server..." -ForegroundColor Gray
        $frontendProcess = Start-Process -FilePath "powershell" -ArgumentList "-NoExit", "-Command", "cd app; npx http-server . -p 3000" -PassThru -WindowStyle Normal
    }
    
    $frontendPID = $frontendProcess.Id
    Write-Host "Frontend process started with PID: $frontendPID" -ForegroundColor Green
} else {
    Write-Host "Error: app directory not found" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Display startup information
Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "System Started Successfully!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host "Backend API:  http://localhost:8080" -ForegroundColor Cyan
Write-Host "Frontend UI:  http://localhost:3000" -ForegroundColor Cyan
Write-Host ""
Write-Host "Open your browser to: http://localhost:3000" -ForegroundColor Yellow
Write-Host ""

# Open browser
Write-Host "Opening browser..." -ForegroundColor Gray
Start-Sleep -Seconds 2

# Try to open browser using different methods
$browserOpened = $false

# Try Chrome
$chromeExe = "C:\Program Files\Google\Chrome\Application\chrome.exe"
if (Test-Path $chromeExe) {
    & $chromeExe "http://localhost:3000"
    $browserOpened = $true
} else {
    # Try Edge
    $edgeExe = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
    if (Test-Path $edgeExe) {
        & $edgeExe "http://localhost:3000"
        $browserOpened = $true
    } else {
        # Try Firefox
        $firefoxExe = "C:\Program Files\Mozilla Firefox\firefox.exe"
        if (Test-Path $firefoxExe) {
            & $firefoxExe "http://localhost:3000"
            $browserOpened = $true
        } else {
            # Use default browser
            Start-Process "http://localhost:3000"
            $browserOpened = $true
        }
    }
}

if ($browserOpened) {
    Write-Host "Browser opened successfully" -ForegroundColor Green
} else {
    Write-Host "Please manually open your browser and navigate to: http://localhost:3000" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Running Processes:" -ForegroundColor Cyan
Write-Host "  Backend (PID: $backendPID)" -ForegroundColor Gray
Write-Host "  Frontend (PID: $frontendPID)" -ForegroundColor Gray
Write-Host ""
Write-Host "To stop the application:" -ForegroundColor Yellow
Write-Host "  Stop-Process -Id $backendPID, $frontendPID" -ForegroundColor Gray
Write-Host ""
Write-Host "Two PowerShell windows have been opened." -ForegroundColor Yellow
Write-Host "Close them to stop the application." -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

Read-Host "Press Enter to continue or close this window to exit"
