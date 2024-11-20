package com.debateseason_backend_v1.domain.user.dto;

import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserDTO {

    // 이거 쓰려면 validation 의존성을 추가해야만 한다.
    @NotBlank
    private final String username;

    @NotBlank
    private final String password;


    private final String role;

    // 소속 커뮤니티
    private String community;


}
