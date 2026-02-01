@echo off
echo Starting Resume Screening Application...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH!
    echo Please install Java 17 or higher.
    pause
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Maven is not installed. Using alternative method...
    echo.
    echo Please install Maven or use VS Code Java Extension.
    echo.
    echo Quick Fix:
    echo 1. Open VS Code
    echo 2. Install "Java Extension Pack"
    echo 3. Open ResumeScreenerApplication.java
    echo 4. Click Run button
    pause
    exit /b 1
)

echo Building and running application...
echo.
call mvn clean spring-boot:run

pause
