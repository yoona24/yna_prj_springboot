-- YA-PRJ 데이터베이스 초기 설정
-- MySQL 8.0

-- 기본 문자셋 설정
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 권한 부여
GRANT ALL PRIVILEGES ON yaprj.* TO 'test_admin'@'%';
FLUSH PRIVILEGES;

-- 초기 장학금 데이터 (선택사항)
-- 앱 실행 시 자동 생성되므로 필요시에만 사용
