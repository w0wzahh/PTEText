@echo off
REM Build + run the API (needs JDK 17+ and Maven on PATH)
REM Swagger UI:  http://localhost:8080/swagger-ui/index.html
cd /d "%~dp0.."

netstat -ano | findstr ":8080.*LISTENING" >nul
if not errorlevel 1 (
    echo [run] Port 8080 already has a listener - the server is probably already running.
    echo [run] Stop it first, or just use it: http://localhost:8080/swagger-ui/index.html
    exit /b 0
)

netstat -ano | findstr ":3306.*LISTENING" >nul
if errorlevel 1 (
    echo [run] WARNING: nothing is listening on port 3306 - start MySQL in XAMPP first.
    echo [run] The server will fail its database checks without it.
    timeout /t 4 >nul
)

mvn spring-boot:run
