package group1.com.MangaSystemAndManagement.repository;

import group1.com.MangaSystemAndManagement.model.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Long> {
    Optional<ProjectRole> findByRoleNameIgnoreCase(String roleName);
    List<ProjectRole> findAllByRoleNameIgnoreCase(String roleName);
}
