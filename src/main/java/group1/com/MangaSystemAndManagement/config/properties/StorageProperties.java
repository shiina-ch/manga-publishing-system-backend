package group1.com.MangaSystemAndManagement.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(String uploadDirectory, DataSize maxSize) {

    public StorageProperties {
        if (uploadDirectory == null || uploadDirectory.isBlank()) {
            throw new IllegalArgumentException("Upload directory must be configured");
        }
        uploadDirectory = uploadDirectory.trim();
        if (maxSize == null || maxSize.toBytes() <= 0) {
            throw new IllegalArgumentException("Upload max size must be positive");
        }
    }

    public Path uploadPath() {
        return Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public String uploadResourceLocation() {
        String location = uploadPath().toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }
}
