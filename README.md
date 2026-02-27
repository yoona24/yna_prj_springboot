# YA-PRJ 장학금 매칭 서비스

공공데이터포털(data.go.kr)에 있는 [한국장학재단_학자금지원정보(대학생)] 데이터를 활용하여 대학생 및 대학원생을 대상으로 지원하는 학자금 정보(운영기관, 상품명, 상품구분, 학자금 지원 유형, 신청대상, 신청기간, 지원금액, 지원인원 등)와 장학금 자격 확인 서비스 (https://www.data.go.kr/data/15028252/fileData.do)

## 기술 스택

- **Backend**: Spring Boot 3.2 + Java 17
- **Frontend**: React + TypeScript + Vite
- **Database**: MySQL 8.0 + Redis
- **Container**: Docker + Docker Compose

## 빠른 시작 (Docker Compose 권장)

### 1. 전체 서비스 한 번에 실행

```bash
# 프로젝트 디렉토리로 이동
cd yna_prj_springboot/app

# 모든 서비스 빌드 및 실행
docker-compose up --build

# 또는 백그라운드 실행
docker-compose up --build -d
```

### 2. 서비스 상태 확인

```bash
docker-compose ps
```

### 3. 로그 확인

```bash
# 모든 서비스 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

### 4. 서비스 종료

```bash
# 컨테이너 중지
docker-compose down

# 컨테이너 + 볼륨(DB 데이터) 삭제
docker-compose down -v
```

## 접속 URL

| 서비스 | URL |
|--------|-----|
| Frontend | http://localhost:9000 |
| Backend API | http://localhost:8000 |
| 관리자 페이지 | http://localhost:9000/admin |

## 관리자 계정

- **ID**: admin
- **PW**: 1234

## 로컬 개발 (Docker 없이)

### 사전 요구사항

- Java 17+
- Node.js 18+
- MySQL 8.0
- Redis

### 실행

**Mac/Linux:**
```bash
./startup.sh
```

**Windows:**
```cmd
startup.bat
```

## 프로젝트 구조

```
ya_prj/app/
├── docker-compose.yml      # Docker Compose 설정
├── startup.sh              # 로컬 실행 스크립트 (Mac/Linux)
├── startup.bat             # 로컬 실행 스크립트 (Windows)
├── backend-spring/         # Spring Boot 백엔드
│   ├── src/
│   ├── build.gradle
│   └── Dockerfile
├── frontend/               # React 프론트엔드
│   ├── src/
│   ├── package.json
│   └── Dockerfile
└── database/               # DB 데이터 (자동 생성)
```

## 주요 기능

1. **장학금 자격 확인**: 사용자 정보 입력 → 자격 여부 자동 판정
2. **관리자 기능**: CSV 업로드, 장학금 관리, 대시보드
3. **소셜 로그인**: 카카오, 네이버, 구글 (<span style="color: red;">작업진행 중...</span>)

## 환경 변수

`docker-compose.yml`에서 설정하거나 `.env` 파일 사용:

```env
# OAuth (선택사항)
KAKAO_CLIENT_ID=your_client_id
KAKAO_CLIENT_SECRET=your_client_secret
NAVER_CLIENT_ID=your_client_id
NAVER_CLIENT_SECRET=your_client_secret
GOOGLE_CLIENT_ID=your_client_id
GOOGLE_CLIENT_SECRET=your_client_secret
```
