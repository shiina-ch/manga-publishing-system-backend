package group1.com.MangaSystemAndManagement.config;

import group1.com.MangaSystemAndManagement.config.properties.BootstrapAdminProperties;
import group1.com.MangaSystemAndManagement.model.SystemRole;
import group1.com.MangaSystemAndManagement.model.SystemRoleName;
import group1.com.MangaSystemAndManagement.model.AccountStatus;
import group1.com.MangaSystemAndManagement.repository.SystemRoleRepository;
import group1.com.MangaSystemAndManagement.service.impl.SystemRoleNormalizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import group1.com.MangaSystemAndManagement.model.Account;
import group1.com.MangaSystemAndManagement.repository.*;
import jakarta.annotation.Nullable;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitialized implements CommandLineRunner {

    private final SystemRoleRepository systemRoleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemRoleNormalizationService systemRoleNormalizationService;
    private final BootstrapAdminProperties bootstrapAdminProperties;

    static final List<SystemRoleName> DEFAULT_ROLES = List.of(SystemRoleName.values());

    @Override
    public void run(@Nullable String... args) {
        systemRoleNormalizationService.normalizeLegacyRoles();
        initRoles();
        if (bootstrapAdminProperties.enabled()) {
            initAdminAccount();
        } else {
            log.info("Bootstrap Admin creation is disabled");
        }
    }

    private void initRoles() {
        for (SystemRoleName roleName : DEFAULT_ROLES) {
            List<SystemRole> existingRoles = systemRoleRepository.findAllByRoleNameIgnoreCase(roleName.name());
            if (existingRoles.isEmpty()) {
                SystemRole role = new SystemRole();
                role.setRoleName(roleName.name());
                systemRoleRepository.save(role);
                log.info("Created role: {}", roleName);
            } else {
                log.info("Role already exists: {}", roleName);
            }
        }
    }

    private void initAdminAccount() {
        String adminEmail = bootstrapAdminProperties.email();
        List<SystemRole> adminRoles = systemRoleRepository.findAllByRoleNameIgnoreCase(SystemRoleName.ADMIN.name());
        if (adminRoles.isEmpty()) {
            log.warn("ADMIN role not found, skipping admin account creation.");
            return;
        }
        if (adminRoles.size() != 1) {
            throw new IllegalStateException(
                    "Expected exactly one canonical ADMIN role after normalization, found " + adminRoles.size());
        }
        SystemRole adminRole = adminRoles.get(0);

        if (accountRepository.findByEmail(adminEmail).isPresent()) {
            log.info("Admin account already exists: {}", adminEmail);
            return;
        }

        Account adminAccount = new Account();
        adminAccount.setFirstName("Admin");
        adminAccount.setLastName("System");
        // Placeholder metadata required by the current Account schema.
        adminAccount.setPhoneNumber("0123456789");
        adminAccount.setEmail(adminEmail);
        adminAccount.setPassword(passwordEncoder.encode(bootstrapAdminProperties.password()));
        adminAccount.setSystemRole(List.of(adminRole));
        adminAccount.setStatus(AccountStatus.ACTIVE);

        accountRepository.save(adminAccount);
        log.info("Created admin account: {}", adminEmail);
    }
}
