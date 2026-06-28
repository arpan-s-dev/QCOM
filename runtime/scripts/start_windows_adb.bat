@echo off
REM Start Windows adb so WSL USB bridge works. Run once per boot (or after reboot).
set PT=%~dp0..\..\platform-tools\adb.exe
if not exist "%PT%" set PT=%~dp0..\..\..\platform-tools\adb.exe
"%PT%" kill-server
start "" /B "%PT%" -a -P 5037 server nodaemon
timeout /t 2 /nobreak >nul
"%PT%" devices
