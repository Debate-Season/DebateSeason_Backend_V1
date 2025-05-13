package com.debateseason_backend_v1.domain.chat.domain.model.report;


import java.util.List;
import java.util.Set;

public interface ReportTarget {

    /**
     * 신고를 생성 합니다.
     * @param targetId 신고 대상 ID
     * @param reporterId 신고자 ID
     * @param reportReasonTypes 신고 타입
     * @param description 신고 상세 이유
     * @return Report
     */
    //TODO User 도메인 모델은 아직 리팩토링 진행중으로 도메인모델 설계 되면 기존 유저 엔티티 결합 해제 하고 User 도메인 모델과 결합
    Report reportBy(Long targetId, Long reporterId, Set<ReportReasonType> reportReasonTypes, String description);

    /**
     * 모든 신고를 조회 합니다.
     */
    List<Report> reports();

    /**
     *  신고 상태를 확인 합니다.
     */
    ReportStatus reportStatus();



}
