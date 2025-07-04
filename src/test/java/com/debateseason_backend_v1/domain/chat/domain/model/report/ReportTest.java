package com.debateseason_backend_v1.domain.chat.domain.model.report;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.fixtures.chat.ChatTypeReportFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Report 도메인 테스트")
class ReportTest {


    @Nested
    @DisplayName("Report 생성 테스트")
    class ReportCreateTest {
        @Nested
        @DisplayName("성공 케이스")
        class success {
            @Test
            @DisplayName("모든 필수 값이 있으면 Report객체가 정상적으로 생성된다.")
            void should_create_report_when_all_required_values() {
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

            @Test
            @DisplayName("처음 Report 객체를 생성하면 Report status 는 NONE 이다.")
            void should_report_status_NONE_when_first_creat_report() {
                //Given
                Long givenTargetId = 1L;
                Long givenReporterId = 2L;
                ReportTargetType givenTargetType = ReportTargetType.CHAT;
                Set<ReportReasonType> givenReportReasonTypes = Set.of(ReportReasonType.ABUSE);
                String givenDescription = "신고이유";
                //When
                Report givenReport = Report.create(
                        givenTargetId,
                        givenReporterId,
                        givenTargetType,
                        givenReportReasonTypes,
                        givenDescription
                );
                //Then
                assertThat(givenReport.getStatus())
                        .isNotNull()
                        .isEqualTo(ReportStatus.NONE);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class fail {

            @Test
            @DisplayName("신고를 생성할때 신구신고 대상 ID(targetId) ,신고 대상 타입(targetType),신고 이유(reasonType) 중에 하나라도 값이 없으면 IllegalArgumentException 예외를 터트린다.")
            void should_throw_exception_when_any_of_targetId_targetType_or_reasonType_is_missing() {

                //When & Then
                //targetId 가 null 일때
                assertThatThrownBy(ChatTypeReportFixture::createTargetIdNullReport)
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("신고 대상 ID(targetId)는 필수 입니다");
                //targetType 이 null 일때
                assertThatThrownBy(ChatTypeReportFixture::createTargetTypeNullReport)
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("신고 대상 타입(targetType)은 필수 입니다");
                //reasonType 이 null 일때
                assertThatThrownBy(ChatTypeReportFixture::createReasonTypeNullReport)
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("신고 이유(reasonType)는 필수 입니다.");

            }
        }

    }

    @Nested
    @DisplayName("신고 접수 처리 테스트")
    class ReportAcceptTest {

        @Nested
        @DisplayName("성공 케이스")
        class success {
            @Test
            @DisplayName("정상적으로 신고 접수가 되면 신고 상태가 PENDING 상태가 된다")
            void should_report_status_PENDING_when_all_required_values() {
                //Given
                Report givenReport = ChatTypeReportFixture.createReport();
                System.out.println(givenReport.getStatus());
                //When
                givenReport.markAsProcessing();
                //Then
                assertThat(givenReport.getStatus()).isEqualTo(ReportStatus.PENDING);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class Fail {
            @Test
            @DisplayName("이미 승인된 신고를 신고할때 접수 처리할 수 없다")
            void should_throw_exception_when_mark_as_processing_already_accepted_report() {
                // Given
                Report givenReport = ChatTypeReportFixture.createAcceptedReport();

                // When & Then
                assertThatThrownBy(givenReport::markAsProcessing)
                        .isInstanceOf(CustomException.class)
                        .extracting("codeInterface")
                        .isEqualTo(ErrorCode.REPORT_ALREADY_PROCESSED);
            }

            @Test
            @DisplayName("이미 거부된 신고는 접수 처리할 수 없다")
            void should_throw_exception_when_mark_as_processing_already_rejected_report() {
                // Given
                Report givenReport = ChatTypeReportFixture.createRejectedReport();

                // When & Then
                assertThatThrownBy(givenReport::markAsProcessing)
                        .isInstanceOf(CustomException.class)
                        .extracting("codeInterface")
                        .isEqualTo(ErrorCode.REPORT_ALREADY_PROCESSED);
            }

        }
    }


    @Nested
    @DisplayName("신고 승인 테스트")
    class ReportApprovalTest {

        @Nested
        @DisplayName("성공 케이스")
        class success {

            @Test
            @DisplayName("신고를 관리자가 승인하면 신고상태가 ReportStatus.ACCEPTED 상태가 된다.")
            void should_report_status_pending_when_accept_report() {
                //Given
                Report givenReport = ChatTypeReportFixture.createPendingReport();
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
        class fail {

            @Test
            @DisplayName("이미 신고를 한상태 (ReportStatus.ACCEPTED ReportStatus.REJECTED)인 경우에는 에러코드 REPORT_ALREADY_PROCESSED를 반환하며 실패한다.")
            void should_report_status_pending_when_accept_report() {
                //Given
                Report givenReport = ChatTypeReportFixture.createAcceptedReport();
                Long givenAdminId = 1L;
                String givenAdminComment = "테스트 코멘트";
                //When & Then
                assertThatThrownBy(() -> givenReport.accept(givenAdminId, givenAdminComment))
                        .isInstanceOf(CustomException.class)
                        .extracting("codeInterface")
                        .isEqualTo(ErrorCode.REPORT_ALREADY_PROCESSED);
            }

            @Test
            @DisplayName("신고를 승인할때 신구신고 대상 ID(targetId) ,신고 대상 타입(targetType),신고 이유(reasonType) 중에 하나라도 값이 없으면 IllegalArgumentException 예외를 터트린다.")
            void should_throw_exception_when_any_of_targetId_targetType_or_reasonType_is_missing_and_accept_report() {
                //Given
                Long givenAdminId = 1L;
                String givenAdminComment = "어디민 코멘트";

                //When & Then
                //targetId 가 null 일때
                assertThatThrownBy(() -> ChatTypeReportFixture.createTargetIdNullReport().accept(givenAdminId, givenAdminComment))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("신고 대상 ID(targetId)는 필수 입니다");
                //targetType 이 null 일때
                assertThatThrownBy(() -> ChatTypeReportFixture.createTargetTypeNullReport().accept(givenAdminId, givenAdminComment))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("신고 대상 타입(targetType)은 필수 입니다");
                //reasonType 이 null 일때
                assertThatThrownBy(() -> ChatTypeReportFixture.createReasonTypeNullReport().accept(givenAdminId, givenAdminComment))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("신고 이유(reasonType)는 필수 입니다.");

            }

        }
    }


    @Nested
    @DisplayName("신고 거부 테스트")
    class ReportRejectionTest {

        @Nested
        @DisplayName("성공 케이스")
        class success {
            @Test
            @DisplayName("정상적으로 신고가 되면 Report의 신고 상태는 REJECTED 가 된다.")
            void should_report_status_REJECTED_when_reject_report() {
                //Given
                Long givenAdminId = 1L;
                String givenAdminComment = "어드민코멘트";
                Report givenReport = ChatTypeReportFixture.createReport();
                //When
                givenReport.reject(givenAdminId, givenAdminComment);
                //Then
                assertThat(givenReport.getStatus()).isEqualTo(ReportStatus.REJECTED);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class fail {
            //신고를 거부할때 신구신고 대상 ID(targetId) ,신고 대상 타입(targetType),신고 이유(reasonType) 중에 하난라도 값이 없으면 IllegalArgumentException 예외를 터트린다.
            @Test
            @DisplayName("신고를 승인할때 신구신고 대상 ID(targetId) ,신고 대상 타입(targetType),신고 이유(reasonType) 중에 하나라도 값이 없으면 IllegalArgumentException 예외를 터트린다.")
            void should_throw_exception_when_any_of_targetId_targetType_or_reasonType_is_missing_and_accept_report() {
                //Given
                Long givenAdminId = 1L;
                String givenAdminComment = "어디민 코멘트";

                //When & Then
                //targetId 가 null 일때
                assertThatThrownBy(() -> ChatTypeReportFixture.createTargetIdNullReport().reject(givenAdminId, givenAdminComment))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("신고 대상 ID(targetId)는 필수 입니다");
                //targetType 이 null 일때
                assertThatThrownBy(() -> ChatTypeReportFixture.createTargetTypeNullReport().reject(givenAdminId, givenAdminComment))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("신고 대상 타입(targetType)은 필수 입니다");
                //reasonType 이 null 일때
                assertThatThrownBy(() -> ChatTypeReportFixture.createReasonTypeNullReport().reject(givenAdminId, givenAdminComment))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("신고 이유(reasonType)는 필수 입니다.");

            }

        }

    }

    @Nested
    @DisplayName("상태 변경 방어 로직 테스트")
    class GuardStatusChangeTest {

        @Nested
        @DisplayName("성공 케이스")
        class Success {
            @Test
            @DisplayName("이미 처리된 신고인 경우 CustomException(ErrorCode.REPORT_ALREADY_PROCESSED) 예외를 발생시킨다")
            void should_guard_status_change_when_already_process_report() {
                //Given
                Report givenReport = ChatTypeReportFixture.createAcceptedReport();

                //When & Then
                assertThatThrownBy(givenReport::guardStatusChange)
                        .isInstanceOf(CustomException.class)
                        .extracting("codeInterface")
                        .isEqualTo(ErrorCode.REPORT_ALREADY_PROCESSED);
            }
        }

    }


}