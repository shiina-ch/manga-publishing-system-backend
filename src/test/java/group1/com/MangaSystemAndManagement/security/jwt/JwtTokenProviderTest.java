package group1.com.MangaSystemAndManagement.security.jwt;

import group1.com.MangaSystemAndManagement.model.Account;
import group1.com.MangaSystemAndManagement.model.SystemRole;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JwtTokenProviderTest {

    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(
            "This-is-a-test-only-HS256-signing-key".getBytes(StandardCharsets.UTF_8));

    @Test
    void generatedTokenContainsCanonicalDistinctRoleClaims() {
        JwtTokenProvider provider = provider();
        Account account = accountWithRoles("TANTOR", "TANTOU_EDITOR", "EDITOR");

        String token = provider.generateToken(account);

        assertEquals(
                List.of("TANTOU_EDITOR", "EDITORIAL_BOARD_MEMBER"),
                provider.extractClaim(token, claims -> claims.get("roles", List.class))
        );
    }

    @Test
    void tokenIsInvalidAfterAccountBecomesDisabled() {
        JwtTokenProvider provider = provider();
        Account account = accountWithRoles("MANGAKA");
        String token = provider.generateToken(account);
        account.setStatus("INACTIVE");

        assertFalse(provider.isTokenValid(token, account));
    }

    private JwtTokenProvider provider() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(provider, "jwtExpiration", 60_000L);
        return provider;
    }

    private Account accountWithRoles(String... roleNames) {
        Account account = new Account();
        account.setId(42L);
        account.setEmail("account@example.com");
        account.setSystemRole(java.util.Arrays.stream(roleNames).map(this::role).toList());
        return account;
    }

    private SystemRole role(String name) {
        SystemRole role = new SystemRole();
        role.setRoleName(name);
        return role;
    }
}
