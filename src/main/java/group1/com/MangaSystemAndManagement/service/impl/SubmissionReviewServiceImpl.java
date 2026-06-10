package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.model.SubmissionReview;
import group1.com.MangaSystemAndManagement.repository.SubmissionReviewRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.SubmissionReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class SubmissionReviewServiceImpl implements SubmissionReviewService {
    private final SubmissionReviewRepository repository;
    @Override
    @Transactional
    public SubmissionReview create(SubmissionReview entity) {
        return repository.save(entity);
    }
    @Override
    public Optional<SubmissionReview> findById(Long id) {
        return repository.findById(id);
    }
    @Override
    public List<SubmissionReview> findAll() {
        return repository.findAll();
    }
    @Override
    @Transactional
    public SubmissionReview update(Long id, SubmissionReview entity) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("SubmissionReview not found with id " + id);
        }
        entity.setId(id);
        return repository.save(entity);
    }
    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("SubmissionReview not found with id " + id);
        }
        repository.deleteById(id);
    }
}
