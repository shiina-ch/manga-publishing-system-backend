package group1.com.MangaSystemAndManagement.model;

import java.util.Locale;

public enum AccountStatus {
    PENDING,
    ACTIVE,
    REJECTED,
    INACTIVE;

    public static AccountStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Account status is required");
        }

        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown account status: " + value, exception);
        }
    }

    public boolean matches(String value) {
        try {
            return this == from(value);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
