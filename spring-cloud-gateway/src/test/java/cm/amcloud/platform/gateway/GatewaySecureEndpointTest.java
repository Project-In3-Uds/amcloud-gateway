package cm.amcloud.platform.gateway;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GatewaySecureEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testAccessSecureIamEndpoint() {
        // Step 1: Fetch a valid JWT from the IAM service
        String loginUrl = "http://localhost:8081/auth/login";
        Map<String, String> loginRequest = Map.of("username", "admin", "password", "adminpass");
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(loginUrl, loginRequest, Map.class);

        String token = (String) loginResponse.getBody().get("token"); // Ensure token includes required scopes

        // Step 2: Use the fetched JWT to access the secure endpoint
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        String secureEndpointUrl = "http://localhost:8081/secure-data";

        ResponseEntity<String> response = restTemplate.getForEntity(secureEndpointUrl, String.class, headers);

        // Step 3: Assert the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("This is secure data from IAM!", response.getBody());
    }
}
