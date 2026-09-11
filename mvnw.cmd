@echo off
setlocal

set WRAPPER_PS1=%~dp0.mvn\wrapper\maven-wrapper.ps1

if not exist "%WRAPPER_PS1%" (
  echo Maven wrapper script not found: %WRAPPER_PS1%
  exit /b 1
)

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%WRAPPER_PS1%" %*
exit /b %ERRORLEVEL%
