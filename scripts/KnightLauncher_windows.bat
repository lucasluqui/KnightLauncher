@echo off
setlocal

:: Starts Knight Launcher, looking for a Java to start it with in this order:
:: 1. The launcher's own Java runtime, in KnightLauncher\runtime.
:: 2. Any Java installed on this computer. The launcher downloads its own runtime if needed.
:: 3. The Java runtime the game ships with.

set "KL_DIR=%~dp0"
set "KL_JAVA="
set "KL_RUNTIME_DIR="

if exist "%KL_DIR%KnightLauncher\runtime.properties" (
  for /f "usebackq tokens=1,* delims==" %%a in ("%KL_DIR%KnightLauncher\runtime.properties") do (
    if "%%a"=="dir" set "KL_RUNTIME_DIR=%%b"
  )
)

if defined KL_RUNTIME_DIR if exist "%KL_DIR%KnightLauncher\runtime\%KL_RUNTIME_DIR%\bin\javaw.exe" set "KL_JAVA=%KL_DIR%KnightLauncher\runtime\%KL_RUNTIME_DIR%\bin\javaw.exe"

if not defined KL_JAVA (
  where javaw >nul 2>&1 && set "KL_JAVA=javaw"
)

if not defined KL_JAVA if exist "%KL_DIR%..\runtime\bin\javaw.exe" set "KL_JAVA=%KL_DIR%..\runtime\bin\javaw.exe"
if not defined KL_JAVA if exist "%KL_DIR%java_vm\bin\javaw.exe" set "KL_JAVA=%KL_DIR%java_vm\bin\javaw.exe"

if not defined KL_JAVA (
  echo Knight Launcher needs Java to start, but none was found.
  pause
  exit /b 1
)

cd /d "%KL_DIR%"
start "" "%KL_JAVA%" -jar "%KL_DIR%KnightLauncher.jar"
