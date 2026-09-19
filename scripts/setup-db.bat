@echo off
REM PTEText database setup — XAMPP's MySQL must be running, obviously.
REM Double-click, read the output, celebrate.
REM If XAMPP isn't in C:\xampp, edit the MYSQL path below.
setlocal

set MYSQL=C:\xampp\mysql\bin\mysql.exe
set SQLDIR=%~dp0..\sql

if not exist "%MYSQL%" (
    echo Could not find mysql.exe at %MYSQL%
    echo Either fix the path above or run the sql/ files in phpMyAdmin yourself.
    pause
    exit /b 1
)

echo Creating databases and tables...
"%MYSQL%" -u root < "%SQLDIR%\01_create_databases.sql" || goto :fail

echo Loading demo data...
"%MYSQL%" -u root < "%SQLDIR%\02_seed_data.sql" || goto :fail

echo.
echo Done! Databases: ptetext_users, ptetext_chat, ptetext_system
echo Demo logins: alice, bob, carol, dave, erin, frank  (password: password123)
pause
exit /b 0

:fail
echo.
echo Setup failed. Is MySQL actually running in the XAMPP control panel? :)
pause
exit /b 1
