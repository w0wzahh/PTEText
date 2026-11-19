@echo off
REM Make sure the PTEText server is running. If it isn't, this starts it in
REM its own window (via scripts\run.bat) and waits for it to come up.
REM Safe to run any number of times - it does nothing if the server is up.
cd /d "%~dp0.."
call mvn -q compile
if errorlevel 1 (
    echo [dev-server] Compile failed - fix the errors above first.
    exit /b 1
)
java -cp target\classes com.ptetext.client.DevServer
