package group1.com.MangaSystemAndManagement.config;

import group1.com.MangaSystemAndManagement.config.properties.BootstrapAdminProperties;
import group1.com.MangaSystemAndManagement.config.properties.CorsProperties;
import group1.com.MangaSystemAndManagement.config.properties.JwtProperties;
import group1.com.MangaSystemAndManagement.config.properties.StorageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.unit.DataSize;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationPropertiesTest {

    private static final String VALID_SECRET = Base64.getEncoder().encodeToString(
            "This-is-a-test-only-HS256-signing-key".getBytes(StandardCharsets.UTF_8));

    @TempDir
    Path tempDirectory;

    @Test
    void rejectsMissingMalformedAndShortJwtSecrets() {
        assertThrows(IllegalArgumentException.class, () -> new JwtProperties(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new JwtProperties("not base64!", 1));
        String shortSecret = Base64.getEncoder().encodeToString(new byte[16]);
        assertThrows(IllegalArgumentException.class, () -> new JwtProperties(shortSecret, 1));
    }

    @Test
    void rejectsNonpositiveJwtExpirationAndAcceptsValidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new JwtProperties(VALID_SECRET, 0));
        JwtProperties properties = new JwtProperties(VALID_SECRET, 60_000);
        assertEquals(60_000, properties.expiration());
        assertTrue(properties.decodedSecret().length >= 32);
    }

    @Test
    void rejectsEmptyWildcardAndInvalidCorsOrigins() {
        assertThrows(IllegalArgumentException.class, () -> new CorsProperties(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new CorsProperties(List.of("*")));
        assertThrows(IllegalArgumentException.class, () -> new CorsProperties(List.of("localhost:5173")));
        assertThrows(IllegalArgumentException.class, () -> new CorsProperties(List.of("https://example.com/path")));
    }

    @Test
    void normalizesCommaSeparatedCorsOrigins() {
        CorsProperties properties = new CorsProperties(List.of(
                " http://localhost:5173, https://example.com ",
                "http://localhost:5173"));
        assertEquals(
                List.of("http://localhost:5173", "https://example.com"),
                properties.allowedOrigins());
    }

    @Test
    void validatesAndNormalizesStorageConfiguration() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new StorageProperties(" ", DataSize.ofMegabytes(1)));
        assertThrows(
                IllegalArgumentException.class,
                () -> new StorageProperties(tempDirectory.toString(), DataSize.ofBytes(0)));

        Path configured = tempDirectory.resolve("nested").resolve("..").resolve("uploads");
        StorageProperties properties = new StorageProperties(
                configured.toString(),
                DataSize.ofMegabytes(5));
        assertEquals(configured.toAbsolutePath().normalize(), properties.uploadPath());
        assertTrue(properties.uploadResourceLocation().startsWith("file:"));
        assertTrue(properties.uploadResourceLocation().endsWith("/"));
    }

    @Test
    void disabledBootstrapDoesNotRequireCredentials() {
        BootstrapAdminProperties properties = new BootstrapAdminProperties(false, "", "");
        assertFalse(properties.enabled());
    }

    @Test
    void enabledBootstrapRequiresValidEmailAndStrongPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new BootstrapAdminProperties(true, "", "StrongPassword1!"));
        assertThrows(
                IllegalArgumentException.class,
                () -> new BootstrapAdminProperties(true, "admin@example.com", ""));
        assertThrows(
                IllegalArgumentException.class,
                () -> new BootstrapAdminProperties(true, "invalid", "StrongPassword1!"));
        assertThrows(
                IllegalArgumentException.class,
                () -> new BootstrapAdminProperties(true, "admin@example.com", "weak"));

        BootstrapAdminProperties valid = new BootstrapAdminProperties(
                true,
                " admin@example.com ",
                "StrongPassword1!");
        assertEquals("admin@example.com", valid.email());
    }
}
