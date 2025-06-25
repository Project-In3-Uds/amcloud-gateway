package cm.amcloud.platform.gateway.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@SpringBootTest
class SecurityConfigTest {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Test
    void testJwtDecoder() {
        // Arrange
        SecurityConfig securityConfig = new SecurityConfig();

        // Act
        ReactiveJwtDecoder jwtDecoder = securityConfig.jwtDecoder(issuerUri);

        // Assert
        assertNotNull(jwtDecoder);
    }
}
