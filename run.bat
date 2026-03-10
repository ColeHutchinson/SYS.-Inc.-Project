@echo off
REM ============================================================
REM  Music Catalog — Build & Run Script (Windows)
REM  Requirements: Java 17+ and sqlite-jdbc JAR in .\lib\
REM ============================================================

set SQLITE_JAR=lib\sqlite-jdbc-3.45.1.0.jar
set SRC_DIR=src
set OUT_DIR=out
set MAIN_CLASS=com.musiccatalog.Main

REM Check Java
where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: javac not found. Please install JDK 17+ and add it to PATH.
    pause
    exit /b 1
)

REM Check for SQLite JAR
if not exist "%SQLITE_JAR%" (
    echo.
    echo SQLite JDBC driver not found at %SQLITE_JAR%
    echo.
    echo Please download it from:
    echo   https://github.com/xerial/sqlite-jdbc/releases/download/3.45.1.0/sqlite-jdbc-3.45.1.0.jar
    echo.
    echo Save it to the lib\ folder, then run this script again.
    pause
    exit /b 1
)

echo Compiling...
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

REM Collect all .java files
dir /s /b "%SRC_DIR%\*.java" > sources.txt

javac -cp "%SQLITE_JAR%" -d "%OUT_DIR%" @sources.txt
if %ERRORLEVEL% NEQ 0 (
    echo Compilation FAILED.
    del sources.txt
    pause
    exit /b 1
)
del sources.txt

echo Compilation successful!
echo Launching Music Catalog...
java -cp "%OUT_DIR%;%SQLITE_JAR%" %MAIN_CLASS%
