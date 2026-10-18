-- v1.4.4 토론위키 본문 요약 — Expand (컬럼 1개, 기존 무영향)
-- 웹 토론방 사이드바 요약 카드·이슈 페이지 meta description 용. 2~3문장 평문.
-- edit_summary(리비전 변경 사유)와 다른 필드다.
-- 적용: 백엔드 배포 전 prod 선적용 (명시 승인 필요). 운영 debate_wiki_revision 은 0건이다.

ALTER TABLE debate_wiki_revision
    ADD COLUMN summary VARCHAR(500) NULL AFTER edit_summary;

-- 되돌리기: ALTER TABLE debate_wiki_revision DROP COLUMN summary;
