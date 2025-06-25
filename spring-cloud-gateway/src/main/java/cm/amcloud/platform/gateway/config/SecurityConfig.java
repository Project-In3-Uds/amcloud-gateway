package cm.amcloud.platform.gateway.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

/**
 * SecurityConfig: Configures Spring Security as a Resource Server for the Gateway,
 * aligning with typical IAM logic for JWT-based authentication and authorization.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * Configures the SecurityWebFilterChain for the gateway as a Resource Server.
     * It disables CSRF, configures authorization rules, and sets up JWT-based
     * authentication using the `oauth2ResourceServer`.
     *
     * @param http The ServerHttpSecurity object provided by Spring Security WebFlux.
     * @return The configured SecurityWebFilterChain.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        logger.info("Configuring SecurityWebFilterChain...");

        return http
                // Disable CSRF protection as it's typically not needed for stateless REST APIs
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // Configure authorization rules for incoming requests
                .authorizeExchange(exchange -> exchange
                        // Allow all requests to paths starting with /auth/ and /public/ without authentication
                        .pathMatchers("/auth/**", "/public/**").permitAll()
                        // Require ADMIN role for requests to /api/admin/**
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")
                        // Require 'read' scope for requests to /api/data/**
                        .pathMatchers("/api/data/**").hasAuthority("SCOPE_read")
                        // Secure all other requests; authentication is required by default
                        .anyExchange().authenticated()
                )
                // Configure OAuth2 Resource Server to enable JWT authentication
                .oauth2ResourceServer(oauth2 -> oauth2
                        // Set the JWT authentication converter to extract authorities
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))
                )
                .build(); // Build the SecurityWebFilterChain
    }

    /**
     * Extracts authorities from JWT claims and wraps them in a reactive Mono.
     * This converter maps `roles` and `scopes` claims from the JWT to Spring Security's
     * `GrantedAuthority` objects.
     *
     * @return A Converter that extracts authorities from JWT claims, returning a Mono.
     */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        return jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // Extract roles from the "roles" claim (e.g., "ROLE_ADMIN", "ROLE_USER")
            // JwtService already adds "ROLE_" prefix. Spring Security expects it as is.
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                authorities.addAll(roles.stream()
                        .map(SimpleGrantedAuthority::new) // Use role string directly (e.g., "ROLE_ADMIN")
                        .collect(Collectors.toList()));
            }

            // Extract scopes from the "scope" claim (space-separated string) or "scopes" (list of strings).
            // JwtService provides "read", "write" (no SCOPE_ prefix).
            // Spring Security expects "SCOPE_read", "SCOPE_write".
            List<String> scopes = new ArrayList<>();
            String scopeString = jwt.getClaimAsString("scope"); // Try to get as single string
            if (scopeString != null) {
                scopes.addAll(List.of(scopeString.split(" ")));
            } else {
                List<String> scopesList = jwt.getClaimAsStringList("scopes"); // Try to get as list
                if (scopesList != null) {
                    scopes.addAll(scopesList);
                }
            }

            if (!scopes.isEmpty()) {
                authorities.addAll(scopes.stream()
                        .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope.toUpperCase())) // Add "SCOPE_" prefix here
                        .collect(Collectors.toList()));
            }

            // Log the extracted authorities for debugging purposes
            logger.debug("Extracted authorities for JWT: {}", authorities);

            // Return a Mono containing the JwtAuthenticationToken,
            // which represents the authenticated user with their extracted authorities.
            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        };
    }

    /**
     * Configures the ReactiveJwtDecoder.
     * This bean leverages Spring Security's built-in capabilities to fetch the public key
     * (or JWK Set URI) from the issuer configured in your application properties
     * (e.g., `spring.security.oauth2.resourceserver.jwt.issuer-uri`).
     *
     * @param issuerUri The issuer URI obtained from application properties.
     * @return A ReactiveJwtDecoder configured with the issuer URI.
     */
    @Bean
    ReactiveJwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        logger.info("Configuring ReactiveJwtDecoder with issuer-uri: {}", issuerUri);
        // ReactiveJwtDecoders.fromIssuerLocation automatically handles fetching JWK Set
        // and validating JWTs based on the provided issuer URI.
        return ReactiveJwtDecoders.fromIssuerLocation(issuerUri);
    }
}
