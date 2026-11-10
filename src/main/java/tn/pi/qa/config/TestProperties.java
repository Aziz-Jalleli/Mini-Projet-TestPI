package tn.pi.qa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "pi")
public record TestProperties(
        @DefaultValue("https://pi.tn") String baseUrl,
        @DefaultValue("chrome") String browser,
        @DefaultValue("false") boolean headless,
        @DefaultValue("15") int timeoutSeconds,
        @DefaultValue("10") int maxLoadSeconds,
        @DefaultValue("25") int maxLinksToCheck,
        @DefaultValue("800") int slowMotionMs) {
}
