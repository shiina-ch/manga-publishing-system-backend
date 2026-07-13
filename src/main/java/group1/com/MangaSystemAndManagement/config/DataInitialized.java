package group1.com.MangaSystemAndManagement.config;

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
import group1.com.MangaSystemAndManagement.model.ProjectRole;
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

    static final List<SystemRoleName> DEFAULT_ROLES = List.of(SystemRoleName.values());

    @Override
    public void run(@Nullable String... args) {
        systemRoleNormalizationService.normalizeLegacyRoles();
        initRoles();
        initAdminAccount();
        initBoardMembers();
        initWorkflowMembers();
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
        String adminEmail = "admin@gmail.com";
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
        adminAccount.setPassword(passwordEncoder.encode("admin123"));
        adminAccount.setSystemRole(List.of(adminRole));
        adminAccount.setStatus(AccountStatus.ACTIVE);

        accountRepository.save(adminAccount);
        log.info("Created admin account: {}", adminEmail);
    }

    private void initBoardMembers() {
        List<SystemRole> boardRoles = systemRoleRepository
                .findAllByRoleNameIgnoreCase(SystemRoleName.EDITORIAL_BOARD_MEMBER.name());
        if (boardRoles.isEmpty()) {
            log.warn("EDITORIAL_BOARD_MEMBER role not found, skipping board members creation.");
            return;
        }
        SystemRole boardRole = boardRoles.get(0);

        for (int i = 1; i <= 3; i++) {
            String email = "board" + i + "@manga.com";
            if (accountRepository.findByEmail(email).isEmpty()) {
                Account boardAccount = new Account();
                boardAccount.setFirstName("Board");
                boardAccount.setLastName("Member " + i);
                boardAccount.setPhoneNumber("098765432" + i);
                boardAccount.setEmail(email);
                boardAccount.setPassword(passwordEncoder.encode("password123"));
                boardAccount.setSystemRole(List.of(boardRole));
                boardAccount.setStatus(AccountStatus.ACTIVE);

                accountRepository.save(boardAccount);
                log.info("Created board member account: {}", email);
            }
        }
    }

    private void initWorkflowMembers() {
        // 1. Create Tantou
        List<SystemRole> tantouRoles = systemRoleRepository
                .findAllByRoleNameIgnoreCase(SystemRoleName.TANTOU_EDITOR.name());
        if (!tantouRoles.isEmpty()) {
            String email = "tantou@manga.com";
            if (accountRepository.findByEmail(email).isEmpty()) {
                Account account = new Account();
                account.setFirstName("Tantou");
                account.setLastName("Editor");
                account.setPhoneNumber("0900000001");
                account.setEmail(email);
                account.setPassword(passwordEncoder.encode("password123"));
                account.setSystemRole(List.of(tantouRoles.get(0)));
                account.setStatus(AccountStatus.ACTIVE);
                accountRepository.save(account);
                log.info("Created Tantou account: {}", email);
            }
        }

        // 2. Create Mangaka
        List<SystemRole> mangakaRoles = systemRoleRepository.findAllByRoleNameIgnoreCase(SystemRoleName.MANGAKA.name());
        if (!mangakaRoles.isEmpty()) {
            String email = "mangaka@manga.com";
            if (accountRepository.findByEmail(email).isEmpty()) {
                Account account = new Account();
                account.setFirstName("Master");
                account.setLastName("Mangaka");
                account.setPhoneNumber("0900000002");
                account.setEmail(email);
                account.setPassword(passwordEncoder.encode("password123"));
                account.setSystemRole(List.of(mangakaRoles.get(0)));
                account.setStatus(AccountStatus.ACTIVE);
                accountRepository.save(account);
                log.info("Created Mangaka account: {}", email);
            }
        }

        // 3. Create Assistants
        List<SystemRole> assistantRoles = systemRoleRepository
                .findAllByRoleNameIgnoreCase(SystemRoleName.ASSISTANT.name());
        if (!assistantRoles.isEmpty()) {
            for (int i = 1; i <= 3; i++) {
                String email = "assistant" + i + "@manga.com";
                if (accountRepository.findByEmail(email).isEmpty()) {
                    Account account = new Account();
                    account.setFirstName("Assistant");
                    account.setLastName("Number " + i);
                    account.setPhoneNumber("090000001" + i);
                    account.setEmail(email);
                    account.setPassword(passwordEncoder.encode("password123"));
                    account.setSystemRole(List.of(assistantRoles.get(0)));
                    account.setStatus(AccountStatus.ACTIVE);
                    accountRepository.save(account);
                    log.info("Created Assistant account: {}", email);
                }
            }
        }
    }
}
