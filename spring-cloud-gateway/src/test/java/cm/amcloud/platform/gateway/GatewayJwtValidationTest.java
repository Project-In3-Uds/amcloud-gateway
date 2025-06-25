package cm.amcloud.platform.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    // Explicitly set the issuer-uri for the test context.
    // This value MUST match the 'iam.issuer-uri' configured in your IAM service.
    // Assuming IAM service runs on port 8081 for testing purposes.
    properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081"
    })
public class GatewayJwtValidationTest {

    @LocalServerPort
    private int port;

    // Injects the issuer URI from the test application context properties.
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    // Autowires TestRestTemplate for making HTTP requests in the test.
    @Autowired
    private TestRestTemplate restTemplate;

    // --- CRITICAL STEP: REPLACE THIS WITH A REAL, UNEXPIRED JWT ---
    // 1. Ensure your IAM service is running on http://localhost:8081.
    // 2. Authenticate with your IAM service to obtain a fresh JWT.
    // 3. Paste that ENTIRE JWT string here.
    //    The current token is a placeholder with a dummy signature and may be expired.
    //    Its payload: {"iss":"http://localhost:8081","sub":"admin","iat":1748500000,"exp":1748503600}
    private static String validJwt = "{eyJraWQiOiJteS1rZXktaWQiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODEiLCJzdWIiOiJhZG1pbiIsImlhdCI6MTc0ODQ5OTI2MSwiZXhwIjoxNzQ4NTAyODYxfQ.SKtr406GEmdvlIVvT8MoGoBjS5PE3CASctjzSbmla99rVqHGogt5ag2YVPCrUyV6ltlyRyC7r3ynbmCU_tYW5IKUxkwsszoRQu_G_ITZ3Nka4N_q_-XgTS0HWKuI-1MwP56s6vVaZDSEo5ee8286Q8hZhKQnIhvUBZ6sU8_3wuinButq35Q3gPaPZkPF1rwjtCjVW66D7VpqnR2bGfXGOdabH3K9wECYFSgZHjG40CRXYKr8Jt585MqBIG3vBFIJc4qfKn-OwHQTlC4idl7rcB2RB84Gl0M50D3P8KKCouwJMEgKxScee7CYPIwJ2xNeCtcDXtnENdWp4ManeiIwSA";

    /**
     * Tests access to a secured endpoint with a valid JWT.
     * This simulates a client sending a request with a token issued by the IAM service.
     */
    @Test
    public void testAccessSecuredEndpointWithValidJwt() {
        System.out.println("Testing access to secured endpoint with a valid JWT.");
        System.out.println("Using issuer-uri for tests: " + issuerUri);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + validJwt);

        // Define the URL for the secured endpoint.
        // This assumes "/billing/" is a secured path configured in your gateway's SecurityConfig.
        String url = "http://localhost:" + port + "/billing/";

        // Make the POST request with the JWT in the Authorization header.
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.POST, // Adjust HTTP method if your endpoint is GET/PUT/DELETE
                new org.springframework.http.HttpEntity<>(null, headers), // Request body is null for this example
                String.class
        );

        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());

        // Assert that the gateway allows access (returns a 2xx status code).
        // Adjust HttpStatus.OK if your successful response is a different 2xx code.
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Tests access to a secured endpoint without any JWT.
     * This verifies that the gateway correctly rejects unauthenticated requests.
     */
    @Test
    public void testAccessSecuredEndpointWithoutJwt() {
        System.out.println("Testing access to secured endpoint without a JWT.");
        System.out.println("Using issuer-uri for tests: " + issuerUri);

        // Define the URL for the secured endpoint.
        String url = "http://localhost:" + port + "/billing/";

        // Make a GET request without any Authorization header.
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        System.out.println("Response status: " + response.getStatusCode());

        // Assert that the gateway rejects the request with 401 Unauthorized.
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Tests access to a public endpoint.
     * This verifies that the gateway allows access to paths configured as permitAll().
     */
    @Test
    public void testAccessPublicEndpoint() {
        System.out.println("Testing access to a public endpoint.");
        System.out.println("Using issuer-uri for tests: " + issuerUri);

        // Define the URL for a public endpoint.
        // This assumes "/auth/public" is a public path configured in your gateway's SecurityConfig
        // and is handled by GatewayTestController.
        String url = "http://localhost:" + port + "/auth/public";

        // Make a GET request to the public endpoint.
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        System.out.println("Response status: " + response.getStatusCode());

        // Assert that the gateway allows access (returns 200 OK or other success code).
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
