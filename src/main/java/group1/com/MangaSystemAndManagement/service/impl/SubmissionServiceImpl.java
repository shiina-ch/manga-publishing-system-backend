package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.model.Submission;
import group1.com.MangaSystemAndManagement.repository.SubmissionRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {
    private final SubmissionRepository repository;
    @Override
    @Transactional
    public Submission create(Submission entity) {
        return repository.save(entity);
    }
    @Override
    public Optional<Submission> findById(Long id) {
        return repository.findById(id);
    }
    @Override
    public List<Submission> findAll() {
        return repository.findAll();
    }
    @Override
    @Transactional
    public Submission update(Long id, Submission entity) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Submission not found with id " + id);
        }
        entity.setId(id);
        return repository.save(entity);
    }
    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Submission not found with id " + id);
        }
        repository.deleteById(id);
    }
}
