package group1.com.MangaSystemAndManagement.repository;

import group1.com.MangaSystemAndManagement.model.SystemRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemRoleRepository extends JpaRepository<SystemRole, Long> {
    SystemRole findByRoleName(String roleName);
}
