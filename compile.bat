@echo off
REM Compile all Java source files for the bot

set CLASSPATH=.;..\lib\*

javac -cp %CLASSPATH% TankBot.java TargetLocator.java
if %ERRORLEVEL% neq 0 (
    echo Failed to compile.
    exit /b %ERRORLEVEL%
)

echo Compilation completed.
pause
