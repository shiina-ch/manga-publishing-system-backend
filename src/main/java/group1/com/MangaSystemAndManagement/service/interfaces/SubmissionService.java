package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.dto.request.SubmissionRequest;
import group1.com.MangaSystemAndManagement.model.Submission;
import java.util.List;
import java.util.Optional;
public interface SubmissionService {
    Submission create(SubmissionRequest request);
    Optional<Submission> findById(Long id);
    List<Submission> findAll();
    Submission update(Long id, SubmissionRequest request);
    void delete(Long id);
    Submission approveAndCreateProject(Long submissionId);
}
