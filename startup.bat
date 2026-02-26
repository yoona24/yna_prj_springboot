@echo off

echo.
echo ==========================================
echo   YA-PRJ Scholarship Matching Service
echo ==========================================
echo.

cd /d "%~dp0"
set PROJECT_ROOT=%cd%

:: Check Python
echo [0/6] Checking requirements...
python --version > nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python is not installed.
    pause
    exit /b 1
)
echo - Python OK

node --version > nul 2>&1
if errorlevel 1 (
    echo [ERROR] Node.js is not installed.
    pause
    exit /b 1
)
echo - Node.js OK

docker --version > nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not installed.
    pause
    exit /b 1
)
echo - Docker OK
echo.

:: Clean up
echo [1/6] Cleaning up old processes...
taskkill /f /fi "WINDOWTITLE eq YA-PRJ*" > nul 2>&1
echo Done

:: Docker
echo [2/6] Starting Docker containers...
docker-compose down > nul 2>&1
docker-compose up -d mysql redis
if errorlevel 1 (
    echo [ERROR] Docker failed
    pause
    exit /b 1
)
echo Done

:: Directories
echo [3/6] Checking directories...
if not exist "database\mysql" mkdir database\mysql
if not exist "database\redis" mkdir database\redis
echo Done

:: MySQL wait
echo [4/6] Waiting for MySQL...
set CNT=0
:mysql_wait
set /a CNT=CNT+1
if %CNT% gtr 30 goto mysql_fail
docker-compose exec -T mysql mysqladmin ping -h localhost -u test_admin -p1111 > nul 2>&1
if errorlevel 1 (
    echo   Waiting... %CNT%/30
    timeout /t 2 /nobreak > nul
    goto mysql_wait
)
echo MySQL OK
goto mysql_done

:mysql_fail
echo [ERROR] MySQL failed to start
pause
exit /b 1

:mysql_done
docker-compose exec -T redis redis-cli ping > nul 2>&1
echo Redis OK

:: Backend setup
echo [5/6] Setting up Backend...
cd /d "%PROJECT_ROOT%\backend"

echo   Removing old venv...
if exist "venv" rmdir /s /q venv

echo   Creating new virtual environment...
python -m venv venv
if errorlevel 1 goto backend_fail

echo   Upgrading pip...
python -m pip install --upgrade pip > nul 2>&1

echo   Installing dependencies...
call venv\Scripts\python.exe -m pip install -r requirements.txt
if errorlevel 1 goto backend_fail

if not exist ".env" copy .env.example .env > nul

echo   Starting backend server...
start "YA-PRJ Backend" cmd /k "cd /d %PROJECT_ROOT%\backend && call venv\Scripts\activate && python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload"
timeout /t 5 /nobreak > nul
echo Backend OK
goto frontend_setup

:backend_fail
echo [ERROR] Backend setup failed
pause
exit /b 1

:: Frontend setup
:frontend_setup
echo [6/6] Setting up Frontend...
cd /d "%PROJECT_ROOT%\frontend"

:: Always reinstall node_modules for clean setup
if exist "node_modules" rmdir /s /q node_modules
echo   Running npm install...
call npm install
if errorlevel 1 goto frontend_fail

if not exist ".env" echo VITE_API_URL=http://localhost:8000> .env

echo   Starting frontend server...
start "YA-PRJ Frontend" cmd /k "cd /d %PROJECT_ROOT%\frontend && npx vite --host --port 9000"
timeout /t 5 /nobreak > nul
echo Frontend OK
goto done

:frontend_fail
echo [ERROR] Frontend setup failed
pause
exit /b 1

:done
echo.
echo ==========================================
echo   All services started successfully!
echo ==========================================
echo.
echo Frontend:  http://localhost:9000
echo Backend:   http://localhost:8000
echo API Docs:  http://localhost:8000/docs
echo Admin:     http://localhost:9000/admin (admin / 1234)
echo.
pause
