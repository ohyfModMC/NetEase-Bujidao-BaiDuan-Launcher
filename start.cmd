@echo off
chcp 65001 >nul
set "LESSCHARSET=utf-8"
setlocal

REM ================================================================
REM  BuJiDao launcher (NetEase -> international)
REM  - starts the 127.0.0.1:9876 white-end proxy (bypasses the
REM    NetEase monitor / "Close Game" gate)
REM  - launches the game with all JVM args from authproxy\gameargs.txt
REM  - every log line goes to the console window AND to launcher.log:
REM      . launcher lifecycle via the :log routine
REM      . the full game startup output via PowerShell Tee-Object
REM  - the game's own full log is also in .minecraft\logs\latest.log
REM  Log: G:\Game\FurryNEL_Latest\FurryNEL_Pack_PCL2HMCL\launcher.log
REM ================================================================

dir /a-d /b "%CD%\Java" 2>nul | findstr . >nul
if %errorlevel% equ 0 (
    echo
) else (
    mkdir Java 2>nul
    bitsadmin /transfer Java17 /download /priority normal "https://download.oracle.com/java/17/archive/jdk-17.0.12_windows-x64_bin.zip" "%CD%\Java\jdk-17.0.12_windows-x64_bin.zip"
    tar -xf %CD%\Java\jdk-17.0.12_windows-x64_bin.zip -C %CD%\Java\
    xcopy /E /Y "%CD%\Java\jdk-17.0.12\*" "%CD%\Java"
    rmdir /S /Q "%CD%\Java\jdk-17.0.12"
)
cls

set "ROOT=%CD%"
set "GAMEDIR=%ROOT%\.minecraft"
set "LOG=%ROOT%\launcher.log"
set "PROXYLOG=%ROOT%\authproxy\proxy.log"
set "JAVA=%ROOT%\Java\bin\java.exe"
set "JAVAC=%ROOT%\Java\bin\javac.exe"
set "PROXY_DIR=%ROOT%\authproxy"
set "PID_FILE=%PROXY_DIR%\proxy.pid"

REM ---------- step -: start the proxy. ----------
cd server_proxy
start fantnelcli.exe --skinsDir=%PROXY_DIR%\skins_local

if exist "%LOG%" del "%LOG%"
call :log === BuJiDao launcher start ===
call :log gameDir = %GAMEDIR%
cd /d "%GAMEDIR%"

REM ---------- step 0: refuse to start if a game/proxy session is already active ----------
REM    port 9876 = fantelcli's SkinSocketServer (replaces old NetEaseProxy)
netstat -ano | findstr ":9876 " >nul
if not errorlevel 1 goto portbusy
goto waitsocket

:portbusy
echo [%time%] ABORT: port 9876 already in use - another game session is active
echo [%time%] close the running fantelcli window first, then run start.cmd again
exit /b 2

REM ---------- step 1: wait for fantelcli's SkinSocketServer on 127.0.0.1:9876 ----------
REM    fantelcli binds 9876 before login; we wait until it's listening, then launch the game.
:waitsocket
call :log waiting for fantelcli SkinSocketServer on 127.0.0.1:9876
set /a WAIT=0
:waitloop
netstat -ano | findstr ":9876 " >nul
if not errorlevel 1 goto socketup
set /a WAIT+=1
if %WAIT% GEQ 60 goto nosocket
ping -n 2 127.0.0.1 >nul
goto waitloop
:socketup
call :log SkinSocketServer is up on 127.0.0.1:9876
goto rungame
:nosocket
call :log WARNING: SkinSocketServer not ready after 60s - game may be blocked by monitor

REM ---------- step 2: launch the game (output to console AND launcher.log) ----------
REM   FIX: Force PowerShell to use UTF-8 for both input and output
:rungame
call :log launching game ...
powershell -NoProfile -Command "[Console]::OutputEncoding = [Text.UTF8Encoding]::new(); [Console]::InputEncoding = [Text.UTF8Encoding]::new(); $sw = New-Object System.IO.StreamWriter('%LOG%', $true, [Text.UTF8Encoding]::new()); try { & '%JAVA%' '-Druntime_path=%GAMEDIR%\versions\1.20.1\natives\runtime' '@%PROXY_DIR%\gameargs.txt' 2>&1 | ForEach-Object { $_; $sw.WriteLine([string]$_) } } finally { $sw.Close() }; exit $LASTEXITCODE"
set "EXITCODE=%errorlevel%"
call :log game exited, exit code = %EXITCODE%

REM ---------- step 3: cleanup ----------
REM    fantelcli runs in its own window; user closes it with q+Enter or Ctrl+C.
:done
call :log === launcher done ===
endlocal & exit /b %EXITCODE%

REM ---------- log helper: print to console AND append to launcher.log ----------
:log
set "TS=%time%"
echo [%TS%] %*
echo [%TS%] %* >> "%LOG%"
exit /b 0