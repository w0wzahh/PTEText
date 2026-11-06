@echo off
setlocal
cd /d "%~dp0..\.."

echo ============================================================
echo   PTEText - unit tests
echo ============================================================
echo.

call mvn -B test > "%TEMP%\ptetext-test-log.txt" 2>&1
set MVNEXIT=%ERRORLEVEL%

findstr /c:"Tests run:" "%TEMP%\ptetext-test-log.txt"

echo.
echo ============================================================
if %MVNEXIT%==0 (
    echo   RESULT: suite green.
) else (
    echo   RESULT: suite FAILED - full log saved to
    echo   %TEMP%\ptetext-test-log.txt
    echo   Scroll up for the first failing test name.
)
echo ============================================================
pause
exit /b %MVNEXIT%
