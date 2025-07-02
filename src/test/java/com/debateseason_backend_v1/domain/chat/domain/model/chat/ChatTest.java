package com.debateseason_backend_v1.domain.chat.domain.model.chat;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.chat.domain.model.report.Report;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportReasonType;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportTargetType;
import com.debateseason_backend_v1.fixtures.chat.ChatFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.assertj.core.api.Assertions;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;


@DisplayName("Chat 도메인 테스트")
class ChatTest {


    @Nested
    @DisplayName("채팅 신고 테스트")
    class ChatReportTest {

        @Nested
        @DisplayName("성공 케이스")
        class Success {

            @Test
            @DisplayName("채팅을 정상적으로 신고하면 Report 객체가 생성된다.")
            void should_create_Report_object_when_report_chat_normally() {
                //given
                Chat chat = ChatFixture.create();
                Long anotherUserId = 123L;
                String givenReportReasonDescription = "욕설 했어요";
                //when
                Report expectReport = chat.reportBy(
                        chat.getId(),
                        anotherUserId,
                        Set.of(ReportReasonType.ABUSE),
                        givenReportReasonDescription
                );
                //then
                assertThat(expectReport)
                        .isNotNull()
                        .isInstanceOf(Report.class)
                        .satisfies(report -> {
                            assertThat(report.getReporterId()).isEqualTo(anotherUserId);
                            assertThat(report.getReportReasonTypes()).contains(ReportReasonType.ABUSE);
                            assertThat(report.getDescription()).isEqualTo(givenReportReasonDescription);
                            assertThat(report.getTargetType()).isEqualTo(ReportTargetType.CHAT);
                            assertThat(report.getCreatedAt()).isNotNull();
                        });
            }

            @Test
            @DisplayName("다른 사용자 ID로 신고하면 예외가 발생하지 않는다")
            void should_not_throw_exception_when_different_userId() {

                //given
                Chat chat = ChatFixture.create();
                Long differentUserId = 123L;
                //when & then
                assertThatCode(() -> chat.guardSelfReport(differentUserId))
                        .doesNotThrowAnyException();
            }

            @Test
            @DisplayName("채팅을 신고하면 채팅 메시지가 마스킹 처리된다.")
            void should_mask_reported_message_when_report_normally() {
                //given
                Chat chat = ChatFixture.create();

                //when
                Chat maskedChat = ChatFixture.createMaskReportedChat(chat);
                
                //then : 채팅의 메시지가 정상적으로 마스킹 처리 되는지 확인
                assertThat(maskedChat.getContent()).isEqualTo(Chat.REPORTED_MESSAGE_CONTENT);
            }
        }


        @Nested
        @DisplayName("실패 케이스")
        class Fail {

            @Test
            @DisplayName("동일한 사용자 ID로 신고하면 SELF_REPORT_NOT_ALLOWED 예외가 발생한다")
            void should_throw_self_report_not_allowed_when_same_userId() {
                // Given: 사용자 ID가 1인 채팅 메시지가 있고
                Chat chat = ChatFixture.create();
                Long selfReporterId = chat.getUserId();
                //When 자신의 메시지를 신고 하려고 할때
                //Then: SELF_REPORT_NOT_ALLOWED 예외가 발생 한다
                assertThatThrownBy(() -> chat.guardSelfReport(selfReporterId))
                        .isInstanceOf(CustomException.class)
                        .extracting("codeInterface")
                        .isEqualTo(ErrorCode.SELF_REPORT_NOT_ALLOWED);
            }

        }

    }


}