@echo off
:: by Crowfunder
:: my gh: https://github.com/Crowfunder
:: KnightLauncher gh: https://github.com/lucas-allegri/KnightLauncher

:: Checking if script was run inside Spiral Knights directory. If not, it asks for inputting game's main directory full path. Broken if somebody somehow has random "rsrc" and "scenes" folders in place where script is running. Too bad.
IF EXIST rsrc ( IF EXIST scenes ( SET gamepath="%cd%" ) ELSE ( SET /P gamepath=Please enter Spiral Knights' directory absolute path: ) )
CD "%gamepath%"

:: Simple asking for confirmation, just in case.
ECHO KnightLauncher will be installed/updated in this folder: %gamepath%
SET /P opt=Would you like to proceed? (Y/N) 
IF /I %opt% EQU y ( SET buff=true )
IF /I %opt% EQU yes ( SET buff=true )
IF /I %buff% NEQ true ( EXIT /b )

:: Checking if other versions are installed, if there are - remove them retaining "KnightLauncher.properties".
IF EXIST *KnightLauncher* ( 
    ECHO Detected other version installed, removing...
    IF EXIST KnightLauncher.properties ( REN KnightLauncher.properties move.properties)
    DEL *KnightLauncher*
    IF EXIST move.properties ( REN move.properties KnightLauncher.properties )
    ECHO Success!
)

:: Preparing installation info.
ECHO Downloading...
curl.exe -sSL https://api.github.com/repos/lucas-allegri/KnightLauncher/releases/latest | findstr browser_download_url > temp 
SET /P url=<temp
DEL temp
SET url=%url:"=%
SET url=%url:      browser_download_url: =%
SET filename=%url:https://github.com/lucas-allegri/KnightLauncher/releases/download/=%
ECHO %filename% > temp
(FOR /F "tokens=1,* delims=/" %%a IN (temp) DO ECHO %%b) > temp2
SET /P filename=<temp2
DEL temp
DEL temp2

:: Downloading and installing new version. NOTE: If "tar" fails - update to Windows 10, or update your Windows 10. Build 17063 at least.
curl.exe %url% -o %filename% -L
tar -xf %filename% 
DEL %filename%
ECHO Success!
