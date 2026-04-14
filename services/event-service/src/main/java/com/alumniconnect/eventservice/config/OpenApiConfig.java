package com.alumniconnect.eventservice.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AlumniConnect Event API")
                        .description("Event listing and registration endpoints for AlumniConnect.")
                        .version("v1"))
                .servers(List.of(
                        new Server().url("/event").description("Via API Gateway"),
                        new Server().url("http://localhost:8083").description("Direct service")));
    }
}
