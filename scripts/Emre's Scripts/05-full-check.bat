@echo off
setlocal EnableDelayedExpansion
title PTEText check 05 - full project check
cd /d "%~dp0"
set NO_PAUSE=1
set OVERALL=0
set APP_STARTED=0
set R_ENV=SKIP
set R_DB=SKIP
set R_TEST=SKIP
set R_API=SKIP

echo ############################################################
echo   PTEText - full project check
echo   Sit back, this takes about a minute.
echo ############################################################
echo.

echo === stage 1/4: environment ===
call "01-check-requirements.bat" %~1
if errorlevel 1 (
    set R_ENV=FAIL
    echo.
    echo Environment is broken, no point continuing. Fix and rerun.
    goto :summary
)
set R_ENV=OK
echo.

echo === stage 2/4: databases ===
call "02-check-databases.bat" %~1
if errorlevel 1 (set R_DB=FAIL & set /a OVERALL+=1) else set R_DB=OK
echo.

echo === stage 3/4: unit tests ===
call "03-run-tests.bat" %~1
if errorlevel 1 (set R_TEST=FAIL & set /a OVERALL+=1) else set R_TEST=OK
echo.

echo === stage 4/4: live API ===
call :waitup
if errorlevel 1 (
    echo   App never came up on :8080 - skipping API checks.
    echo   Try scripts\run.bat yourself and watch the output.
    set R_API=FAIL
    set /a OVERALL+=1
) else (
    echo   App is up, running live checks.
    call "04-check-api.bat" %~1
    if errorlevel 1 (set R_API=FAIL & set /a OVERALL+=1) else set R_API=OK
)
echo.

:summary
echo ############################################################
echo   stage results
echo     environment : %R_ENV%
echo     databases   : %R_DB%
echo     unit tests  : %R_TEST%
echo     live API    : %R_API%
echo ############################################################
if %OVERALL%==0 if "%R_ENV%"=="OK" (
    echo   FINAL: everything passes. The project works.
) else (
    echo   FINAL: %OVERALL% stage^(s^) had failures.
    echo   Per-check logs are in scripts\Emre's Scripts\logs\
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

:waitup
curl -s --max-time 3 -o nul http://localhost:8080/api/health 2>nul
if not errorlevel 1 exit /b 0
echo App is not running - booting it in a separate window.
echo Waiting for it to come up ^(up to ~60s^)...
start "PTEText app" /min cmd /c "cd /d "%~dp0..\.." && mvn -q spring-boot:run"
set APP_STARTED=1
for /l %%i in (1,1,30) do (
    "%SystemRoot%\System32\timeout.exe" /t 2 /nobreak >nul
    curl -s --max-time 2 -o nul http://localhost:8080/api/health 2>nul
    if not errorlevel 1 exit /b 0
)
exit /b 1
