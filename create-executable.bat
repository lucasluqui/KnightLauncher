:: Escalate cmd window to admin.
:: Credits: Ben Gripka @ https://stackoverflow.com/questions/1894967/how-to-request-administrator-access-inside-a-batch-file

:: Check for permissions
    IF "%PROCESSOR_ARCHITECTURE%" EQU "amd64" (
>nul 2>&1 "%SYSTEMROOT%\SysWOW64\cacls.exe" "%SYSTEMROOT%\SysWOW64\config\system"
) ELSE (
>nul 2>&1 "%SYSTEMROOT%\system32\cacls.exe" "%SYSTEMROOT%\system32\config\system"
)

:: If error flag set, we do not have admin.
if '%errorlevel%' NEQ '0' (
    echo Requesting administrative privileges...
    goto UACPrompt
) else ( goto gotAdmin )

:UACPrompt
    echo Set UAC = CreateObject^("Shell.Application"^) > "%temp%\getadmin.vbs"
    set params= %*
    echo UAC.ShellExecute "cmd.exe", "/c ""%~s0"" %params:"=""%", "", "runas", 1 >> "%temp%\getadmin.vbs"

    "%temp%\getadmin.vbs"
    del "%temp%\getadmin.vbs"
    exit /B

:gotAdmin
    pushd "%CD%"
    CD /D "%~dp0"

:: Name of the entry point file (without extension)
set ENTRY_POINT_NAME=run.py

:: Name of the distributable
set DIST_NAME=KnightLauncher

:: Activate virtual environment
call .\venv\Scripts\activate.bat

:: Create installer
pyinstaller %ENTRY_POINT_NAME% --onefile --noconsole --clean --name=%DIST_NAME% --icon=".\resources\image\icon-256.ico"

:: Debug
pause