package com.alumniconnect.identityservice.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI identityOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AlumniConnect Identity API")
                        .description("Authentication, registration, and identity endpoints for AlumniConnect.")
                        .version("v1"))
                .servers(List.of(
                        new Server().url("/identity").description("Via API Gateway"),
                        new Server().url("http://localhost:8081").description("Direct service")));
    }
}
