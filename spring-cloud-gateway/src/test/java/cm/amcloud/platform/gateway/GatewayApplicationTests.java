package cm.amcloud.platform.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.TestPropertySource; 

import reactor.core.publisher.Mono;

/**
 * Integration test for the Spring Cloud Gateway application context loading.
 * This test verifies that the gateway's Spring context loads correctly
 * with all its route and security configurations, without requiring
 * the target backend microservices to be running.
 * All configuration properties are provided via @TestPropertySource
 * to simulate a complete deployment environment.
 */
@SpringBootTest // Loads the full Spring application context.
                // Essential for integration tests that require Spring beans to be started.
@TestPropertySource(properties = {
    // --- Server Configuration ---
    "server.port=8080",
    "spring.application.name=gateway",

    // --- Test Route Configuration ---
    // Simple test route to verify basic gateway configuration.
    "spring.cloud.gateway.routes[0].id=test_route",
    "spring.cloud.gateway.routes[0].uri=http://httpbin.org",
    "spring.cloud.gateway.routes[0].predicates[0]=Path=/test/**",
    "spring.cloud.gateway.routes[0].filters[0]=RewritePath=/test/(?<segment>.*), /${segment}",

    // --- IAM Route Configuration ---
    // Route configuration for the IAM microservice.
    "spring.cloud.gateway.routes[1].id=iam_route",
    "spring.cloud.gateway.routes[1].uri=http://localhost:8081",
    "spring.cloud.gateway.routes[1].predicates[0]=Path=/auth/**",

    // --- Billing Route Configuration ---
    // Route configuration for the Billing microservice.
    "spring.cloud.gateway.routes[2].id=billing_route",
    "spring.cloud.gateway.routes[2].uri=http://localhost:8082",
    "spring.cloud.gateway.routes[2].predicates[0]=Path=/billing/**",
    "spring.cloud.gateway.routes[2].predicates[1]=Path=/billing",
    "spring.cloud.gateway.routes[2].filters[0]=RewritePath=/billing(?<segment>/?.*), /api/subscriptions${segment}",

    // --- Reservation Service Configuration ---
    // Route configuration for the Reservation microservice.
    "spring.cloud.gateway.routes[3].id=reservation_route",
    "spring.cloud.gateway.routes[3].uri=http://localhost:8083",
    "spring.cloud.gateway.routes[3].predicates[0]=Path=/reservations/**",
    "spring.cloud.gateway.routes[3].predicates[1]=Path=/reservations",
    "spring.cloud.gateway.routes[3].filters[0]=RewritePath=/reservations(?<segment>/?.*), /api/reservations${segment}",

    // --- Invitation Route Configuration ---
    // Route configuration for the Invitation microservice.
    "spring.cloud.gateway.routes[4].id=invitation_route",
    "spring.cloud.gateway.routes[4].uri=http://localhost:8084",
    "spring.cloud.gateway.routes[4].predicates[0]=Path=/invitations/**",
    "spring.cloud.gateway.routes[4].predicates[1]=Path=/invitations",
    "spring.cloud.gateway.routes[4].filters[0]=RewritePath=/invitations(?<segment>/?.*), /api/invitations${segment}",

    // --- Notification Route Configuration ---
    // Route configuration for the Notification microservice.
    "spring.cloud.gateway.routes[5].id=notification_route",
    "spring.cloud.gateway.routes[5].uri=http://localhost:8085",
    "spring.cloud.gateway.routes[5].predicates[0]=Path=/notifications/**",
    "spring.cloud.gateway.routes[5].predicates[1]=Path=/notifications",
    "spring.cloud.gateway.routes[5].filters[0]=RewritePath=/notifications(?<segment>/?.*), /api/notifications${segment}",

    // --- Secure Data Route Configuration ---
    // Route configuration for secure data (often pointing to IAM or a data service).
    "spring.cloud.gateway.routes[6].id=secure_data_route",
    "spring.cloud.gateway.routes[6].uri=http://localhost:8081/secure-data",
    "spring.cloud.gateway.routes[6].predicates[0]=Path=/secure-data",

    // --- Test HTTPS Route Configuration ---
    // Route configuration to test HTTPS redirection or connectivity.
    "spring.cloud.gateway.routes[7].id=test_https_route",
    "spring.cloud.gateway.routes[7].uri=https://example.com",
    "spring.cloud.gateway.routes[7].predicates[0]=Path=/test/https",
    "spring.cloud.gateway.routes[7].filters[0]=RewritePath=/test/https, /",

    // --- JWT Configuration ---
    // JWT configuration properties for token validation.
    "jwt.issuer-uri=http://localhost:8081",
    "jwt.jwk-set-uri=http://localhost:8081/jwks.json",

    // --- Logging Configuration ---
    "logging.level.cm.amcloud.platform.gateway=DEBUG",
    "logging.level.org.springframework.cloud.gateway=DEBUG",

    // --- Bean Definition Overriding ---
    // Allows test beans to override existing application beans, useful for mocking.
    "spring.main.allow-bean-definition-overriding=true"
})
@Import(GatewayApplicationTests.JwtDecoderTestConfig.class) // Imports the nested test configuration class
class GatewayApplicationTests {

    /**
     * Nested @TestConfiguration class to provide a mock or dummy bean
     * for ReactiveJwtDecoder during the test context loading.
     * This prevents the need for a real JWT validation setup (e.g., fetching JWKs)
     * when only testing context loading.
     */
    @TestConfiguration
    static class JwtDecoderTestConfig {
        /**
         * Defines a dummy ReactiveJwtDecoder bean.
         * This bean will be used by Spring Security in the test context
         * instead of the real JwtDecoder, allowing the application context
         * to load even without a valid JWK Set URI or actual JWT signing keys.
         * It simply returns a dummy Jwt for any provided token value.
         * @return A dummy ReactiveJwtDecoder instance.
         */
        @Bean
        public ReactiveJwtDecoder jwtDecoder() {
            // Return a dummy JWT for any token, so the context loads successfully.
            // This bypasses actual JWT validation during context startup.
            return token -> Mono.just(Jwt.withTokenValue(token)
                    .header("alg", "none") // Dummy algorithm header
                    .claim("sub", "test") // Dummy subject claim
                    .build());
        }
    }

    /**
     * This test method simply verifies that the Spring application context
     * loads successfully without any errors. It serves as a basic sanity check
     * for the application's overall configuration and bean wiring within a test environment.
     * No specific assertions are needed here, as the test passes if the context loads without exceptions.
     */
    @Test
    void contextLoads() {
        // The test passes if the application context loads without throwing exceptions.
        // This confirms that all required properties are resolved and beans can be created.
    }

}
