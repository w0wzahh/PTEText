@echo off
setlocal EnableDelayedExpansion
title PTEText check 03 - unit tests
if /i "%~1"=="verbose" set VERBOSE=1
call "%~dp0_detect.bat"
cd /d "%PROJECT_ROOT%"
set "LOGFILE=%LOGDIR%\03-tests-%STAMP%.log"

echo ============================================================
echo   PTEText - unit tests
echo   log: %LOGFILE%
echo ============================================================
echo.

where mvn >nul 2>&1
if errorlevel 1 (
    echo mvn not on PATH. Run 01-check-requirements first.
    if not defined NO_PAUSE pause
    exit /b 1
)

call mvn -B test > "%LOGFILE%" 2>&1
set MVNEXIT=%ERRORLEVEL%

findstr /c:"Tests run:" "%LOGFILE%"

echo.
if %MVNEXIT% NEQ 0 (
    echo [Failing tests]
    findstr /c:"FAILURE!" /c:"ERROR!" "%LOGFILE%"
    echo.
)
findstr /c:"Total time" "%LOGFILE%"

echo.
echo ============================================================
if %MVNEXIT%==0 (
    echo   RESULT: suite green.
) else (
    echo   RESULT: suite FAILED - look above for the failing test
    echo   names, full log saved to %LOGFILE%
)
echo ============================================================
if not defined NO_PAUSE pause
exit /b %MVNEXIT%
