package cm.amcloud.platform.gateway.filter;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * AuthHeaderForwardFilter: A global filter that extracts JWT claims (subject, roles, scopes)
 * after successful authentication by Spring Security and forwards them as custom HTTP headers
 * to downstream microservices. This ensures that the 'token' object is available.
 */
@Component
public class AuthHeaderForwardFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(AuthHeaderForwardFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Retrieve the security context from the ReactiveSecurityContextHolder
        return ReactiveSecurityContextHolder.getContext()
                .filter(context -> context.getAuthentication() != null)
                .map(context -> context.getAuthentication())
                .flatMap(authentication -> {
                    // Check if the authentication is a JwtAuthenticationToken
                    if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
                        Jwt jwt = jwtAuthenticationToken.getToken();
                        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();

                        // Add X-User-ID header with the subject of the JWT
                        String userId = jwt.getSubject();
                        if (userId != null) {
                            requestBuilder.header("X-User-ID", userId);
                            logger.debug("Added X-User-ID: {}", userId);
                        }

                        // Add X-User-Roles header with the roles from the JWT
                        // Roles are already "ROLE_ADMIN" from JwtService and SecurityConfig.
                        // We want to keep the "ROLE_" prefix in the header.
                        List<String> roles = authentication.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .filter(authority -> authority.startsWith("ROLE_")) // Filter for roles
                                .collect(Collectors.toList());
                        if (!roles.isEmpty()) {
                            requestBuilder.header("X-User-Roles", String.join(",", roles)); // Keep "ROLE_" prefix
                            logger.debug("Added X-User-Roles: {}", roles);
                        }

                        // Add X-User-Scopes header with the scopes from the JWT
                        // Scopes are "SCOPE_READ" from SecurityConfig.
                        // We want to remove the "SCOPE_" prefix for the header.
                        List<String> scopes = authentication.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .filter(authority -> authority.startsWith("SCOPE_")) // Filter for scopes
                                .map(scope -> scope.substring(6)) // Remove "SCOPE_" prefix here
                                .collect(Collectors.toList());
                        if (!scopes.isEmpty()) {
                            requestBuilder.header("X-User-Scopes", String.join(",", scopes)); // Send without "SCOPE_" prefix
                            logger.debug("Added X-User-Scopes: {}", scopes);
                        }

                        // Build the new request with added headers
                        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
                    }
                    // If not a JwtAuthenticationToken, or no authentication, proceed without adding headers
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange)); // If no authentication context, proceed
    }

    /**
     * Defines the order of this filter.
     * It should run after Spring Security's authentication filters.
     * A high order value means it runs later.
     * Spring Security's filters typically run at lower orders (e.g., -100 to 0).
     */
    @Override
    public int getOrder() {
        // A more typical positive order if you want it to run after default security filters.
        // This ensures Spring Security has processed the token and populated the context.
        return 100;
    }
}
