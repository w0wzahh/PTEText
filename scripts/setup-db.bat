@echo off
REM PTEText database setup - MySQL must be running (start it in XAMPP first).
REM If mysql.exe lives somewhere unusual, set a MYSQL env var to its full path.
setlocal

call "%~dp0Emre's Scripts\_detect.bat"
set SQLDIR=%~dp0..\sql

if not defined MYSQL (
    echo Could not find mysql.exe anywhere the kit looks:
    echo   PATH, C:\xampp, D:\xampp, E:\xampp, wamp64,
    echo   Program Files\MySQL, Program Files\MariaDB
    echo Set a MYSQL env var with the full path, or run the sql\ files
    echo in phpMyAdmin yourself instead.
    pause
    exit /b 1
)
echo using %MYSQL%
echo.

"%MYSQL%" -u root -e "SELECT 1" >nul 2>&1
if errorlevel 1 (
    echo MySQL is not answering - start it in the XAMPP control panel first.
    pause
    exit /b 1
)

for %%f in ("%SQLDIR%\0*.sql") do (
    echo Running %%~nxf ...
    "%MYSQL%" -u root < "%%f" || goto :fail
)

echo.
echo Done! Databases: ptetext_users, ptetext_chat, ptetext_system
echo Demo logins: alice, bob, carol, dave, erin, frank  (password: password123)
pause
exit /b 0

:fail
echo.
echo Setup failed partway through. The error is above - and if some
echo databases already exist, that is fine, just fix the error and rerun.
pause
exit /b 1
