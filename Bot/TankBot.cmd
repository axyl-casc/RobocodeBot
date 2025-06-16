@echo off
rem Launch TankBot using robocode tank royale API

rem Load environment variables from .env if it exists
if exist "%~dp0\.env" (
    for /f "usebackq tokens=1,* delims==" %%i in ("%~dp0\.env") do set %%i=%%j
)

if "%SERVER_URL%"=="" set SERVER_URL=ws://localhost:7654
if "%SERVER_SECRET%"=="" set SERVER_SECRET=SUMu9ukWcUUteXLidcLHZOFZWg+R2u9VGUb7bVaA3K

java -cp "lib/*;src" TankBot
