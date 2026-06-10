package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.model.SubmissionReview;
import java.util.List;
import java.util.Optional;
public interface SubmissionReviewService {
    SubmissionReview create(SubmissionReview entity);
    Optional<SubmissionReview> findById(Long id);
    List<SubmissionReview> findAll();
    SubmissionReview update(Long id, SubmissionReview entity);
    void delete(Long id);
}
