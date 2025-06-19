@echo off
cd /d "%~dp0"

rem Load environment variables from .env if it exists
if exist "%~dp0\.env" (
    for /f "usebackq tokens=1,* delims==" %%i in ("%~dp0\.env") do set %%i=%%j
)

if "%SERVER_URL%"=="" set SERVER_URL=ws://localhost:7653
if "%SERVER_SECRET%"=="" set SERVER_SECRET=VizYXf24+eMu2SNGCdiQQ1StNFyWEkmi8qGpYycMR/

java -cp "lib/*;." infinite.mind.TankBot
