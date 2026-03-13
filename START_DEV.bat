@echo off
REM Start Equipment Inspection System - Development Mode

echo.
echo ============================================
echo Equipment Inspection Report System
echo Development Startup Script
echo ============================================
echo.

REM Check if running from correct directory
if not exist "pom.xml" (
    echo Error: Please run this script from the project root directory
    pause
    exit /b 1
)

REM Start Backend
echo Starting Backend API Server...
echo Location: srv/
start "Backend - Equipment Inspection API" cmd /k "cd srv && mvn spring-boot:run"

REM Wait for backend to start
echo Waiting 10 seconds for backend to start...
timeout /t 10 /nobreak

REM Start Frontend
echo.
echo Starting Frontend Application...
echo Location: app/

if exist "app\" (
    start "Frontend - Equipment Inspection UI" cmd /k "cd app && python -m http.server 3000 || npx http-server . -p 3000"
) else (
    echo Error: app directory not found
    pause
    exit /b 1
)

echo.
echo ============================================
echo System Started Successfully!
echo ============================================
echo.
echo Backend API:  http://localhost:8080
echo Frontend UI:  http://localhost:3000
echo.
echo Open your browser to: http://localhost:3000
echo.
echo Press any key to continue...
pause

REM Open browser
echo Opening browser...
start http://localhost:3000

echo.
echo Note: Two command windows have been opened:
echo 1. Backend (mvn spring-boot:run)
echo 2. Frontend (http-server on port 3000)
echo.
echo To stop the application, close both windows.
echo.
pause
