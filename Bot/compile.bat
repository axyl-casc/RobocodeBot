@echo off
cd /d "%~dp0"

echo === Compiling Bot ===
set CLASSPATH=.;lib\*

javac --release 11 -cp %CLASSPATH% infinite\mind\*.java
if %ERRORLEVEL% neq 0 (
    echo ❌ Compilation failed.
    exit /b %ERRORLEVEL%
)

echo ✅ Compilation completed.

echo === Generating Javadocs ===
javadoc -d ..\docs\script-dir\javadoc -cp "lib/*" -sourcepath . infinite.mind
if %ERRORLEVEL% neq 0 (
    echo ❌ Javadoc generation failed. Ensure JDK is installed and configured in your PATH.
    exit /b %ERRORLEVEL%
)

echo ✅ Javadoc generated at docs/script-dir/javadoc
