package com.debateseason_backend_v1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class DebateSeasonBackendV1Application {

    public static void main(String[] args) {
        SpringApplication.run(DebateSeasonBackendV1Application.class, args);
    }

}
