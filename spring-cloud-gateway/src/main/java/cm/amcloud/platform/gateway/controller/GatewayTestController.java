package cm.amcloud.platform.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * GatewayTestController: A simple controller within the gateway application
 * to provide a public endpoint for testing purposes.
 */
@RestController
public class GatewayTestController {

    /**
     * Handles GET requests to /auth/public.
     * This endpoint is configured as permitAll() in SecurityConfig and is used
     * to verify that public paths are accessible without authentication.
     *
     * @return A Mono emitting a confirmation string.
     */
    @GetMapping("/auth/public")
    public Mono<String> publicEndpoint() {
        return Mono.just("This is a public endpoint accessible via the gateway.");
    }
}
