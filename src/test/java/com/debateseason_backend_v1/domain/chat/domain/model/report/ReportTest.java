package com.debateseason_backend_v1.domain.chat.domain.model.report;

import com.debateseason_backend_v1.common.exception.CodeInterface;
import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.fixtures.chat.ReportFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Report 모메인 테스트")
class ReportTest {

    @Nested
    @DisplayName("Report 생성 테스트")
    class ReportCreateTest{
        @Nested
        @DisplayName("성공 케이스")
        class success{
            @Test
            @DisplayName("모든 필수 값이 있으면 Report객체가 정상적으로 생성된다.")
            void should_create_report_when_all_required_values(){
                //Given
                Long targetId = 1L;
                Long reporterId = 2L;
                ReportTargetType targetType = ReportTargetType.CHAT;
                Set<ReportReasonType> reasonTypes = Set.of(ReportReasonType.ABUSE);
                String description = "욕설 신고";
                //When
                Report report = Report.create(targetId, reporterId, targetType, reasonTypes, description);
                //Then
                assertThat(report).isNotNull().isInstanceOf(Report.class);
                assertThat(report.getTargetId()).isEqualTo(targetId);
                assertThat(report.getReporterId()).isEqualTo(reporterId);
                assertThat(report.getTargetType()).isEqualTo(targetType);
                assertThat(report.getDescription()).isEqualTo(description);

            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class fail{

        }
    }


    @Nested
    @DisplayName("신고 접수 처리 테스트")
    class ReportAcceptTest{

    }


    @Nested
    @DisplayName("신고 승인 테스트")
    class ReportApprovalTest{

        @Nested
        @DisplayName("성공 케이스")
        class success{

            @Test
            @DisplayName("신고를 관리자가 승인하면 신고상태가 ReportStatus.ACCEPTED 상태가 된다.")
            void should_report_status_pending_when_accept_report(){
                //Given
                Report givenReport = ReportFixture.createChatTypeBasicReport();
                Long givenAdminId = 1L;
                String givenAdminComment = "관리자 코멘트";
                //When
                givenReport.accept(givenAdminId, givenAdminComment);
                //Then
                assertThat(givenReport.getStatus()).isEqualTo(ReportStatus.ACCEPTED);
            }

        }

        @Nested
        @DisplayName("실패 케이스")
        class fail{

            @Test
            @DisplayName("이미 승인된 신고는 신고할 수 없다.")
            void should_report_status_pending_when_accept_report() {
                //Given
                Report givenReport = ReportFixture.createAcceptedChatTypeReport();
                Long givenAdminId = 1L;
                String givenAdminComment = "테스트 코멘트";
                //When & Then
                assertThatThrownBy(() -> givenReport.accept(givenAdminId, givenAdminComment))
                        .isInstanceOf(CustomException.class)
                        .extracting("codeInterface");
            }

        }
    }


    @Nested
    @DisplayName("신고 거부 테스트")
    class ReportRejectionTest{

    }

    @Nested
    @DisplayName("상태 변경 방어 로직 테스트")
    class GuardStatusChangeTest{

    }

}