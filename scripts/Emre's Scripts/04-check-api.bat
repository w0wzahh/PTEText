@echo off
setlocal EnableDelayedExpansion
set FAILS=0
set MYSQL=C:\xampp\mysql\bin\mysql.exe
set API=http://localhost:8080

echo ============================================================
echo   PTEText - live API check
echo ============================================================
echo.

curl -s --max-time 5 -o nul "%API%/api/health" 2>nul
if errorlevel 1 (
    echo Nothing answering on %API%.
    echo Start the app first - run scripts\run.bat, wait for
    echo "Started PteTextApplication", then run this again.
    pause
    exit /b 1
)
echo App answering on :8080.
echo.

echo [Health - three datasources]
curl -s --max-time 5 "%API%/api/health" > "%TEMP%\ptetext-health.json"
for %%d in (ptetext_users ptetext_chat ptetext_system) do (
    findstr "%%d.*OK" "%TEMP%\ptetext-health.json" >nul
    if errorlevel 1 (
        echo   FAIL - %%d not OK. Response:
        type "%TEMP%\ptetext-health.json"
        set /a FAILS+=1
    ) else (
        echo   OK - %%d pool answering
    )
)
echo.

echo [Login - ptetext_users]
curl -s --max-time 5 -X POST "%API%/api/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"alice\",\"password\":\"password123\"}" > "%TEMP%\ptetext-login.json"
findstr sessionId "%TEMP%\ptetext-login.json" >nul
if errorlevel 1 (
    echo   FAIL - login did not return a sessionId. Response:
    type "%TEMP%\ptetext-login.json"
    set /a FAILS+=1
    goto :summary
)
for /f "tokens=2 delims=:," %%s in ('type "%TEMP%\ptetext-login.json"') do set SID=%%~s
echo   OK - session issued: !SID:~0,8!...
echo.

curl -s --max-time 5 -X POST "%API%/api/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"alice\",\"password\":\"WRONG\"}" > "%TEMP%\ptetext-badlogin.json"
findstr sessionId "%TEMP%\ptetext-badlogin.json" >nul
if errorlevel 1 (
    echo   OK - bad password rejected
) else (
    echo   FAIL - wrong password logged in?! Check AuthService.
    set /a FAILS+=1
)
echo.

echo [Messaging - ptetext_chat]
"%MYSQL%" -u root -N -e "SELECT COUNT(*) FROM ptetext_system.activity_log" > "%TEMP%\ptetext-n.txt"
set /p LOGBEFORE=<"%TEMP%\ptetext-n.txt"

curl -s --max-time 5 -X POST "%API%/api/chat/1/messages" -H "Content-Type: application/json" -d "{\"sessionId\":\"!SID!\",\"body\":\"automated test-kit message\"}" > "%TEMP%\ptetext-send.json"
findstr messageId "%TEMP%\ptetext-send.json" >nul
if errorlevel 1 (
    echo   FAIL - message send rejected. Response:
    type "%TEMP%\ptetext-send.json"
    set /a FAILS+=1
) else (
    echo   OK - message sent into conversation 1
)

curl -s --max-time 5 "%API%/api/chat/1/messages?sessionId=!SID!" > "%TEMP%\ptetext-msgs.json"
findstr /c:"automated test-kit message" "%TEMP%\ptetext-msgs.json" >nul
if errorlevel 1 (
    echo   FAIL - sent message not in the read list
    set /a FAILS+=1
) else (
    echo   OK - message read back through the API
)

curl -s --max-time 5 -X POST "%API%/api/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"erin\",\"password\":\"password123\"}" > "%TEMP%\ptetext-erin.json"
for /f "tokens=2 delims=:," %%s in ('type "%TEMP%\ptetext-erin.json"') do set ESID=%%~s
curl -s --max-time 5 -X POST "%API%/api/chat/1/messages" -H "Content-Type: application/json" -d "{\"sessionId\":\"!ESID!\",\"body\":\"should not land\"}" > "%TEMP%\ptetext-intruder.json"
findstr /c:"messageId" "%TEMP%\ptetext-intruder.json" >nul
if errorlevel 1 (
    echo   OK - non-member blocked from conv 1
) else (
    echo   FAIL - erin is not in conv 1 but her message landed
    set /a FAILS+=1
)
echo.

echo [Read receipts]
curl -s --max-time 5 -o nul -w "%%{http_code}" -X POST "%API%/api/chat/1/read" -H "Content-Type: application/json" -d "{\"sessionId\":\"!SID!\"}" > "%TEMP%\ptetext-read.txt"
set /p READCODE=<"%TEMP%\ptetext-read.txt"
if "!READCODE!"=="204" (
    echo   OK - mark-read returned 204
) else (
    echo   FAIL - mark-read returned !READCODE!
    set /a FAILS+=1
)
curl -s --max-time 5 "%API%/api/chat?sessionId=!SID!" > "%TEMP%\ptetext-convs.json"
findstr "unreadCount.:0" "%TEMP%\ptetext-convs.json" >nul
if errorlevel 1 (
    echo   FAIL - no conversation shows unreadCount 0 after mark-read
    set /a FAILS+=1
) else (
    echo   OK - conversation list shows unreadCount
)
echo.

echo [Audit - ptetext_system]
"%MYSQL%" -u root -N -e "SELECT COUNT(*) FROM ptetext_system.activity_log" > "%TEMP%\ptetext-n.txt"
set /p LOGAFTER=<"%TEMP%\ptetext-n.txt"
set /a DELTA=LOGAFTER-LOGBEFORE
if !DELTA! GEQ 1 (
    echo   OK - activity_log grew by !DELTA! row^(s^) during the test
) else (
    echo   FAIL - MESSAGE_SENT never hit ptetext_system.activity_log
    set /a FAILS+=1
)
echo.

curl -s --max-time 5 -o nul -w "%%{http_code}" -X POST "%API%/api/auth/logout" -H "Content-Type: application/json" -d "{\"sessionId\":\"!SID!\"}" > "%TEMP%\ptetext-out.txt"
set /p OUTCODE=<"%TEMP%\ptetext-out.txt"
if "!OUTCODE!"=="204" (
    echo   Logout OK.
) else (
    echo   Logout returned !OUTCODE! ^(minor^).
)
echo.

:summary
echo ============================================================
if %FAILS%==0 (
    echo   RESULT: full API flow works end to end.
) else (
    echo   RESULT: %FAILS% check^(s^) failed above.
)
echo ============================================================
pause
exit /b %FAILS%
