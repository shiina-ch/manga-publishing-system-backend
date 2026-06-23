package group1.com.MangaSystemAndManagement.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemRoleNameTest {

    @Test
    void parsesCanonicalRoleCaseInsensitivelyAndTrimsWhitespace() {
        assertEquals(SystemRoleName.MANGAKA, SystemRoleName.from("  mangaka "));
    }

    @Test
    void mapsLegacyTantorAlias() {
        assertEquals(SystemRoleName.TANTOU_EDITOR, SystemRoleName.from(" tantor "));
    }

    @Test
    void mapsLegacyEditorAlias() {
        assertEquals(SystemRoleName.EDITORIAL_BOARD_MEMBER, SystemRoleName.from("editor"));
    }

    @Test
    void rejectsUnknownRole() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SystemRoleName.from("LEAD_MANGAKA")
        );
        assertTrue(exception.getMessage().contains("Unknown system role"));
    }

    @Test
    void accountExposesCanonicalDistinctAuthoritiesFromLegacyData() {
        Account account = new Account();
        account.setSystemRole(List.of(role("TANTOR"), role("tantou_editor"), role("EDITOR")));

        assertEquals(
                List.of("TANTOU_EDITOR", "EDITORIAL_BOARD_MEMBER"),
                account.getAuthorities().stream().map(authority -> authority.getAuthority()).toList()
        );
    }

    @Test
    void onlyActiveAccountIsEnabled() {
        Account account = new Account();
        account.setStatus(AccountStatus.ACTIVE);
        assertTrue(account.isEnabled());

        for (AccountStatus disabledStatus : List.of(
                AccountStatus.PENDING,
                AccountStatus.REJECTED,
                AccountStatus.INACTIVE)) {
            account.setStatus(disabledStatus);
            assertFalse(account.isEnabled());
        }

        account.setStatus((String) null);
        assertFalse(account.isEnabled());
        account.setStatus("UNKNOWN_LEGACY_STATUS");
        assertFalse(account.isEnabled());
    }

    @Test
    void newAccountDefaultsToPendingAndDisabled() {
        Account account = new Account();

        assertEquals(AccountStatus.PENDING.name(), account.getStatus());
        assertFalse(account.isEnabled());
    }

    @Test
    void invalidPersistedRolesDoNotGrantAuthorities() {
        Account account = new Account();
        account.setSystemRole(Arrays.asList(role(null), role("  "), role("UNKNOWN")));

        assertTrue(account.getAuthorities().isEmpty());
    }

    @Test
    void validAuthoritiesSurviveMixedInvalidPersistedRoles() {
        Account account = new Account();
        account.setSystemRole(Arrays.asList(
                null,
                role(null),
                role("UNKNOWN"),
                role(" tantor "),
                role("MANGAKA")));

        assertEquals(
                List.of("TANTOU_EDITOR", "MANGAKA"),
                account.getAuthorities().stream().map(authority -> authority.getAuthority()).toList()
        );
    }

    private SystemRole role(String roleName) {
        SystemRole role = new SystemRole();
        role.setRoleName(roleName);
        return role;
    }
}
