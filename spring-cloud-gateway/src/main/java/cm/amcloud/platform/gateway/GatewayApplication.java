package cm.amcloud.platform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication(exclude = {
    SecurityAutoConfiguration.class,
    ReactiveSecurityAutoConfiguration.class,
    OAuth2ClientAutoConfiguration.class,
    OAuth2ResourceServerAutoConfiguration.class
})
public class GatewayApplication {

    public static void main(String[] args) {

        // Load .env file
        Dotenv dotenv = Dotenv.configure()
                 .load();

        // Set environment variables
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        SpringApplication.run(GatewayApplication.class, args);
    }
}
