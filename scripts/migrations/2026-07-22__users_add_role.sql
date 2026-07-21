-- 2026-07-22 적용 완료 (운영 반영됨)
--
-- 적용 결과:
--   users 231행 전부 role=USER 로 채워짐. ADMIN 승격은 아직 하지 않았다.
--
-- 배경:
--   v1.3 이슈/토론 구조 개편에서 이슈·채팅방 생성을 ADMIN 전용으로 잠근다.
--   그런데 지금까지 역할(Role) 개념이 코드에 존재하지 않았다.
--   - CustomUserDetails 가 모든 사용자에게 ROLE_USER 를 하드코딩
--   - users 테이블에 role 컬럼 없음, JWT 에 role 클레임 없음
--   - "ADMIN" 이라는 문자열은 주석과 Swagger 설명에만 존재
--   따라서 인가를 걸기 전에 역할을 담을 자리를 먼저 만든다.
--
-- 영향:
--   기존 231행은 전부 기본값 'USER' 로 채워진다. 읽기/쓰기 경로 동작 변화 없음.
--   이 단계에서는 아직 인가 매처를 걸지 않으므로 ADMIN 은 아무 효과가 없다.
--   (인가 적용은 v1.3.3 에서 별도 배포)
--
-- 호환성:
--   이미 발급된 access token 에는 role 클레임이 없다.
--   JwtUtil.getRole() 이 클레임 부재 시 USER 로 fallback 하므로
--   구버전 앱은 재로그인 없이 그대로 동작하고, access token 만료 주기 내에 자연 수렴한다.
--
-- 적용 후 할 일:
--   운영자 계정에만 수동으로 ADMIN 을 부여한다. 대상 user_id 는 사전에 확인할 것.
--     UPDATE users SET role = 'ADMIN' WHERE user_id IN (...);
--
-- 이 프로젝트는 Flyway/Liquibase 를 쓰지 않고 ddl-auto: none 이므로
-- 스키마 변경은 수동 적용하고 이 디렉터리에 이력을 남긴다.

ALTER TABLE users
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER'
    AFTER status;
