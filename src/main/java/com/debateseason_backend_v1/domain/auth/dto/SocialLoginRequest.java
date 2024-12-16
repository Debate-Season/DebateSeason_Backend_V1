package com.debateseason_backend_v1.domain.auth.dto;

import com.debateseason_backend_v1.common.enums.SocialType;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialLoginRequest {

	private String authCode;
	private SocialType socialType;
	
}