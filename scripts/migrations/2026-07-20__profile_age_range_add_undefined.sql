-- 2026-07-20 적용 완료 (운영 반영됨)
--
-- 배경:
--   Profile.anonymize()가 age_range에 AgeRangeType.UNDEFINED를 넣는데
--   DB enum에 UNDEFINED가 없어 매일 자정 탈퇴 회원 익명화가
--   "Data truncated for column 'age_range'"로 실패하고 있었다.
--   (최소 2026-06-21부터 30일간 120회 발생 확인)
--
--   같은 익명화 대상인 gender 컬럼은 이미 UNDEFINED를 포함하고 있어,
--   age_range만 스키마가 도메인 모델과 어긋난 상태였다.
--   따라서 null 대입이 아니라 enum에 UNDEFINED를 추가하는 방향으로 맞췄다.
--
-- 영향:
--   기존 값은 건드리지 않고 선택지만 추가. profile 199행 전부 보존 확인.
--
-- 이 프로젝트는 Flyway/Liquibase를 쓰지 않고 ddl-auto: none 이므로
-- 스키마 변경은 수동 적용하고 이 디렉터리에 이력을 남긴다.

ALTER TABLE profile MODIFY COLUMN age_range
    enum(
        'TEENAGER',
        'TWENTIES',
        'THIRTIES',
        'FORTIES',
        'FIFTIES',
        'SIXTIES',
        'SEVENTIES',
        'EIGHTIES',
        'OVER_NINETY',
        'UNDEFINED'
    ) NULL;
