package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.dto.request.ProjectRequest;
import group1.com.MangaSystemAndManagement.model.Project;
import java.util.List;
import java.util.Optional;
public interface ProjectService {
    Project create(ProjectRequest request);
    Optional<Project> findById(Long id);
    List<Project> findAll();
    Project update(Long id, ProjectRequest request);
    void delete(Long id);
}
