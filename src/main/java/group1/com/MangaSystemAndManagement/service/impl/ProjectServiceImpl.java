package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.dto.request.ProjectRequest;
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
    public Project create(ProjectRequest request) {
        Project entity = new Project();
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
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
    public Project update(Long id, ProjectRequest request) {
        Project entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Project not found with id " + id));
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
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
