@echo off
chcp 65001 > nul

echo.
echo ==========================================
echo   🛑 YA-PRJ 서비스 종료
echo ==========================================
echo.

:: Backend 종료 (uvicorn 프로세스)
echo Backend 종료 중...
taskkill /f /im uvicorn.exe 2>nul
taskkill /f /fi "WINDOWTITLE eq YA-PRJ Backend*" 2>nul

:: Frontend 종료 (node 프로세스)
echo Frontend 종료 중...
taskkill /f /fi "WINDOWTITLE eq YA-PRJ Frontend*" 2>nul

:: Docker 컨테이너 종료
echo Docker 컨테이너 종료 중...
cd /d "%~dp0"
docker-compose down

echo.
echo ✅ 모든 서비스가 종료되었습니다.
echo.
pause
