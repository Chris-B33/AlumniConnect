package com.alumniconnect.identityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.alumniconnect.identityservice.config.JwtProperties;
import com.alumniconnect.identityservice.config.MinioProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, MinioProperties.class})
public class IdentityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
