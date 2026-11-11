@echo off
setlocal EnableDelayedExpansion
title PTEText - stop app

set PID=
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8080.*LISTENING"') do set PID=%%p

if not defined PID (
    echo Nothing is listening on :8080 - app is not running.
    if not defined NO_PAUSE pause
    exit /b 0
)

for /f "tokens=1 delims=," %%n in ('tasklist /fi "PID eq %PID%" /fo csv /nh') do set IMG=%%~n
echo Killing PID %PID% (!IMG!) - the :8080 listener...
taskkill /F /PID %PID% >nul 2>&1
if errorlevel 1 (
    echo Could not kill it - try closing the window manually.
    if not defined NO_PAUSE pause
    exit /b 1
)

"%SystemRoot%\System32\timeout.exe" /t 1 /nobreak >nul
netstat -ano | findstr ":8080.*LISTENING" >nul 2>&1
if errorlevel 1 (
    echo Done. Port 8080 is free.
    if not defined NO_PAUSE pause
    exit /b 0
)
echo Process died but :8080 is still held - something else
echo grabbed it. Check netstat -ano ^| findstr :8080 yourself.
if not defined NO_PAUSE pause
exit /b 1
