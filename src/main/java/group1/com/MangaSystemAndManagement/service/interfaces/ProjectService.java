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

    /**
     * Assign a Tantō (Editor-in-charge) account to an existing Project.
     * @param projectId the Project to update
     * @param tantouId  the Account id of the Tantō to assign
     * @return the updated Project
     */
    Project assignTantou(Long projectId, Long tantouId);
}
