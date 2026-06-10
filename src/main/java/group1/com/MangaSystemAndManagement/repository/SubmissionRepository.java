package group1.com.MangaSystemAndManagement.repository;
import group1.com.MangaSystemAndManagement.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
}
