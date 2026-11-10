package tn.pi.qa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Spring Boot context that wires configuration (TestProperties) and the
 * WebDriver factory into the TestNG tests.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PiTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(PiTestApplication.class, args);
    }
}
