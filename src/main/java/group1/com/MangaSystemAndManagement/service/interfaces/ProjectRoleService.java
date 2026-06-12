package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.dto.request.ProjectRoleRequest;
import group1.com.MangaSystemAndManagement.model.ProjectRole;
import java.util.List;
import java.util.Optional;
public interface ProjectRoleService {
    ProjectRole create(ProjectRoleRequest request);
    Optional<ProjectRole> findById(Long id);
    List<ProjectRole> findAll();
    ProjectRole update(Long id, ProjectRoleRequest request);
    void delete(Long id);
}
