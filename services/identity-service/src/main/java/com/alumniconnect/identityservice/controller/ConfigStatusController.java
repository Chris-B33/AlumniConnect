package com.alumniconnect.identityservice.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigStatusController {

    private final ConfigurableEnvironment environment;

    @Value("${app.config.message:Not loaded from Config Server (missing remote property)}")
    private String configMessage;

    public ConfigStatusController(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @GetMapping("/api/config")
    public Map<String, Object> config() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", configMessage);
        body.put("loadedFromConfigServer", hasConfigServerPropertySource());
        return body;
    }

    private boolean hasConfigServerPropertySource() {
        for (PropertySource<?> source : environment.getPropertySources()) {
            String name = source.getName().toLowerCase();
            if (name.contains("configserver")) {
                return true;
            }
        }
        return false;
    }
}
