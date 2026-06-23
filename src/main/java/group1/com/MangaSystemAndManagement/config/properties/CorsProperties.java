package group1.com.MangaSystemAndManagement.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        allowedOrigins = normalize(allowedOrigins);
        if (allowedOrigins.isEmpty()) {
            throw new IllegalArgumentException("At least one CORS allowed origin must be configured");
        }
        allowedOrigins.forEach(CorsProperties::validateOrigin);
    }

    private static List<String> normalize(List<String> configuredOrigins) {
        if (configuredOrigins == null) {
            return List.of();
        }
        return configuredOrigins.stream()
                .filter(origin -> origin != null)
                .flatMap(origin -> Arrays.stream(origin.split(",")))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .distinct()
                .toList();
    }

    private static void validateOrigin(String origin) {
        if ("*".equals(origin)) {
            throw new IllegalArgumentException("Wildcard CORS origins are not allowed when credentials are enabled");
        }

        final URI uri;
        try {
            uri = URI.create(origin);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid CORS origin: " + origin, exception);
        }

        boolean validScheme = "http".equalsIgnoreCase(uri.getScheme())
                || "https".equalsIgnoreCase(uri.getScheme());
        boolean hasPath = uri.getRawPath() != null && !uri.getRawPath().isEmpty();
        if (!validScheme
                || uri.getHost() == null
                || uri.getUserInfo() != null
                || hasPath
                || uri.getRawQuery() != null
                || uri.getRawFragment() != null) {
            throw new IllegalArgumentException(
                    "CORS origin must be a complete HTTP(S) origin without a path: " + origin);
        }
    }
}
