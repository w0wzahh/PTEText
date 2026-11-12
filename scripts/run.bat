@echo off
REM Build + run the API (needs JDK 17+ and Maven on PATH)
REM Swagger UI:  http://localhost:8080/swagger-ui/index.html
cd /d "%~dp0.."
mvn spring-boot:run
