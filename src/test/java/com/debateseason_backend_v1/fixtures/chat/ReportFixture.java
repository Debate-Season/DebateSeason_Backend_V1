package com.debateseason_backend_v1.fixtures.chat;

import com.debateseason_backend_v1.domain.chat.domain.model.report.Report;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportReasonType;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportTargetType;

import java.util.Set;

public class ReportFixture {

    public static Report createChatTypeBasicReport(){
        return Report.create(
                1L,1L, ReportTargetType.CHAT,
                Set.of(ReportReasonType.ABUSE),"부적절한신고"
        );
    }

    /**
     * return: 관리자가 승인한 Report를 반환합니다. 관리자가 승인한 신고이기 떄문에 신고상태는 ACCEPTED 상태입니다.
     */
    public static Report createAcceptedChatTypeReport(){
        Report report = createChatTypeBasicReport();
        report.accept(1L, "테스트 코멘트");
        return report;
    }
}
