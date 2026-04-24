@echo off
echo ================================================================================
echo                    TimeSaver Load Test Launcher
echo ================================================================================
echo.

echo Step 1: Checking if backend is running on port 8081 (Docker)...
echo.
python check_backend.py http://localhost:8081

if errorlevel 1 (
    echo.
    echo ================================================================================
    echo ERROR: Backend is not running!
    echo ================================================================================
    echo.
    echo Please start your backend first:
    echo   1. Make sure Docker is running
    echo   2. Run: docker-compose up
    echo   3. Wait for "Started TimesaverApplication" message
    echo   4. Backend should be on port 8081
    echo   5. Then run this script again
    echo.
    pause
    exit /b 1
)

echo.
echo ================================================================================
echo Step 2: Starting Locust load test...
echo ================================================================================
echo.
echo The web interface will open at: http://localhost:8089
echo.
echo Configure the test:
echo   - Number of users: 10-20
echo   - Spawn rate: 1-2
echo   - Host: http://localhost:8081
echo.
echo Press Ctrl+C to stop the test
echo ================================================================================
echo.

locust -f load_test.py --host http://localhost:8081

pause
