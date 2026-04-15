package com.alumniconnect.mentorshipservice.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mentorshipOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AlumniConnect Mentorship API")
                        .description("Mentorship discovery and request workflows for AlumniConnect.")
                        .version("v1"))
                .servers(List.of(
                        new Server().url("/mentorship").description("Via API Gateway"),
                        new Server().url("http://localhost:8082").description("Direct service")));
    }
}
