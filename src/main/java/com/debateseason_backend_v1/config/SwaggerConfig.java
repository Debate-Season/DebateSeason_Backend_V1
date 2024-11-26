package com.debateseason_backend_v1.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(){
        Info customInfo = new Info()
                .title("토론철 Chat API DOC")
                .version("V1.0")
                .description("채팅 API 문서");

        return new OpenAPI()
                .components(new Components())
                .info(customInfo);
    }


}
