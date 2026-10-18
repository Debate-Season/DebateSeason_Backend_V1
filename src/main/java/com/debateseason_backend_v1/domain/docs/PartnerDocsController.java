package com.debateseason_backend_v1.domain.docs;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.debateseason_backend_v1.config.SwaggerConfig;

import io.swagger.v3.oas.annotations.Hidden;

/** 고객사에게 주는 문서 주소. Swagger UI 를 고객사 그룹으로 바로 연다. 접근 제어는 DocsSecurityConfig. */
@Hidden
@Controller
public class PartnerDocsController {

	@GetMapping(SwaggerConfig.PARTNER_DOCS_PATH)
	public String partnerDocs() {
		// Swagger UI 는 그룹을 표시 이름으로 고른다
		return "redirect:/swagger-ui/index.html?urls.primaryName="
			+ URLEncoder.encode(SwaggerConfig.PARTNER_DISPLAY_NAME, StandardCharsets.UTF_8);
	}
}
