package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.model.Project;
import group1.com.MangaSystemAndManagement.repository.ProjectRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository repository;
    @Override
    @Transactional
    public Project create(Project entity) {
        return repository.save(entity);
    }
    @Override
    public Optional<Project> findById(Long id) {
        return repository.findById(id);
    }
    @Override
    public List<Project> findAll() {
        return repository.findAll();
    }
    @Override
    @Transactional
    public Project update(Long id, Project entity) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Project not found with id " + id);
        }
        entity.setId(id);
        return repository.save(entity);
    }
    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Project not found with id " + id);
        }
        repository.deleteById(id);
    }
}
