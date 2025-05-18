package com.debateseason_backend_v1.domain.user.domain.policy.login;

import java.util.List;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.domain.policy.Policy;
import com.debateseason_backend_v1.domain.user.domain.rules.login.LoginRule;

@Component
public record LoginPolicy(List<LoginRule> rules) implements Policy<User> {
}