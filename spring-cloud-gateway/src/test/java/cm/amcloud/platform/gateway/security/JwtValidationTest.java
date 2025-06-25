package cm.amcloud.platform.gateway.security;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
public class JwtValidationTest {

    @Value("http://localhost:8081/jwks.json")
    private String jwkSetUri;

    @Value("http://localhost:8081")
    private String issuerUri;

    @Test
    public void testFetchJwksAndValidateToken() {
        // Fetch the JWKS from the IAM service
        WebClient webClient = WebClient.create();
        Map<String, Object> jwks = webClient.get()
                .uri(jwkSetUri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // Assert that the JWKS contains keys
        assertTrue(jwks.containsKey("keys"), "JWKS does not contain keys");

        // Simulate token validation (you can expand this with a real token validation library)
        System.out.println("JWKS fetched successfully: " + jwks);
    }
}
