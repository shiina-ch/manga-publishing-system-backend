package group1.com.MangaSystemAndManagement.config;

import group1.com.MangaSystemAndManagement.model.SystemRole;
import group1.com.MangaSystemAndManagement.repository.SystemRoleRepository;
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

    private static final List<String> DEFAULT_ROLES = List.of(
            "MANGAKA",
            "ASSISTANT",
            "TANTOR",
            "EDITOR",
            "ADMIN",
            "MANAGER");

    @Override
    public void run(@Nullable String... args) {
        initRoles();
        initAdminAccount();
    }

    private void initRoles() {
        for (String roleName : DEFAULT_ROLES) {
            SystemRole existingRole = systemRoleRepository.findByRoleName(roleName);
            if (existingRole == null) {
                SystemRole role = new SystemRole();
                role.setRoleName(roleName);
                systemRoleRepository.save(role);
                log.info("Created role: {}", roleName);
            } else {
                log.info("Role already exists: {}", roleName);
            }
        }
    }

    private void initAdminAccount() {
        String adminEmail = "admin@gmail.com";
        if (accountRepository.findByEmail(adminEmail).isPresent()) {
            log.info("Admin account already exists: {}", adminEmail);
            return;
        }

        SystemRole adminRole = systemRoleRepository.findByRoleName("ADMIN");
        if (adminRole == null) {
            log.warn("ADMIN role not found, skipping admin account creation.");
            return;
        }

        Account adminAccount = new Account();
        adminAccount.setFirstName("Admin");
        adminAccount.setLastName("System");
        adminAccount.setPhoneNumber("0123456789");
        adminAccount.setEmail(adminEmail);
        adminAccount.setPassword(passwordEncoder.encode("admin123"));
        adminAccount.setAddress("123 Admin Street");
        adminAccount.setSystemRole(List.of(adminRole));

        accountRepository.save(adminAccount);
        log.info("Created admin account: {}", adminEmail);
    }
}
