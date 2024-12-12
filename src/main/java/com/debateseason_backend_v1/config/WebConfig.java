package com.debateseason_backend_v1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    //프론트 와 콜스 문제를 해결하기 위한 클래스(설정)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") //모든 경로 허용
                .allowedOriginPatterns("http://debate-season.click:*")
                .allowedOriginPatterns("ws://debate-season.click:*")
                .allowedOriginPatterns("https://debate-season.click:*")
                .allowedOriginPatterns("wss://debate-season.click:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true) // 자격 증명 허용
                .maxAge(3600); //preflight
    }
}
