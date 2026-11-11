@echo off
setlocal EnableDelayedExpansion
title PTEText check 01 - requirements
if /i "%~1"=="verbose" set VERBOSE=1
set PASS=0
set FAILS=0
call "%~dp0_detect.bat"
set "LOGFILE=%LOGDIR%\01-requirements-%STAMP%.log"
set "OUT=%LOGDIR%\check.tmp"

echo ============================================================
echo   PTEText - environment check
echo   log: %LOGFILE%
echo ============================================================
echo.

echo [Java] >> "%LOGFILE%"
set JVER=
java -version > "%OUT%" 2>&1
for /f "tokens=3" %%v in ('type "%OUT%" ^| findstr /i "version"') do set JVER=%%~v
type "%OUT%" >> "%LOGFILE%"
if not defined JVER (
    echo   FAIL - java not on PATH. Install a JDK 17+ first.
    set /a FAILS+=1
) else (
    echo   OK - found !JVER!
    for /f "delims=. tokens=1" %%m in ("!JVER!") do set JAVAMAJ=%%m
    if !JAVAMAJ! LSS 17 (
        echo   FAIL - Java !JAVAMAJ! is too old, the project needs 17+.
        set /a FAILS+=1
    ) else (
        echo   OK - major version !JAVAMAJ! ^>= 17
        set /a PASS+=1
    )
)
echo.

echo [Maven] >> "%LOGFILE%"
set MVNVER=
call mvn -version > "%OUT%" 2>&1
for /f "tokens=3" %%v in ('type "%OUT%" ^| findstr /b Apache') do set MVNVER=%%v
type "%OUT%" >> "%LOGFILE%"
if not defined MVNVER (
    echo   FAIL - mvn not on PATH. Install Maven first.
    set /a FAILS+=1
) else (
    echo   OK - Apache Maven !MVNVER!
    set /a PASS+=1
)
echo.

echo [MySQL client] >> "%LOGFILE%"
if not defined MYSQL (
    echo   FAIL - mysql.exe not found anywhere checked:
    echo          PATH, C:\xampp, D:\xampp, E:\xampp, wamp64,
    echo          Program Files\MySQL, Program Files\MariaDB
    echo   Either install XAMPP or set a MYSQL env var with the full path.
    set /a FAILS+=1
) else (
    "%MYSQL%" --version > "%OUT%" 2>&1
    if errorlevel 1 (
        echo   FAIL - found %MYSQL% but it will not run.
        set /a FAILS+=1
    ) else (
        echo   OK - at %MYSQL%
        set /a PASS+=1
    )
    type "%OUT%" >> "%LOGFILE%"
)
echo.

echo [MySQL server] >> "%LOGFILE%"
if not defined MYSQL (
    echo   SKIP - no client, nothing to ping with.
) else (
    if defined MYSQLADMIN (
        "%MYSQLADMIN%" -u root ping > "%OUT%" 2>&1
    ) else (
        "%MYSQL%" -u root -e "SELECT 1" > "%OUT%" 2>&1
    )
    if errorlevel 1 (
        echo   FAIL - server not answering on localhost:3306.
        echo        Start MySQL in the XAMPP control panel first.
        set /a FAILS+=1
        echo   Raw output: >> "%LOGFILE%"
        type "%OUT%" >> "%LOGFILE%"
        if defined VERBOSE type "%OUT%"
    ) else (
        echo   OK - server answering on localhost:3306
        set /a PASS+=1
        type "%OUT%" >> "%LOGFILE%"
    )
)
echo.

echo [curl] >> "%LOGFILE%"
set CURLVER=
if defined CURL "%CURL%" --version > "%OUT%" 2>&1
if defined CURL for /f "tokens=2" %%v in ('type "%OUT%" ^| findstr /b curl') do set CURLVER=%%v
if defined CURL type "%OUT%" >> "%LOGFILE%"
if not defined CURL (
    echo   FAIL - curl.exe not found on PATH or System32. Ships with
    echo        Windows 10+, very odd. API checks need it.
    set /a FAILS+=1
) else (
    echo   OK - curl !CURLVER! at %CURL%
    set /a PASS+=1
)
echo.

echo [Port 8080] >> "%LOGFILE%"
set PORTPID=
netstat -ano | findstr ":8080.*LISTENING" > "%OUT%"
for /f "tokens=5" %%p in ('type "%OUT%"') do set PORTPID=%%p
if not defined PORTPID (
    echo   OK - port free, nothing stale listening
    set /a PASS+=1
) else (
    echo   NOTE - PID !PORTPID! already holds :8080.
    echo        If it is a stale app, run stop-app.bat before testing.
    type "%OUT%" >> "%LOGFILE%"
)
echo.

echo ============================================================
echo   passed: %PASS%   failed: %FAILS%
if %FAILS%==0 (
    echo   RESULT: all tools present. Move on to 02-check-databases.
) else (
    echo   RESULT: %FAILS% problem^(s^) above. Fix them first.
    echo   Full output saved to %LOGFILE%
)
echo ============================================================
if not defined NO_PAUSE pause
exit /b %FAILS%
