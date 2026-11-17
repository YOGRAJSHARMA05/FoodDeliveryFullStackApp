package com.fooddelivery.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Legacy Swagger resource endpoints.
 *
 * Springdoc (OpenAPI 3) serves its real metadata at /v3/api-docs and
 * /v3/api-docs/swagger-config. Some older tools still probe the Springfox
 * /swagger-resources paths, so this controller keeps those probes working
 * instead of letting them fall through to the global exception handler.
 */
@RestController
@RequestMapping("/swagger-resources")
public class SwaggerResourcesController {

    @GetMapping
    public List<Map<String, String>> resources() {
        return List.of(Map.of(
                "name", "default",
                "url", "/v3/api-docs",
                "swaggerVersion", "3.0"
        ));
    }

    @GetMapping("/configuration/ui")
    public Map<String, Object> uiConfiguration() {
        return Map.of();
    }

    @GetMapping("/configuration/security")
    public Map<String, Object> securityConfiguration() {
        return Map.of();
    }

    /** Return an empty object for any other legacy probe instead of a 500. */
    @GetMapping("/**")
    public Map<String, Object> fallback() {
        return Map.of();
    }
}
