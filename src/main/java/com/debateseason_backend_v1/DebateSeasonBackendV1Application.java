package com.debateseason_backend_v1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;

@EnableScheduling
@OpenAPIDefinition(servers = {@Server(url = "${swagger-base-url}", description = "Swagger https 프로토콜 적용")})
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class DebateSeasonBackendV1Application {

	
	public static void main(String[] args) {
		SpringApplication.run(DebateSeasonBackendV1Application.class, args);
	}

}
