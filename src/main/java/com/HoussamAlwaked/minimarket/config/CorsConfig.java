package com.HoussamAlwaked.minimarket.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Set this in Railway/Render/etc:
     * CORS_ALLOWED_ORIGINS=https://your-frontend.com,http://localhost:8081,http://localhost:5173
     */
    private static final List<String> DEFAULT_ALLOWED_ORIGINS = List.of(
            "http://localhost:8081", // Expo web dev server (common)
            "http://localhost:5173"  // Vite dev server (common)
    );

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> allowedOrigins = resolveAllowedOrigins();

        registry.addMapping("/**")
                // Use allowedOrigins (explicit list). Do NOT use "*" in production.
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                // Allow all typical REST methods + preflight
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                // Allow headers needed for JSON + your API key header
                .allowedHeaders("Content-Type", "Authorization", "X-API-KEY", "X-USER-ID", "X-USER-EMAIL",
                        "Accept", "Origin")
                // If you want the browser to be able to read certain response headers, expose them here
                .exposedHeaders("Location")
                // If you are NOT using cookies/sessions cross-site, keep this false
                .allowCredentials(false)
                // Cache preflight response for 1 hour
                .maxAge(3600);
    }

    private List<String> resolveAllowedOrigins() {
        String originsEnv = System.getenv("CORS_ALLOWED_ORIGINS");

        if (originsEnv == null || originsEnv.isBlank()) {
            return DEFAULT_ALLOWED_ORIGINS;
        }

        return Arrays.stream(originsEnv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }
}
