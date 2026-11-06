@echo off
setlocal EnableDelayedExpansion
set FAILS=0
set MYSQL=C:\xampp\mysql\bin\mysql.exe
set QOUT=%TEMP%\ptetext-q.txt

echo ============================================================
echo   PTEText - database check
echo ============================================================
echo.

if not exist "%MYSQL%" (
    echo mysql.exe not found at %MYSQL% - edit the path in this file.
    pause
    exit /b 1
)

"%MYSQL%" -u root -e "SELECT 1" >nul 2>&1
if errorlevel 1 (
    echo MySQL is not answering. Start it in the XAMPP panel first.
    pause
    exit /b 1
)

echo [Databases]
for %%d in (ptetext_users ptetext_chat ptetext_system) do (
    "%MYSQL%" -u root -N -e "SELECT COUNT(*) FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='%%d'" > "%QOUT%"
    set /p N=<"%QOUT%"
    if "!N!"=="1" (
        echo   OK - %%d exists
    ) else (
        echo   FAIL - %%d missing. Run scripts\setup-db.bat.
        set /a FAILS+=1
    )
)
echo.

echo [Tables]
call :checktable ptetext_users users
call :checktable ptetext_users contacts
call :checktable ptetext_users sessions
call :checktable ptetext_chat conversations
call :checktable ptetext_chat conversation_participants
call :checktable ptetext_chat messages
call :checktable ptetext_system activity_log
call :checktable ptetext_system attachments
echo.

echo [Stored procedures]
call :checkproc ptetext_users sp_register_user
call :checkproc ptetext_users sp_open_session
call :checkproc ptetext_users sp_cleanup_expired_sessions
call :checkproc ptetext_system sp_log_action
echo.

echo [Row counts]
call :count "SELECT COUNT(*) FROM ptetext_users.users"
echo   users: !N!
set USERCOUNT=!N!
call :count "SELECT COUNT(*) FROM ptetext_chat.messages"
echo   messages: !N!
call :count "SELECT COUNT(*) FROM ptetext_system.activity_log"
echo   activity_log: !N!
if !USERCOUNT! LSS 6 echo   ^(hint: seed has 6 demo users - fewer means someone wiped or never seeded^)
echo.

echo [Integrity]
call :count "SELECT COUNT(*) FROM ptetext_users.sessions s LEFT JOIN ptetext_users.users u ON s.user_id=u.user_id WHERE u.user_id IS NULL"
if !N!==0 (
    echo   OK - no sessions pointing at deleted users
) else (
    echo   FAIL - !N! orphan session^(s^) found
    set /a FAILS+=1
)
call :count "SELECT COUNT(*) FROM ptetext_chat.messages m LEFT JOIN ptetext_chat.conversations c ON m.conversation_id=c.conversation_id WHERE c.conversation_id IS NULL"
if !N!==0 (
    echo   OK - no messages in non-existent conversations
) else (
    echo   FAIL - !N! orphan message^(s^) found
    set /a FAILS+=1
)
call :count "SELECT COUNT(*) FROM ptetext_users.users WHERE LEFT(password_hash,7) <> 'bcrypt$'"
if !N!==0 (
    echo   OK - every password stored as bcrypt
) else (
    echo   NOTE - !N! user^(s^) still on a legacy hash ^(auto-upgrades on next login, fine^)
)
echo.

echo ============================================================
if %FAILS%==0 (
    echo   RESULT: databases are healthy. Move on to 03-run-tests.
) else (
    echo   RESULT: %FAILS% problem^(s^) above.
)
echo ============================================================
pause
exit /b %FAILS%

:count
"%MYSQL%" -u root -N -e "%~1" > "%QOUT%"
set /p N=<"%QOUT%"
exit /b 0

:checktable
call :count "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='%~1' AND TABLE_NAME='%~2'"
if !N!==1 (
    echo   OK - %~1.%~2
) else (
    echo   FAIL - %~1.%~2 missing
    set /a FAILS+=1
)
exit /b 0

:checkproc
call :count "SELECT COUNT(*) FROM information_schema.ROUTINES WHERE ROUTINE_SCHEMA='%~1' AND ROUTINE_NAME='%~2'"
if !N!==1 (
    echo   OK - %~2
) else (
    echo   FAIL - %~2 missing
    set /a FAILS+=1
)
exit /b 0
