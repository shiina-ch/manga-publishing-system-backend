package group1.com.MangaSystemAndManagement.repository;
import group1.com.MangaSystemAndManagement.model.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Long> {
}
