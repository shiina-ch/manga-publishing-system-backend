package group1.com.MangaSystemAndManagement.repository;
import group1.com.MangaSystemAndManagement.model.ProjectMember;
import group1.com.MangaSystemAndManagement.model.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
}
