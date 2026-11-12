@echo off
setlocal EnableDelayedExpansion
title PTEText check 04 - live API
if /i "%~1"=="verbose" set VERBOSE=1
set PASS=0
set FAILS=0
call "%~dp0_detect.bat"
set "LOGFILE=%LOGDIR%\04-api-%STAMP%.log"
set "OUT=%LOGDIR%\check.tmp"
set "API=http://localhost:8080"

echo ============================================================
echo   PTEText - live API check
echo   log: %LOGFILE%
echo ============================================================
echo.

curl -s --max-time 5 -o nul "%API%/api/health" > "%OUT%" 2>&1
if errorlevel 1 (
    echo Nothing answering on %API%.
    echo Start the app first - run scripts\run.bat, wait for
    echo "Started PteTextApplication", then run this again.
    type "%OUT%" >> "%LOGFILE%"
    if not defined NO_PAUSE pause
    exit /b 1
)
echo App answering on :8080.
echo.

echo [Health - three datasources] >> "%LOGFILE%"
curl -s --max-time 5 "%API%/api/health" > "%LOGDIR%\health.json" 2>"%OUT%"
type "%LOGDIR%\health.json" >> "%LOGFILE%"
for %%d in (ptetext_users ptetext_chat ptetext_system) do (
    findstr "%%d.*OK" "%LOGDIR%\health.json" >nul
    if errorlevel 1 (
        echo   FAIL - %%d not OK. Response:
        type "%LOGDIR%\health.json"
        set /a FAILS+=1
    ) else (
        echo   OK - %%d pool answering
        set /a PASS+=1
    )
)
echo.

echo [Login - ptetext_users] >> "%LOGFILE%"
curl -s --max-time 5 -X POST "%API%/api/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"alice\",\"password\":\"password123\"}" > "%LOGDIR%\login.json" 2>"%OUT%"
type "%LOGDIR%\login.json" >> "%LOGFILE%"
findstr sessionId "%LOGDIR%\login.json" >nul
if errorlevel 1 (
    echo   FAIL - login did not return a sessionId. Response:
    type "%LOGDIR%\login.json"
    set /a FAILS+=1
    goto :summary
)
for /f "tokens=2 delims=:," %%s in ('type "%LOGDIR%\login.json"') do set SID=%%~s
echo   OK - session issued: !SID:~0,8!...
set /a PASS+=1
echo.

curl -s --max-time 5 -X POST "%API%/api/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"alice\",\"password\":\"WRONG\"}" > "%LOGDIR%\badlogin.json" 2>"%OUT%"
type "%LOGDIR%\badlogin.json" >> "%LOGFILE%"
findstr sessionId "%LOGDIR%\badlogin.json" >nul
if errorlevel 1 (
    echo   OK - bad password rejected
    set /a PASS+=1
) else (
    echo   FAIL - wrong password logged in?! Check AuthService.
    set /a FAILS+=1
)
echo.

echo [Session gate] >> "%LOGFILE%"
curl -s --max-time 5 -o "%LOGDIR%\gate.json" -w "%%{http_code}" "%API%/api/chat?sessionId=definitely-fake" > "%OUT%" 2>nul
set /p GATECODE=<"%OUT%"
type "%LOGDIR%\gate.json" >> "%LOGFILE%"
if "!GATECODE!"=="401" (
    echo   OK - fake session rejected with 401
    set /a PASS+=1
) else (
    echo   FAIL - fake session got !GATECODE! instead of 401
    type "%LOGDIR%\gate.json"
    set /a FAILS+=1
)
curl -s --max-time 5 "%API%/api/auth/session/!SID!" > "%LOGDIR%\session.json" 2>nul
findstr "userId" "%LOGDIR%\session.json" >nul
if errorlevel 1 (
    echo   FAIL - /api/auth/session did not return a userId
    type "%LOGDIR%\session.json"
    set /a FAILS+=1
) else (
    echo   OK - session token resolves to a userId
    set /a PASS+=1
)
echo.

echo [Messaging - ptetext_chat] >> "%LOGFILE%"
if defined MYSQL (
    "%MYSQL%" -u root -N -e "SELECT COUNT(*) FROM ptetext_system.activity_log" > "%OUT%" 2>nul
    set /p LOGBEFORE=<"%OUT%"
)
if not defined LOGBEFORE set LOGBEFORE=0

curl -s --max-time 5 -X POST "%API%/api/chat/1/messages" -H "Content-Type: application/json" -d "{\"sessionId\":\"!SID!\",\"body\":\"automated test-kit message\"}" > "%LOGDIR%\send.json" 2>"%OUT%"
type "%LOGDIR%\send.json" >> "%LOGFILE%"
findstr messageId "%LOGDIR%\send.json" >nul
if errorlevel 1 (
    echo   FAIL - message send rejected. Response:
    type "%LOGDIR%\send.json"
    set /a FAILS+=1
) else (
    echo   OK - message sent into conversation 1
    set /a PASS+=1
)

curl -s --max-time 5 "%API%/api/chat/1/messages?sessionId=!SID!" > "%LOGDIR%\msgs.json" 2>"%OUT%"
type "%LOGDIR%\msgs.json" >> "%LOGFILE%"
findstr /c:"automated test-kit message" "%LOGDIR%\msgs.json" >nul
if errorlevel 1 (
    echo   FAIL - sent message not in the read list
    type "%LOGDIR%\msgs.json"
    set /a FAILS+=1
) else (
    echo   OK - message read back through the API
    set /a PASS+=1
)

curl -s --max-time 5 -X POST "%API%/api/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"erin\",\"password\":\"password123\"}" > "%LOGDIR%\erin.json" 2>nul
for /f "tokens=2 delims=:," %%s in ('type "%LOGDIR%\erin.json"') do set ESID=%%~s
curl -s --max-time 5 -X POST "%API%/api/chat/1/messages" -H "Content-Type: application/json" -d "{\"sessionId\":\"!ESID!\",\"body\":\"should not land\"}" > "%LOGDIR%\intruder.json" 2>nul
type "%LOGDIR%\intruder.json" >> "%LOGFILE%"
findstr /c:"messageId" "%LOGDIR%\intruder.json" >nul
if errorlevel 1 (
    echo   OK - non-member blocked from conv 1
    set /a PASS+=1
) else (
    echo   FAIL - erin is not in conv 1 but her message landed
    set /a FAILS+=1
)
echo.

echo [Read receipts] >> "%LOGFILE%"
curl -s --max-time 5 -o nul -w "%%{http_code}" -X POST "%API%/api/chat/1/read" -H "Content-Type: application/json" -d "{\"sessionId\":\"!SID!\"}" > "%OUT%" 2>nul
set /p READCODE=<"%OUT%"
if "!READCODE!"=="204" (
    echo   OK - mark-read returned 204
    set /a PASS+=1
) else (
    echo   FAIL - mark-read returned !READCODE!
    set /a FAILS+=1
)
curl -s --max-time 5 "%API%/api/chat?sessionId=!SID!" > "%LOGDIR%\convs.json" 2>nul
type "%LOGDIR%\convs.json" >> "%LOGFILE%"
findstr "unreadCount.:0" "%LOGDIR%\convs.json" >nul
if errorlevel 1 (
    echo   FAIL - no conversation shows unreadCount 0 after mark-read
    type "%LOGDIR%\convs.json"
    set /a FAILS+=1
) else (
    echo   OK - conversation list shows unreadCount
    set /a PASS+=1
)
echo.

echo [Audit - ptetext_system] >> "%LOGFILE%"
if defined MYSQL (
    "%MYSQL%" -u root -N -e "SELECT COUNT(*) FROM ptetext_system.activity_log" > "%OUT%" 2>nul
    set /p LOGAFTER=<"%OUT%"
    if not defined LOGAFTER set LOGAFTER=0
    set /a DELTA=LOGAFTER-LOGBEFORE
    if !DELTA! GEQ 1 (
        echo   OK - activity_log grew by !DELTA! row^(s^) during the test
        set /a PASS+=1
    ) else (
        echo   FAIL - MESSAGE_SENT never hit ptetext_system.activity_log
        set /a FAILS+=1
    )
) else (
    echo   SKIP - mysql.exe not found, audit delta not checked
)
echo.

curl -s --max-time 5 -o nul -w "%%{http_code}" -X POST "%API%/api/auth/logout" -H "Content-Type: application/json" -d "{\"sessionId\":\"!SID!\"}" > "%OUT%" 2>nul
set /p OUTCODE=<"%OUT%"
if "!OUTCODE!"=="204" (
    echo   Logout OK.
) else (
    echo   Logout returned !OUTCODE! ^(minor^).
)
echo.

:summary
echo ============================================================
echo   passed: %PASS%   failed: %FAILS%
if %FAILS%==0 (
    echo   RESULT: full API flow works end to end.
) else (
    echo   RESULT: %FAILS% check^(s^) failed above.
    echo   Every raw response is saved in %LOGDIR%
)
echo ============================================================
if not defined NO_PAUSE pause
exit /b %FAILS%
