@echo off
REM ============================================================
REM  PTEText test kit - step 1: environment check
REM  Verifies every tool the project needs is actually on this
REM  machine, with versions. Run this first, always.
REM ============================================================
setlocal EnableDelayedExpansion
set FAILS=0

echo ============================================================
echo   PTEText - environment check
echo ============================================================
echo.

REM --- Java: need JDK 17 or newer ------------------------------
echo [Java]
where java >nul 2>&1
if errorlevel 1 (
    echo   FAIL - java not on PATH. Install a JDK 17+ first.
    set /a FAILS+=1
) else (
    for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set JVER=%%~v
    echo   OK - found !JVER!
    REM Pull the major version out (first number before the dot) and check 17+
    for /f "delims=. tokens=1" %%m in ("!JVER!") do set JAVAMAJ=%%m
    if !JAVAMAJ! LSS 17 (
        echo   FAIL - Java !JAVAMAJ! is too old, the project needs 17+.
        set /a FAILS+=1
    ) else (
        echo   OK - major version !JAVAMAJ! ^>= 17
    )
)
echo.

REM --- Maven ----------------------------------------------------
echo [Maven]
where mvn >nul 2>&1
if errorlevel 1 (
    echo   FAIL - mvn not on PATH. Install Maven or use the wrapper.
    set /a FAILS+=1
) else (
    for /f "tokens=3" %%v in ('mvn -version 2^>^&1 ^| findstr /b /c:"Apache Maven"') do (
        echo   OK - Apache Maven %%v
    )
)
echo.

REM --- MySQL (XAMPP) --------------------------------------------
echo [MySQL]
set MYSQL=C:\xampp\mysql\bin\mysql.exe
if not exist "%MYSQL%" (
    echo   FAIL - mysql.exe not found at %MYSQL%
    echo        Either install XAMPP to C:\xampp or edit this path in
    echo        every script in this folder.
    set /a FAILS+=1
) else (
    "%MYSQL%" --version >nul 2>&1
    if errorlevel 1 (
        echo   FAIL - mysql.exe exists but will not run.
        set /a FAILS+=1
    ) else (
        for /f "delims=" %%v in ('"%MYSQL%" --version') do echo   OK - %%v
    )
)
echo.

REM --- Is the server itself answering? --------------------------
echo [MySQL server]
"%MYSQL%" -u root -e "SELECT 1" >nul 2>&1
if errorlevel 1 (
    echo   FAIL - mysql.exe exists but the SERVER is not answering.
    echo        Start MySQL in the XAMPP control panel first.
    set /a FAILS+=1
) else (
    echo   OK - server answering on localhost:3306
)
echo.

REM --- curl (for the API checks later) --------------------------
echo [curl]
where curl >nul 2>&1
if errorlevel 1 (
    echo   FAIL - curl not found. Ships with Windows 10+, very odd.
    echo        API checks will not work without it.
    set /a FAILS+=1
) else (
    echo   OK - curl available
)
echo.

REM --- Verdict --------------------------------------------------
echo ============================================================
if %FAILS%==0 (
    echo   RESULT: all tools present. Move on to 02-check-databases.
) else (
    echo   RESULT: %FAILS% problem^(s^) above. Fix them first.
)
echo ============================================================
exit /b %FAILS%
