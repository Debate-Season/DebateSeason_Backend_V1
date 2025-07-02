package com.debateseason_backend_v1.domain.chat.domain.model.chat;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.fixtures.chat.ChatFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Chat 도메인 테스트")
class ChatTest {

    @Nested
    @DisplayName("사용자가 자신의 메시지를 신고하려고 할 때")
    class WhenUserTriesToReportOneMessage{
        @Test
        @DisplayName("예외가 발생해야 한다")
        void then_should_throw_exception() {
            // Given: 사용자 ID가 1인 채팅 메시지가 있고
            Chat chat = ChatFixture.create();
            Long selfReporterId = chat.getUserId();
            //When 자신의 메시지를 신고 하려고 할때
            //Then: SELF_REPORT_NOT_ALLOWED 예외가 발생 한다
            CustomException expectException = assertThrows(CustomException.class, () -> {
                chat.guardSelfReport(selfReporterId);
            });
            assertEquals(ErrorCode.SELF_REPORT_NOT_ALLOWED, expectException.getCodeInterface());
        }
    }
}