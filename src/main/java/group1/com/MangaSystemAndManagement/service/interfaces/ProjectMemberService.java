package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.model.ProjectMember;
import group1.com.MangaSystemAndManagement.model.ProjectMemberId;
import java.util.List;
import java.util.Optional;
public interface ProjectMemberService {
    ProjectMember create(ProjectMember entity);
    Optional<ProjectMember> findById(ProjectMemberId id);
    List<ProjectMember> findAll();
    ProjectMember update(ProjectMemberId id, ProjectMember entity);
    void delete(ProjectMemberId id);
}
