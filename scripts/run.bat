@echo off
REM Build + run in one step (needs JDK 17+ and Maven on PATH)
cd /d "%~dp0.."
mvn -q compile exec:java
