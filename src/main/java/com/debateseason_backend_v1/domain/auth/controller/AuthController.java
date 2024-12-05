package com.debateseason_backend_v1.domain.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.domain.auth.doc.AuthControllerDoc;
import com.debateseason_backend_v1.security.dto.LoginRequestDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerDoc {

	@PostMapping("/login")
	public void login(
		@RequestBody @Valid LoginRequestDTO loginRequestDTO
	) {
		// 실제 로그인 처리는 LoginFilter에서 수행됨
	}
	
}