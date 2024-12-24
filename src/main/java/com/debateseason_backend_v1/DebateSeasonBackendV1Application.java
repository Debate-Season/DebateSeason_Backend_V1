package com.debateseason_backend_v1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class) // 자동으로 날짜 입력활성화
@EnableJpaAuditing
public class DebateSeasonBackendV1Application {

	public static void main(String[] args) {
		SpringApplication.run(DebateSeasonBackendV1Application.class, args);
	}

}
