@echo off
REM ==========================================================================
REM  PTEText - database setup (XAMPP on Windows)
REM  Creates the three databases and loads demo data.
REM  Edit the MYSQL path below if XAMPP is installed somewhere else.
REM ==========================================================================
setlocal

set MYSQL=C:\xampp\mysql\bin\mysql.exe
set SQLDIR=%~dp0..\sql

if not exist "%MYSQL%" (
    echo Could not find mysql.exe at %MYSQL%
    echo Edit this script or run the SQL files in phpMyAdmin instead.
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
echo Setup failed. Is MySQL running in the XAMPP control panel?
pause
exit /b 1
