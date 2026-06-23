package group1.com.MangaSystemAndManagement.config;

import group1.com.MangaSystemAndManagement.model.SystemRoleName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import group1.com.MangaSystemAndManagement.model.Account;
import group1.com.MangaSystemAndManagement.model.AccountStatus;
import group1.com.MangaSystemAndManagement.model.SystemRole;
import group1.com.MangaSystemAndManagement.repository.AccountRepository;
import group1.com.MangaSystemAndManagement.repository.SystemRoleRepository;
import group1.com.MangaSystemAndManagement.service.impl.SystemRoleNormalizationService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializedTest {

    @Mock
    private SystemRoleRepository systemRoleRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SystemRoleNormalizationService normalizationService;

    @Test
    void bootstrapRoleListContainsExactlyCanonicalRoles() {
        assertEquals(Set.of(SystemRoleName.values()), Set.copyOf(DataInitialized.DEFAULT_ROLES));
        assertEquals(SystemRoleName.values().length, DataInitialized.DEFAULT_ROLES.size());
    }

    @Test
    void bootstrapAdminIsExplicitlyActive() {
        SystemRole adminRole = role(1L, SystemRoleName.ADMIN.name());
        for (SystemRoleName roleName : SystemRoleName.values()) {
            when(systemRoleRepository.findAllByRoleNameIgnoreCase(roleName.name()))
                    .thenReturn(List.of(roleName == SystemRoleName.ADMIN
                            ? adminRole
                            : role(roleName.ordinal() + 2L, roleName.name())));
        }
        when(accountRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin123")).thenReturn("encoded");

        initializer().run();

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertEquals(AccountStatus.ACTIVE.name(), captor.getValue().getStatus());
        assertTrue(captor.getValue().isEnabled());
    }

    @Test
    void duplicateAdminRowsAfterNormalizationFailClearly() {
        SystemRole first = role(1L, SystemRoleName.ADMIN.name());
        SystemRole second = role(2L, SystemRoleName.ADMIN.name());
        when(systemRoleRepository.findAllByRoleNameIgnoreCase(SystemRoleName.ADMIN.name()))
                .thenReturn(List.of(first, second));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> initializer().run()
        );

        assertTrue(exception.getMessage().contains("exactly one canonical ADMIN"));
    }

    private DataInitialized initializer() {
        return new DataInitialized(
                systemRoleRepository,
                accountRepository,
                passwordEncoder,
                normalizationService);
    }

    private SystemRole role(long id, String name) {
        SystemRole role = new SystemRole();
        role.setId(id);
        role.setRoleName(name);
        return role;
    }
}
