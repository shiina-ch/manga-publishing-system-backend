package group1.com.MangaSystemAndManagement.repository;
import group1.com.MangaSystemAndManagement.model.SubmissionReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface SubmissionReviewRepository extends JpaRepository<SubmissionReview, Long> {
}
