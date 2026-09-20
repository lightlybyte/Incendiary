@echo off
setlocal EnableDelayedExpansion

REM ============================================================
REM  Config
REM ============================================================
set "JAVA_EXE=JRE\bin\java.exe"
set "VINEFLOWER_JAR=essentials\decompiler.jar"
set "THREAD_COUNT=8"
set "JAVA_MEM=4G"

set "CLIENT_URL=https://piston-data.mojang.com/v1/objects/2dc72797acbc1b63fc16a11c4ac393605f453754/client.jar"
set "SERVER_URL=https://piston-data.mojang.com/v1/objects/823e2250d24b3ddac457a60c92a6a941943fcd6a/server.jar"

set "CLIENT_JAR=client_26.2.jar"
set "SERVER_JAR=server_26.2.jar"
set "CLIENT_DIR=client-26.2"
set "SERVER_DIR=server-26.2"

REM ============================================================
REM  Dispatch
REM ============================================================
if "%~1"=="" goto :usage
if /I "%~1"=="downloadClientJar"  ( call :download client & goto :eof )
if /I "%~1"=="downloadServerJar"  ( call :download server & goto :eof )
if /I "%~1"=="extractClientJar"   ( call :extract  client & goto :eof )
if /I "%~1"=="extractServerJar"   ( call :extract  server & goto :eof )
if /I "%~1"=="decompileClientJar" ( call :decompile client & goto :eof )
if /I "%~1"=="decompileServerJar" ( call :decompile server & goto :eof )
if /I "%~1"=="getClient"          ( call :download client & call :extract client & call :decompile client & goto :eof )
if /I "%~1"=="getServer"          ( call :download server & call :extract server & call :decompile server & goto :eof )
if /I "%~1"=="getBoth"            (
    call :download client & call :extract client
    call :download server & call :extract server
    call :decompile client & call :decompile server
    goto :eof
)
echo [!] Unknown argument: %~1
goto :usage

:usage
echo Usage: %~nx0 ^<command^>
echo.
echo   downloadClientJar ^| downloadServerJar
echo   extractClientJar  ^| extractServerJar
echo   decompileClientJar ^| decompileServerJar
echo   getClient ^| getServer ^| getBoth
exit /b 1


REM ============================================================
REM  :download <kind>
REM ============================================================
:download
set "KIND=%~1"
if /I "%KIND%"=="client" ( set "URL=%CLIENT_URL%" & set "JAR=%CLIENT_JAR%" ) else ( set "URL=%SERVER_URL%" & set "JAR=%SERVER_JAR%" )

if exist "%JAR%" (
    echo [=] %JAR% already exists, skipping.
    exit /b 0
)

echo [*] Downloading %KIND%...
powershell -NoProfile -Command ^
    "$ProgressPreference='SilentlyContinue';" ^
    "Invoke-WebRequest -Uri '%URL%' -OutFile '%JAR%.part' -UseBasicParsing"
if errorlevel 1 (
    echo [!] Download failed.
    exit /b 1
)
move /Y "%JAR%.part" "%JAR%" >nul
echo [OK] Saved %JAR%
exit /b 0


REM ============================================================
REM  :extract <kind>
REM ============================================================
:extract
set "KIND=%~1"
if /I "%KIND%"=="client" ( set "JAR=%CLIENT_JAR%" & set "DIR=%CLIENT_DIR%" ) else ( set "JAR=%SERVER_JAR%" & set "DIR=%SERVER_DIR%" )

if not exist "%JAR%" (
    echo [!] %JAR% not found. Run download%KIND%Jar first.
    exit /b 1
)

echo [*] Extracting %JAR% -^> %DIR%\
if not exist "%DIR%" mkdir "%DIR%"

where 7z >nul 2>nul
if not errorlevel 1 (
    7z x -y -o"%DIR%" "%JAR%" >nul
    echo [OK] Extracted using 7z
    exit /b 0
)

if exist "%JAVA_EXE%" (
    pushd "%DIR%"
    "..\%JAVA_EXE%\..\jar.exe" xf "..\%JAR%"
    popd
    echo [OK] Extracted using jar
    exit /b 0
)

echo [!] No 7z or jar.exe found.
exit /b 1


REM ============================================================
REM  :decompile <kind>  -- zip output + native extract
REM ============================================================
:decompile
set "KIND=%~1"
if /I "%KIND%"=="client" ( set "DIR=%CLIENT_DIR%" ) else ( set "DIR=%SERVER_DIR%" )
set "SRC=%DIR%-src"
set "ZIP=%DIR%-src.zip"

if not exist "%DIR%"            ( echo [!] %DIR% not found. & exit /b 1 )
if not exist "%JAVA_EXE%"       ( echo [!] Java not found at %JAVA_EXE% & exit /b 1 )
if not exist "%VINEFLOWER_JAR%" ( echo [!] Vineflower not found at %VINEFLOWER_JAR% & exit /b 1 )

if exist "%SRC%" rmdir /S /Q "%SRC%" >nul
if exist "%ZIP%" del /Q "%ZIP%" >nul
mkdir "%SRC%"

set /a CLASS_COUNT=0
for /f %%C in ('dir /s /b "%DIR%\*.class" 2^>nul ^| find /c /v ""') do set CLASS_COUNT=%%C
echo [*] %CLASS_COUNT% classes -^> %ZIP%, %THREAD_COUNT% threads

REM --- Phase 1: Vineflower writes a zip in the background ---
start /B "" "%JAVA_EXE%" -Xmx%JAVA_MEM% -jar "%VINEFLOWER_JAR%" --folder --thread-count=%THREAD_COUNT% "%DIR%" "%ZIP%"

set /a LASTSIZE=-1
:waitloop
tasklist /FI "IMAGENAME eq java.exe" 2>nul | find /I "java.exe" >nul
if errorlevel 1 goto :zipdone

set /a NOWSIZE=0
if exist "%ZIP%" (
    for %%A in ("%ZIP%") do set /a NOWSIZE=%%~zA/1024
)
if !NOWSIZE! NEQ !LASTSIZE! (
    echo   writing zip: !NOWSIZE! KB
    set /a LASTSIZE=!NOWSIZE!
)
timeout /t 2 /nobreak >nul
goto :waitloop

:zipdone
if not exist "%ZIP%" (
    echo [!] Vineflower didn't produce %ZIP%
    exit /b 1
)

for %%A in ("%ZIP%") do set /a ZIP_MB=%%~zA/1048576
echo [OK] Zip written: %ZIP% ^(%ZIP_MB% MB^)

REM --- Phase 2: native extract ---
echo [*] Extracting %ZIP% -^> %SRC%\ ...

where 7z >nul 2>nul
if not errorlevel 1 (
    7z x -y -o"%SRC%" "%ZIP%" >nul
    echo [OK] Extracted with 7z
    goto :afterextract
)

if exist "%JAVA_EXE%" (
    pushd "%SRC%"
    "..\%JAVA_EXE%\..\jar.exe" xf "..\%ZIP%"
    popd
    echo [OK] Extracted with jar
    goto :afterextract
)

echo [!] Need 7z or jar.exe to extract.
exit /b 1

:afterextract
set /a AFTER=0
for /f %%C in ('dir /s /b "%SRC%\*.java" 2^>nul ^| find /c /v ""') do set AFTER=%%C
echo [OK] Decompiled %AFTER%/%CLASS_COUNT% classes into %SRC%\
exit /b 0