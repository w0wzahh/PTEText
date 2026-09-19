@echo off
REM ==========================================================================
REM  PTEText - build and run (requires JDK 17+ and Maven on PATH)
REM ==========================================================================
cd /d "%~dp0.."
mvn -q compile exec:java
