package com.debateseason_backend_v1.domain.user.domain.rules.login;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.debateseason_backend_v1.domain.user.component.SystemTimeProvider;
import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.domain.UserId;
import com.debateseason_backend_v1.domain.user.enums.SocialType;
import com.debateseason_backend_v1.domain.user.enums.UserStatus;

@ExtendWith(MockitoExtension.class)
class RefreshUserRuleTest {

    @Mock
    private SystemTimeProvider timeProvider;

    private RefreshUserRule refreshUserRule;

    @BeforeEach
    void setUp() {
        refreshUserRule = new RefreshUserRule(timeProvider);
    }

    @Test
    @DisplayName("탈퇴 대기 상태이고 5일 이내인 경우 상태가 ACTIVE로 변경된다")
    void check_pendingWithdrawalUserWithinFiveDays_restoresUser() {
        // given
        LocalDateTime withdrawalRequestedAt = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime now = withdrawalRequestedAt.plusDays(3); // 3일 후
        
        User pendingWithdrawalUser = User.builder()
                .id(new UserId(1L))
                .status(UserStatus.PENDING_WITHDRAWAL)
                .socialType(SocialType.KAKAO)
                .identifier("identifier")
                .withdrawalRequestedAt(withdrawalRequestedAt)
                .build();
        
        when(timeProvider.now()).thenReturn(now);

        // when
        refreshUserRule.check(pendingWithdrawalUser);

        // then
        assertEquals(UserStatus.ACTIVE, pendingWithdrawalUser.getStatus());
        assertNull(pendingWithdrawalUser.getWithdrawalRequestedAt());
    }

    @Test
    @DisplayName("탈퇴 대기 상태이지만 5일 이상 지난 경우 상태가 변경되지 않는다")
    void check_pendingWithdrawalUserAfterFiveDays_doesNotRestoreUser() {
        // given
        LocalDateTime withdrawalRequestedAt = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime now = withdrawalRequestedAt.plusDays(5); // 5일 후
        
        User pendingWithdrawalUser = User.builder()
                .id(new UserId(1L))
                .status(UserStatus.PENDING_WITHDRAWAL)
                .socialType(SocialType.KAKAO)
                .identifier("identifier")
                .withdrawalRequestedAt(withdrawalRequestedAt)
                .build();
        
        when(timeProvider.now()).thenReturn(now);

        // when
        refreshUserRule.check(pendingWithdrawalUser);

        // then
        assertEquals(UserStatus.PENDING_WITHDRAWAL, pendingWithdrawalUser.getStatus());
        assertEquals(withdrawalRequestedAt, pendingWithdrawalUser.getWithdrawalRequestedAt());
    }

    @Test
    @DisplayName("탈퇴 대기 상태가 아닌 경우 상태가 변경되지 않는다")
    void check_notPendingWithdrawalUser_doesNotChangeStatus() {
        // given
        User activeUser = User.builder()
                .id(new UserId(1L))
                .status(UserStatus.ACTIVE)
                .socialType(SocialType.KAKAO)
                .identifier("identifier")
                .build();

        // when
        refreshUserRule.check(activeUser);

        // then
        assertEquals(UserStatus.ACTIVE, activeUser.getStatus());
        verify(timeProvider, never()).now();
    }
}