-- 2026-10-11 적용 완료 (운영 반영됨)
--
-- 적용 결과:
--   create_issue 21행(전부 2026-03-03 시범 실행분) 모두 embedding_version='e5l-v1' 로 채워짐.
--
-- 배경:
--   데이터 파이프라인(toronchul_data_v1) v0.9.0 이 이슈 벡터의 임베딩 모델 버전을
--   구분하려고 이 컬럼을 쓴다. 파이프라인은 첫 실행 때 스스로 ALTER 하려 했지만
--   pipeline_user 에 create_issue ALTER 권한이 없어 운영 첫 실행이 권한 오류로 멈춘다.
--   v1.3.0 핸드오프 §3.4 합의대로 운영 스키마 변경은 백엔드 이 디렉터리로 일원화하고,
--   파이프라인은 컬럼이 없으면 ALTER 대신 명확한 오류로 중단하도록 바뀐다.
--
--   기존 21행은 v0.9.0 이전 모델로 만든 벡터라 'e5l-v1' 로 표시한다.
--   새 버전('e5l-c1')으로의 재임베딩은 파이프라인 migrate_issue_vectors.py 가 별도로 한다.
--
-- 영향:
--   백엔드 코드는 create_issue 를 읽지도 쓰지도 않는다. NULL 허용 컬럼 추가라 기존 행·쿼리 영향 없음.
--
-- 같은 날 권한 정리 (스키마 변경 아님, 이력만 남긴다):
--   GRANT  SELECT ON debate_season.article TO pipeline_user@'%';
--     크롤러 본문(article)을 파이프라인이 군집 품질 평가에 쓰기 위해.
--   REVOKE ALTER  ON debate_season.media   FROM pipeline_user@'%';
--     파이프라인의 media 자동 ALTER 는 더 이상 실행되지 않으며(create_issue_id 이미 존재),
--     media 스키마 소유권을 백엔드로 일원화한다.

ALTER TABLE create_issue
    ADD COLUMN embedding_version VARCHAR(32) COLLATE utf8mb4_unicode_ci NULL;

UPDATE create_issue SET embedding_version = 'e5l-v1' WHERE embedding_version IS NULL;
