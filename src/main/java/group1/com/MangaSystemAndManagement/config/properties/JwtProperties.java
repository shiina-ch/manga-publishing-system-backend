package group1.com.MangaSystemAndManagement.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Base64;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, long expiration) {

    private static final int MINIMUM_KEY_BYTES = 32;

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret must be configured as nonblank Base64 text");
        }
        if (expiration <= 0) {
            throw new IllegalArgumentException("JWT expiration must be a positive number of milliseconds");
        }
        decodeAndValidate(secret);
    }

    public byte[] decodedSecret() {
        return decodeAndValidate(secret);
    }

    private static byte[] decodeAndValidate(String secret) {
        final byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("JWT secret must be valid Base64 text", exception);
        }
        if (decoded.length < MINIMUM_KEY_BYTES) {
            throw new IllegalArgumentException("JWT secret must decode to at least 32 bytes for HS256");
        }
        return decoded;
    }
}
