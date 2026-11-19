@echo off
setlocal EnableDelayedExpansion
title PTEText - stop app

set FOUND=0

rem run.bat / dev-server.bat / 05-full-check.bat all spawn a window titled
rem "PTEText server" or "PTEText app" - killing the window's whole process
rem tree takes the mvn/java child down with it, no orphan window left.
for %%t in ("PTEText server" "PTEText app") do (
    tasklist /fi "WINDOWTITLE eq %%~t" /fo csv /nh 2>nul | findstr /c:"%%~t" >nul
    if not errorlevel 1 (
        echo Closing the "%%~t" window and everything running in it...
        taskkill /F /T /FI "WINDOWTITLE eq %%~t" >nul 2>&1
        set FOUND=1
    )
)

ping -n 2 127.0.0.1 >nul
netstat -ano | findstr ":8080.*LISTENING" >nul 2>&1
if errorlevel 1 (
    if !FOUND!==1 (
        echo Done. Window closed, port 8080 is free.
    ) else (
        echo Nothing on :8080 - no app window, no listener. Already stopped.
    )
    if not defined NO_PAUSE pause
    exit /b 0
)

rem Port still held - the app was started some other way (IDE, plain mvn).
rem Kill whatever owns the listener.
set PID=
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8080.*LISTENING"') do set PID=%%p
for /f "tokens=1 delims=," %%n in ('tasklist /fi "PID eq !PID!" /fo csv /nh') do set IMG=%%~n
echo Killing PID !PID! (!IMG!) - the :8080 listener...
taskkill /F /PID !PID! >nul 2>&1
if errorlevel 1 (
    echo Could not kill it - try closing the window manually.
    if not defined NO_PAUSE pause
    exit /b 1
)

ping -n 2 127.0.0.1 >nul
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
