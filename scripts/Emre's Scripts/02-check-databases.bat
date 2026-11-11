@echo off
setlocal EnableDelayedExpansion
title PTEText check 02 - databases
if /i "%~1"=="verbose" set VERBOSE=1
set PASS=0
set FAILS=0
call "%~dp0_detect.bat"
set "LOGFILE=%LOGDIR%\02-databases-%STAMP%.log"
set "OUT=%LOGDIR%\check.tmp"
set "QOUT=%LOGDIR%\q.tmp"

echo ============================================================
echo   PTEText - database check
echo   log: %LOGFILE%
echo ============================================================
echo.

if not defined MYSQL (
    echo mysql.exe not found anywhere. Run 01-check-requirements
    echo first - it lists everywhere we looked.
    if not defined NO_PAUSE pause
    exit /b 1
)
echo using %MYSQL%
echo using %MYSQL% >> "%LOGFILE%"
echo.

"%MYSQL%" -u root -e "SELECT 1" > "%OUT%" 2>&1
if errorlevel 1 (
    echo MySQL is not answering. Start it in the XAMPP panel first.
    echo The error was: >> "%LOGFILE%"
    type "%OUT%" >> "%LOGFILE%"
    type "%OUT%"
    if not defined NO_PAUSE pause
    exit /b 1
)

echo [Databases] >> "%LOGFILE%"
for %%d in (ptetext_users ptetext_chat ptetext_system) do (
    "%MYSQL%" -u root -N -e "SELECT COUNT(*) FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='%%d'" > "%QOUT%" 2>"%OUT%"
    set /p N=<"%QOUT%"
    if "!N!"=="1" (
        echo   OK - %%d exists
        set /a PASS+=1
    ) else (
        echo   FAIL - %%d missing. Run scripts\setup-db.bat.
        set /a FAILS+=1
        echo %%d missing >> "%LOGFILE%"
        type "%OUT%" >> "%LOGFILE%"
    )
)
echo.

echo [Tables] >> "%LOGFILE%"
call :checktable ptetext_users users
call :checktable ptetext_users contacts
call :checktable ptetext_users sessions
call :checktable ptetext_chat conversations
call :checktable ptetext_chat conversation_participants
call :checktable ptetext_chat messages
call :checktable ptetext_system activity_log
call :checktable ptetext_system attachments
echo.

echo [Stored procedures] >> "%LOGFILE%"
call :checkproc ptetext_users sp_register_user
call :checkproc ptetext_users sp_open_session
call :checkproc ptetext_users sp_cleanup_expired_sessions
call :checkproc ptetext_system sp_log_action
echo.

echo [Row counts] >> "%LOGFILE%"
call :count "SELECT COUNT(*) FROM ptetext_users.users"
echo   users: !N!
set USERCOUNT=!N!
call :count "SELECT COUNT(*) FROM ptetext_users.contacts"
echo   contacts: !N!
call :count "SELECT COUNT(*) FROM ptetext_users.sessions"
echo   sessions: !N!
call :count "SELECT COUNT(*) FROM ptetext_chat.messages"
echo   messages: !N!
call :count "SELECT COUNT(*) FROM ptetext_system.activity_log"
echo   activity_log: !N!
call :count "SELECT COUNT(*) FROM ptetext_system.attachments"
echo   attachments: !N!
if !USERCOUNT! LSS 6 echo   ^(hint: seed has 6 demo users - fewer means someone wiped or never seeded^)
echo.

echo [Integrity] >> "%LOGFILE%"
call :count "SELECT COUNT(*) FROM ptetext_users.sessions s LEFT JOIN ptetext_users.users u ON s.user_id=u.user_id WHERE u.user_id IS NULL"
if !N!==0 (
    echo   OK - no sessions pointing at deleted users
    set /a PASS+=1
) else (
    echo   FAIL - !N! orphan session^(s^) found
    set /a FAILS+=1
)
call :count "SELECT COUNT(*) FROM ptetext_chat.messages m LEFT JOIN ptetext_chat.conversations c ON m.conversation_id=c.conversation_id WHERE c.conversation_id IS NULL"
if !N!==0 (
    echo   OK - no messages in non-existent conversations
    set /a PASS+=1
) else (
    echo   FAIL - !N! orphan message^(s^) found
    set /a FAILS+=1
)
call :count "SELECT COUNT(*) FROM ptetext_system.attachments a LEFT JOIN ptetext_chat.messages m ON a.message_id=m.message_id WHERE m.message_id IS NULL"
if !N!==0 (
    echo   OK - no attachments pointing at missing messages ^(cross-DB check^)
    set /a PASS+=1
) else (
    echo   FAIL - !N! attachment^(s^) reference messages that do not exist
    set /a FAILS+=1
)
call :count "SELECT COUNT(*) FROM ptetext_users.users WHERE LEFT(password_hash,7) = 'bcrypt$'"
if !N!==!USERCOUNT! (
    echo   OK - every password stored as bcrypt
    set /a PASS+=1
) else (
    set /a LEGACY=USERCOUNT-N
    echo   NOTE - !LEGACY! user^(s^) still on a legacy hash ^(auto-upgrades on next login, fine^)
)
echo.

echo ============================================================
echo   passed: %PASS%   failed: %FAILS%
if %FAILS%==0 (
    echo   RESULT: databases are healthy. Move on to 03-run-tests.
) else (
    echo   RESULT: %FAILS% problem^(s^) above.
    echo   Raw query errors ^(if any^) are in %LOGFILE%
)
echo ============================================================
if not defined NO_PAUSE pause
exit /b %FAILS%

:count
"%MYSQL%" -u root -N -e "%~1" 2> "%OUT%" > "%QOUT%"
set /p N=<"%QOUT%"
if not defined N set N=ERR
echo %~1 = !N! >> "%LOGFILE%"
type "%OUT%" >> "%LOGFILE%"
exit /b 0

:checktable
call :count "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='%~1' AND TABLE_NAME='%~2'"
if !N!==1 (
    echo   OK - %~1.%~2
    set /a PASS+=1
) else (
    echo   FAIL - %~1.%~2 missing
    echo        fix: run scripts\setup-db.bat
    set /a FAILS+=1
)
exit /b 0

:checkproc
call :count "SELECT COUNT(*) FROM information_schema.ROUTINES WHERE ROUTINE_SCHEMA='%~1' AND ROUTINE_NAME='%~2'"
if !N!==1 (
    echo   OK - %~2
    set /a PASS+=1
) else (
    echo   FAIL - %~2 missing
    echo        fix: rerun sql\05_model_fixes_and_procedures.sql ^(or the newest sql file^)
    set /a FAILS+=1
)
exit /b 0
