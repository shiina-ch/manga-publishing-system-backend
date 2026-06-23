package group1.com.MangaSystemAndManagement.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "app.bootstrap-admin")
public record BootstrapAdminProperties(boolean enabled, String email, String password) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$");

    public BootstrapAdminProperties {
        email = email == null ? "" : email.trim();
        password = password == null ? "" : password;

        if (enabled) {
            if (email.isBlank()) {
                throw new IllegalArgumentException("Bootstrap Admin email is required when bootstrap is enabled");
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalArgumentException("Bootstrap Admin email must be a valid email address");
            }
            if (password.isBlank()) {
                throw new IllegalArgumentException("Bootstrap Admin password is required when bootstrap is enabled");
            }
            if (!PASSWORD_PATTERN.matcher(password).matches()) {
                throw new IllegalArgumentException(
                        "Bootstrap Admin password must contain uppercase, lowercase, digit, and special characters");
            }
        }
    }
}
