@echo off
setlocal

set PID=
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8080.*LISTENING"') do set PID=%%p

if not defined PID (
    echo Nothing is listening on :8080 - app is not running.
    pause
    exit /b 0
)

echo Killing PID %PID% (the :8080 listener)...
taskkill /F /PID %PID% >nul 2>&1
if errorlevel 1 (
    echo Could not kill it - try closing the window manually.
    exit /b 1
)
echo Done. Port 8080 is free.
pause
exit /b 0
