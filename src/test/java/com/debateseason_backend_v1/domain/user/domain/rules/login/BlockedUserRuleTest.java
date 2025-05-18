package com.debateseason_backend_v1.domain.user.domain.rules.login;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.domain.UserId;
import com.debateseason_backend_v1.domain.user.enums.SocialType;
import com.debateseason_backend_v1.domain.user.enums.UserStatus;

class BlockedUserRuleTest {

    private final BlockedUserRule blockedUserRule = new BlockedUserRule();

    @Test
    @DisplayName("차단된 계정인 경우 예외가 발생한다")
    void check_blockedUser_throwsException() {
        // given
        User blockedUser = User.builder()
                .id(new UserId(1L))
                .status(UserStatus.BLOCKED)
                .socialType(SocialType.KAKAO)
                .identifier("identifier")
                .build();

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            blockedUserRule.check(blockedUser);
        });
        assertEquals("차단 계정입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("차단되지 않은 계정인 경우 예외가 발생하지 않는다")
    void check_notBlockedUser_doesNotThrowException() {
        // given
        User activeUser = User.builder()
                .id(new UserId(1L))
                .status(UserStatus.ACTIVE)
                .socialType(SocialType.KAKAO)
                .identifier("identifier")
                .build();

        // when & then
        assertDoesNotThrow(() -> {
            blockedUserRule.check(activeUser);
        });
    }
}