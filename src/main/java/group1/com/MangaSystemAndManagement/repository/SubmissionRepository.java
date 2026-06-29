package group1.com.MangaSystemAndManagement.repository;
import group1.com.MangaSystemAndManagement.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    @Query("SELECT s FROM Submission s LEFT JOIN FETCH s.submittedBy")
    List<Submission> findAllWithSubmittedBy();
}
