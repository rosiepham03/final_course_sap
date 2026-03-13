#!/bin/bash

# Equipment Inspection System - Development Startup Script
# For macOS and Linux

echo ""
echo "============================================"
echo "Equipment Inspection Report System"
echo "Development Startup Script (Bash)"
echo "============================================"
echo ""

# Check if running from correct directory
if [ ! -f "pom.xml" ]; then
    echo "Error: Please run this script from the project root directory"
    read -p "Press Enter to exit"
    exit 1
fi

# Check for required tools
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Install Maven from: https://maven.apache.org/"
    read -p "Press Enter to exit"
    exit 1
fi

# Start Backend
echo "Starting Backend API Server..."
echo "Location: srv/"
echo ""

cd srv
mvn spring-boot:run &
BACKEND_PID=$!

echo "Backend process started with PID: $BACKEND_PID"

# Wait for backend to start
echo ""
echo "Waiting 15 seconds for backend to initialize..."
sleep 15

# Go back to project root
cd ..

# Start Frontend
echo ""
echo "Starting Frontend Application..."
echo "Location: app/"
echo ""

if [ -d "app" ]; then
    cd app
    
    # Check for Python
    if command -v python3 &> /dev/null; then
        echo "Starting with Python 3 http.server..."
        python3 -m http.server 3000 &
        FRONTEND_PID=$!
    elif command -v python &> /dev/null; then
        echo "Starting with Python http.server..."
        python -m http.server 3000 &
        FRONTEND_PID=$!
    elif command -v npx &> /dev/null; then
        echo "Starting with Node.js http-server..."
        npx http-server . -p 3000 &
        FRONTEND_PID=$!
    else
        echo "Error: Neither Python nor Node.js is installed"
        echo "Please install Python 3 or Node.js"
        read -p "Press Enter to exit"
        exit 1
    fi
    
    echo "Frontend process started with PID: $FRONTEND_PID"
else
    echo "Error: app directory not found"
    read -p "Press Enter to exit"
    exit 1
fi

# Display startup information
echo ""
echo "============================================"
echo "System Started Successfully!"
echo "============================================"
echo ""
echo "Backend API:  http://localhost:8080"
echo "Frontend UI:  http://localhost:3000"
echo ""
echo "Open your browser to: http://localhost:3000"
echo ""
echo "============================================"
echo "Running Processes:"
echo "  Backend (PID: $BACKEND_PID)"
echo "  Frontend (PID: $FRONTEND_PID)"
echo ""
echo "To stop the application:"
echo "  kill $BACKEND_PID $FRONTEND_PID"
echo ""
echo "Or use Ctrl+C to stop the current process"
echo "============================================"
echo ""

# Try to open browser
if command -v open &> /dev/null; then
    # macOS
    open http://localhost:3000
elif command -v xdg-open &> /dev/null; then
    # Linux
    xdg-open http://localhost:3000
elif command -v google-chrome &> /dev/null; then
    google-chrome http://localhost:3000
elif command -v firefox &> /dev/null; then
    firefox http://localhost:3000
else
    echo "Please manually open your browser and navigate to: http://localhost:3000"
fi

echo ""
echo "Press Ctrl+C to stop the application"
echo ""

# Wait for keyboard interrupt
wait
