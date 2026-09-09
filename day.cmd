@echo off
setlocal
rem Batch files are not subject to PowerShell's execution policy, so this wrapper
rem works whatever the machine is configured to allow. Windows PowerShell 5.1 here
rem is set to AllSigned, which refuses day.ps1; PowerShell 7 is RemoteSigned and
rem runs it fine. Prefer 7, fall back to 5.1.
where pwsh >nul 2>&1
if %ERRORLEVEL%==0 (
    pwsh -NoProfile -ExecutionPolicy Bypass -File "%~dp0day.ps1" %*
) else (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0day.ps1" %*
)
exit /b %ERRORLEVEL%
