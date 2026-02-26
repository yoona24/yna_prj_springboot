#!/bin/bash

# ========================================
# YA-PRJ 장학금 매칭 서비스 시작 스크립트
# ========================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

cd "$(dirname "$0")"
PROJECT_ROOT=$(pwd)

echo -e "${BLUE}"
echo "=========================================="
echo "  🎓 YA-PRJ 장학금 매칭 서비스"
echo "=========================================="
echo -e "${NC}"

cleanup() {
    echo ""
    echo -e "${YELLOW}서비스를 종료합니다...${NC}"
    [ ! -z "$BACKEND_PID" ] && kill $BACKEND_PID 2>/dev/null && echo -e "${GREEN}✓ Backend 종료${NC}"
    [ ! -z "$FRONTEND_PID" ] && kill $FRONTEND_PID 2>/dev/null && echo -e "${GREEN}✓ Frontend 종료${NC}"
    exit 0
}

trap cleanup SIGINT SIGTERM

# ★ 기존 프로세스 정리 (포트 충돌 방지)
echo -e "${YELLOW}[0/6] 기존 프로세스 정리 중...${NC}"
# 포트 8000 (Backend) 사용 중인 프로세스 종료
if lsof -i :8000 &>/dev/null; then
    echo "  포트 8000 사용 중인 프로세스 종료..."
    kill -9 $(lsof -t -i:8000) 2>/dev/null || true
fi
# 포트 9000 (Frontend) 사용 중인 프로세스 종료
if lsof -i :9000 &>/dev/null; then
    echo "  포트 9000 사용 중인 프로세스 종료..."
    kill -9 $(lsof -t -i:9000) 2>/dev/null || true
fi
# uvicorn 프로세스 종료
pkill -f "uvicorn app.main:app" 2>/dev/null || true
echo -e "${GREEN}✓ 정리 완료${NC}"

# 1. Docker 컨테이너 정리
echo -e "${YELLOW}[1/6] Docker 컨테이너 확인 중...${NC}"
docker-compose down 2>/dev/null || true

# 2. 데이터베이스 디렉토리
echo -e "${YELLOW}[2/6] 데이터베이스 디렉토리 확인 중...${NC}"
mkdir -p database/mysql database/redis database/mysql-init
echo -e "${GREEN}✓ 준비 완료${NC}"

# 3. MySQL + Redis 시작
echo -e "${YELLOW}[3/6] MySQL + Redis 시작 중...${NC}"
docker-compose up -d mysql redis

# 4. MySQL 대기
echo -e "${YELLOW}[4/6] MySQL 준비 대기 중...${NC}"
for i in {1..30}; do
    if docker-compose exec -T mysql mysqladmin ping -h localhost -u test_admin -p1111 &> /dev/null; then
        echo -e "${GREEN}✓ MySQL 준비 완료${NC}"
        break
    fi
    [ $i -eq 30 ] && echo -e "${RED}✗ MySQL 시작 실패${NC}" && exit 1
    echo "  대기 중... ($i/30)"
    sleep 2
done

docker-compose exec -T redis redis-cli ping &> /dev/null && echo -e "${GREEN}✓ Redis 준비 완료${NC}"

# 5. Backend 시작
echo -e "${YELLOW}[5/6] Backend 시작 중...${NC}"
cd "$PROJECT_ROOT/backend"

# venv가 다른 시스템에서 복사된 경우 재생성
if [ -d "venv" ] && [ -f "venv/pyvenv.cfg" ]; then
    VENV_HOME=$(grep "^home" venv/pyvenv.cfg | cut -d'=' -f2 | tr -d ' ')
    if [ ! -d "$VENV_HOME" ]; then
        echo "  ⚠️  가상환경이 다른 시스템에서 생성됨. 재생성합니다..."
        rm -rf venv
    fi
fi

# venv 생성
if [ ! -d "venv" ]; then
    echo "  Python 가상환경 생성 중..."
    python3 -m venv venv
    echo -e "${GREEN}  ✓ 가상환경 생성 완료${NC}"
fi

# 의존성 설치
echo "  의존성 설치 중..."
source venv/bin/activate
pip install -q --upgrade pip
pip install -q -r requirements.txt
deactivate

# .env 파일
[ ! -f ".env" ] && cp .env.example .env && echo "  .env 파일 생성됨"

# Backend 실행
echo "  Backend 서버 시작..."
./venv/bin/uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload &
BACKEND_PID=$!
sleep 3
ps -p $BACKEND_PID > /dev/null && echo -e "${GREEN}✓ Backend 시작 완료${NC}" || { echo -e "${RED}✗ Backend 시작 실패${NC}"; exit 1; }

# 6. Frontend 시작
echo -e "${YELLOW}[6/6] Frontend 시작 중...${NC}"
cd "$PROJECT_ROOT/frontend"

[ ! -d "node_modules" ] && echo "  npm 패키지 설치 중..." && npm install
[ ! -f ".env" ] && echo "VITE_API_URL=http://localhost:8000" > .env

echo "  Frontend 서버 시작..."
npm run dev &
FRONTEND_PID=$!
sleep 3
ps -p $FRONTEND_PID > /dev/null && echo -e "${GREEN}✓ Frontend 시작 완료${NC}" || { echo -e "${RED}✗ Frontend 시작 실패${NC}"; exit 1; }

# 완료
echo ""
echo -e "${GREEN}=========================================="
echo "  ✅ 모든 서비스가 시작되었습니다!"
echo "==========================================${NC}"
echo ""
echo -e "${BLUE}📌 접속 URL:${NC}"
echo "   • Frontend:  http://localhost:9000"
echo "   • Backend:   http://localhost:8000"
echo "   • API Docs:  http://localhost:8000/docs"
echo "   • 관리자:    http://localhost:9000/admin (admin / 1234)"
echo ""
echo -e "${YELLOW}⚠️  종료하려면 Ctrl+C를 누르세요${NC}"
echo ""

wait
