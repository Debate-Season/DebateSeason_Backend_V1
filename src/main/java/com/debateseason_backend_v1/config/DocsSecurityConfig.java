package com.debateseason_backend_v1.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.slf4j.Slf4j;

/**
 * API 문서(Swagger) 접근 제어. 앱 API 체인({@link WebSecurityConfig})과 분리된 별도 체인이다.
 *
 * <p>브라우저 로그인 창(HTTP Basic). 계정은 {@code DOCS_USERS=이름:bcrypt해시,이름:bcrypt해시}.
 * <ul>
 *   <li>{@code team} — 모든 그룹 (토론철 서비스·관리·고객사)</li>
 *   <li>그 외 이름 (예: tmoney) — 고객사 그룹만. 내부 그룹 문서는 403</li>
 * </ul>
 * 계정이 하나도 없으면 문서는 아무도 못 연다 (fail closed). 운영 문서가 실수로 공개되지 않게 하려는 것이다.
 *
 * <p>bcrypt 해시에 {@code $} 가 들어가므로 .env 에는 작은따옴표로 감싼다 — systemd 도 셸 source 도 그대로 읽는다.
 */
@Slf4j
@Configuration
public class DocsSecurityConfig {

	public static final String TEAM = "team";
	static final String ROLE_TEAM = "DOCS_TEAM";
	static final String ROLE_PARTNER = "DOCS_PARTNER";

	// 고객사가 볼 수 있는 문서: 고객사 그룹 + Swagger UI 정적 파일·설정
	private static final String[] PARTNER_VISIBLE = {
		"/v3/api-docs/" + SwaggerConfig.GROUP_PARTNER,
		"/v3/api-docs/swagger-config",
		"/swagger-ui/**",
		"/swagger-ui.html",
		SwaggerConfig.PARTNER_DOCS_PATH
	};

	@Bean
	@Order(0)
	public SecurityFilterChain docsFilterChain(HttpSecurity http, @Value("${docs.auth.users:}") String rawUsers) throws Exception {

		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(new InMemoryUserDetailsManager(parse(rawUsers)));
		provider.setPasswordEncoder(new BCryptPasswordEncoder());

		return http
			.securityMatcher("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs", "/v3/api-docs/**",
				SwaggerConfig.PARTNER_DOCS_PATH)
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authenticationProvider(provider)
			.httpBasic(Customizer.withDefaults())
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(PARTNER_VISIBLE).hasAnyRole(ROLE_TEAM, ROLE_PARTNER)
				.anyRequest().hasRole(ROLE_TEAM))
			.build();
	}

	static List<UserDetails> parse(String raw) {
		List<UserDetails> users = new ArrayList<>();
		if (raw != null) {
			for (String token : raw.split(",")) {
				String item = token.trim();
				int sep = item.indexOf(':');
				if (sep <= 0 || !item.substring(sep + 1).startsWith("$2")) {
					if (!item.isEmpty()) {
						log.warn("문서 계정 항목을 무시합니다 - 형식은 이름:bcrypt해시");
					}
					continue;
				}
				String name = item.substring(0, sep);
				users.add(User.withUsername(name)
					.password(item.substring(sep + 1))
					.roles(TEAM.equals(name) ? ROLE_TEAM : ROLE_PARTNER)
					.build());
			}
		}
		log.info("API 문서 계정 {}개 로드 (이름: {})", users.size(), users.stream().map(UserDetails::getUsername).toList());
		return users;
	}
}
