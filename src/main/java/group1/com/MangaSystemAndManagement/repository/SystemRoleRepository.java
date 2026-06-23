package group1.com.MangaSystemAndManagement.repository;

import group1.com.MangaSystemAndManagement.model.SystemRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemRoleRepository extends JpaRepository<SystemRole, Long> {
    List<SystemRole> findAllByRoleNameIgnoreCase(String roleName);
}
