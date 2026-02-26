@echo off
cd /d "%~dp0"

echo Installing frontend dependencies...
call npm install

echo.
echo Starting frontend server...
npx vite --host --port 9000
pause
