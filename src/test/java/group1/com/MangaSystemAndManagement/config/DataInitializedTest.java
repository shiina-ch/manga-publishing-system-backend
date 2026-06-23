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
import group1.com.MangaSystemAndManagement.config.properties.BootstrapAdminProperties;

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
        when(accountRepository.findByEmail("bootstrap@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("StrongPassword1!")).thenReturn("encoded");

        initializer(enabledBootstrap()).run();

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
                () -> initializer(enabledBootstrap()).run()
        );

        assertTrue(exception.getMessage().contains("exactly one canonical ADMIN"));
    }

    @Test
    void disabledBootstrapStillNormalizesAndSeedsRoles() {
        initializer(new BootstrapAdminProperties(false, "", "")).run();

        verify(normalizationService).normalizeLegacyRoles();
        for (SystemRoleName roleName : SystemRoleName.values()) {
            verify(systemRoleRepository).findAllByRoleNameIgnoreCase(roleName.name());
        }
        org.mockito.Mockito.verifyNoInteractions(passwordEncoder);
        org.mockito.Mockito.verifyNoInteractions(accountRepository);
    }

    @Test
    void existingBootstrapAdminIsNotOverwritten() {
        SystemRole adminRole = role(1L, SystemRoleName.ADMIN.name());
        when(systemRoleRepository.findAllByRoleNameIgnoreCase(SystemRoleName.ADMIN.name()))
                .thenReturn(List.of(adminRole));
        when(accountRepository.findByEmail("bootstrap@example.com"))
                .thenReturn(Optional.of(new Account()));

        initializer(enabledBootstrap()).run();

        org.mockito.Mockito.verify(accountRepository, org.mockito.Mockito.never())
                .save(org.mockito.ArgumentMatchers.any());
        org.mockito.Mockito.verifyNoInteractions(passwordEncoder);
    }

    private DataInitialized initializer(BootstrapAdminProperties properties) {
        return new DataInitialized(
                systemRoleRepository,
                accountRepository,
                passwordEncoder,
                normalizationService,
                properties);
    }

    private BootstrapAdminProperties enabledBootstrap() {
        return new BootstrapAdminProperties(
                true,
                "bootstrap@example.com",
                "StrongPassword1!");
    }

    private SystemRole role(long id, String name) {
        SystemRole role = new SystemRole();
        role.setId(id);
        role.setRoleName(name);
        return role;
    }
}
