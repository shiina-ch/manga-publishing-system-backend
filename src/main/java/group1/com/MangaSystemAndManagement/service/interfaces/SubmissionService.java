package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.model.Submission;
import java.util.List;
import java.util.Optional;
public interface SubmissionService {
    Submission create(Submission entity);
    Optional<Submission> findById(Long id);
    List<Submission> findAll();
    Submission update(Long id, Submission entity);
    void delete(Long id);
}
