@echo off
rem shared detection - the other scripts CALL this, nobody runs it directly
rem sets: MYSQL, MYSQLADMIN, CURL, PROJECT_ROOT, LOGDIR, STAMP

if not defined MYSQL (
    for /f "delims=" %%m in ('where mysql.exe 2^>nul') do (
        if not defined MYSQL set "MYSQL=%%m"
    )
)
if not defined MYSQL (
    for %%d in (C:\xampp D:\xampp E:\xampp C:\wamp64) do (
        if not defined MYSQL if exist "%%d\mysql\bin\mysql.exe" set "MYSQL=%%d\mysql\bin\mysql.exe"
    )
)
if not defined MYSQL (
    for /d %%d in ("C:\Program Files\MySQL\MySQL Server*" "C:\Program Files\MariaDB*" "C:\mariadb*") do (
        if not defined MYSQL if exist "%%d\bin\mysql.exe" set "MYSQL=%%d\bin\mysql.exe"
    )
)
if not defined MYSQL (
    for /d %%d in ("C:\wamp64\bin\mysql\mysql*" "C:\wamp64\bin\mariadb\mariadb*") do (
        if not defined MYSQL if exist "%%d\bin\mysql.exe" set "MYSQL=%%d\bin\mysql.exe"
    )
)

if defined MYSQL (
    for %%m in ("%MYSQL%") do set "MYSQLADMIN=%%~dpmysqladmin.exe"
    if not exist "%MYSQLADMIN%" set "MYSQLADMIN="
)

if not defined CURL (
    for /f "delims=" %%c in ('where curl.exe 2^>nul') do (
        if not defined CURL set "CURL=%%c"
    )
)
if not defined CURL if exist "%SystemRoot%\System32\curl.exe" set "CURL=%SystemRoot%\System32\curl.exe"

set "PROJECT_ROOT=%~dp0..\.."
pushd "%PROJECT_ROOT%" >nul 2>&1
set "PROJECT_ROOT=%CD%"
popd >nul 2>&1

set "LOGDIR=%~dp0logs"
if not exist "%LOGDIR%" mkdir "%LOGDIR%" >nul 2>&1

for /f "delims=" %%t in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd-HHmmss" 2^>nul') do set "STAMP=%%t"
if not defined STAMP set "STAMP=%RANDOM%"
