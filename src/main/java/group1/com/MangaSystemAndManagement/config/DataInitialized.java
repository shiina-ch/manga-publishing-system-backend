package group1.com.MangaSystemAndManagement.config;

import group1.com.MangaSystemAndManagement.model.SystemRole;
import group1.com.MangaSystemAndManagement.repository.SystemRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitialized implements CommandLineRunner {

    private final SystemRoleRepository systemRoleRepository;

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
}
