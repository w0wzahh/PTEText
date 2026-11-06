@echo off
REM ============================================================
REM  PTEText test kit - the big red button
REM  Runs everything in order:
REM    1. environment check
REM    2. database check
REM    3. unit tests
REM    4. live API check (boots the app itself if needed)
REM  One answer at the end: does the whole project work or not.
REM ============================================================
setlocal EnableDelayedExpansion
cd /d "%~dp0"
set OVERALL=0
set APP_STARTED=0

echo ############################################################
echo   PTEText - full project check
echo   Sit back, this takes about a minute.
echo ############################################################
echo.

REM ==== 1. Environment ==========================================
call "01-check-requirements.bat"
if errorlevel 1 (
    echo.
    echo Environment is broken, no point continuing. Fix and rerun.
    pause
    exit /b 1
)
echo.

REM ==== 2. Databases ============================================
call "02-check-databases.bat"
if errorlevel 1 set /a OVERALL+=1
echo.

REM ==== 3. Unit tests ===========================================
call "03-run-tests.bat"
if errorlevel 1 set /a OVERALL+=1
echo.

REM ==== 4. Live API =============================================
curl -s --max-time 3 -o nul http://localhost:8080/api/health 2>nul
if errorlevel 1 (
    echo App is not running - booting it in a separate window.
    echo Waiting for it to come up ^(up to ~60s^)...
    start "PTEText app" /min cmd /c "cd /d "%~dp0..\.." && mvn -q spring-boot:run"
    set APP_STARTED=1
    set APPUP=0
    for /l %%i in (1,1,30) do (
        if !APPUP!==0 (
            timeout /t 2 /nobreak >nul
            curl -s --max-time 2 -o nul http://localhost:8080/api/health 2>nul
            if not errorlevel 1 set APPUP=1
        )
    )
)
curl -s --max-time 3 -o nul http://localhost:8080/api/health 2>nul
if errorlevel 1 (
    echo   App never came up on :8080 - skipping API checks.
    echo   Try scripts\run.bat yourself and watch the output.
    set /a OVERALL+=1
) else (
    echo   App is up, running live checks.
    call "04-check-api.bat"
    if errorlevel 1 set /a OVERALL+=1
)
echo.

REM ==== Final verdict ===========================================
echo ############################################################
if %OVERALL%==0 (
    echo   FINAL: everything passes. The project works.
) else (
    echo   FINAL: %OVERALL% stage^(s^) had failures. Scroll up.
)
echo ############################################################
if %APP_STARTED%==1 (
    echo.
    echo NOTE: the app was started for this test and is still
    echo running in its own window. Use stop-app.bat to kill it,
    echo or just close that window.
)
pause
exit /b %OVERALL%
