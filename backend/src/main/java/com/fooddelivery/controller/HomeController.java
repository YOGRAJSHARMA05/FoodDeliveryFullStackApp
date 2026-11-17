package com.fooddelivery.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public landing endpoint. Keeping the root path out of the JWT-protected
 * namespace makes the backend's entry URL useful and avoids a misleading 401.
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "status", "UP",
                "swaggerUi", "/swagger-ui/index.html",
                "apiDocs", "/v3/api-docs",
                "health", "/actuator/health"
        );
    }
}
