@echo off
cd /d "%~dp0"

set CLASSPATH=.;lib\*

javac --release 11 -cp %CLASSPATH% infinite\mind\*.java
if %ERRORLEVEL% neq 0 (
    echo Compilation failed.
    exit /b %ERRORLEVEL%
)

rem Create manifest with main class and classpath to the API jar
set MANIFEST=manifest.txt
echo Main-Class: infinite.mind.TankBotLauncher> %MANIFEST%
echo Class-Path: lib/robocode-tankroyale-bot-api-0.30.2.jar>> %MANIFEST%

jar cfm TankBot.jar %MANIFEST% infinite\mind\*.class TankBot.json
if %ERRORLEVEL% neq 0 (
    echo Jar packaging failed.
    del %MANIFEST%
    exit /b %ERRORLEVEL%
)

del %MANIFEST%

echo TankBot.jar created.
