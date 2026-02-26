#!/bin/bash

# YA-PRJ 서비스 종료 스크립트

echo "YA-PRJ 서비스를 종료합니다..."

cd "$(dirname "$0")"

# Docker Compose로 실행 중이면 종료
docker-compose down

# 로컬 프로세스 종료 (포트 8000, 9000)
if lsof -i :8000 &>/dev/null; then
    echo "포트 8000 프로세스 종료 중..."
    kill -9 $(lsof -t -i:8000) 2>/dev/null || true
fi

if lsof -i :9000 &>/dev/null; then
    echo "포트 9000 프로세스 종료 중..."
    kill -9 $(lsof -t -i:9000) 2>/dev/null || true
fi

# Java 프로세스 종료
pkill -f "java.*yaprj" 2>/dev/null || true

echo "✅ 서비스 종료 완료"
