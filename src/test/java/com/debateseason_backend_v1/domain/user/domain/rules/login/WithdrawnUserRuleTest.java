package com.debateseason_backend_v1.domain.user.domain.rules.login;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.domain.UserId;
import com.debateseason_backend_v1.domain.user.enums.SocialType;
import com.debateseason_backend_v1.domain.user.enums.UserStatus;

class WithdrawnUserRuleTest {

    private final WithdrawnUserRule withdrawnUserRule = new WithdrawnUserRule();

    @Test
    @DisplayName("탈퇴한 계정인 경우 예외가 발생한다")
    void check_withdrawnUser_throwsException() {
        // given
        User withdrawnUser = User.builder()
                .id(new UserId(1L))
                .status(UserStatus.WITHDRAWN)
                .socialType(SocialType.KAKAO)
                .identifier("identifier")
                .build();

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            withdrawnUserRule.check(withdrawnUser);
        });
        assertEquals("탈퇴한 계정입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("탈퇴하지 않은 계정인 경우 예외가 발생하지 않는다")
    void check_notWithdrawnUser_doesNotThrowException() {
        // given
        User activeUser = User.builder()
                .id(new UserId(1L))
                .status(UserStatus.ACTIVE)
                .socialType(SocialType.KAKAO)
                .identifier("identifier")
                .build();

        // when & then
        assertDoesNotThrow(() -> {
            withdrawnUserRule.check(activeUser);
        });
    }
}