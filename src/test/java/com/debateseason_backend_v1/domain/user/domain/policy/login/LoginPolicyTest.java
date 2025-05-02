package com.debateseason_backend_v1.domain.user.domain.policy.login;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.domain.UserId;
import com.debateseason_backend_v1.domain.user.domain.rules.login.BlockedUserRule;
import com.debateseason_backend_v1.domain.user.domain.rules.login.LoginRule;
import com.debateseason_backend_v1.domain.user.domain.rules.login.RefreshUserRule;
import com.debateseason_backend_v1.domain.user.domain.rules.login.WithdrawnUserRule;
import com.debateseason_backend_v1.domain.user.enums.SocialType;
import com.debateseason_backend_v1.domain.user.enums.UserStatus;

@ExtendWith(MockitoExtension.class)
class LoginPolicyTest {

    @Mock
    private BlockedUserRule blockedUserRule;

    @Mock
    private WithdrawnUserRule withdrawnUserRule;

    @Mock
    private RefreshUserRule refreshUserRule;

    private LoginPolicy loginPolicy;

    @BeforeEach
    void setUp() {
        loginPolicy = new LoginPolicy(List.of(refreshUserRule, blockedUserRule, withdrawnUserRule));
    }

    @Test
    @DisplayName("LoginPolicy는 모든 Rule을 순서대로 실행한다")
    void check_executesAllRulesInOrder() {
        // given
        User user = User.builder()
                .id(new UserId(1L))
                .status(UserStatus.ACTIVE)
                .socialType(SocialType.KAKAO)
                .identifier("identifier")
                .build();

        // when
        loginPolicy.check(user);

        // then
        // 순서 검증
        inOrder(refreshUserRule, blockedUserRule, withdrawnUserRule).verify(refreshUserRule).check(user);
        inOrder(refreshUserRule, blockedUserRule, withdrawnUserRule).verify(blockedUserRule).check(user);
        inOrder(refreshUserRule, blockedUserRule, withdrawnUserRule).verify(withdrawnUserRule).check(user);
    }

    @Test
    @DisplayName("Rule 중 하나라도 예외가 발생하면 이후 Rule은 실행되지 않는다")
    void check_whenRuleThrowsException_doesNotExecuteSubsequentRules() {
        // given
        User user = User.builder()
                .id(new UserId(1L))
                .status(UserStatus.ACTIVE)
                .socialType(SocialType.KAKAO)
                .identifier("identifier")
                .build();

        doThrow(new RuntimeException("차단 계정입니다.")).when(blockedUserRule).check(user);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loginPolicy.check(user);
        });
        assertEquals("차단 계정입니다.", exception.getMessage());

        // refreshUserRule은 실행되고, blockedUserRule에서 예외가 발생하므로 withdrawnUserRule은 실행되지 않음
        verify(refreshUserRule).check(user);
        verify(blockedUserRule).check(user);
        verify(withdrawnUserRule, never()).check(user);
    }

    @Test
    @DisplayName("LoginPolicy의 rules 메서드는 생성자에서 전달받은 rules를 반환한다")
    void rules_returnsRulesList() {
        // given
        List<LoginRule> expectedRules = List.of(refreshUserRule, blockedUserRule, withdrawnUserRule);

        // when
        List<LoginRule> actualRules = loginPolicy.rules();

        // then
        assertEquals(expectedRules, actualRules);
    }
}
