package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.dto.request.ProjectRoleRequest;
import group1.com.MangaSystemAndManagement.model.ProjectRole;
import group1.com.MangaSystemAndManagement.repository.ProjectRoleRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.ProjectRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ProjectRoleServiceImpl implements ProjectRoleService {
    private final ProjectRoleRepository repository;
    @Override
    @Transactional
    public ProjectRole create(ProjectRoleRequest request) {
        ProjectRole entity = new ProjectRole();
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
        return repository.save(entity);
    }
    @Override
    public Optional<ProjectRole> findById(Long id) {
        return repository.findById(id);
    }
    @Override
    public List<ProjectRole> findAll() {
        return repository.findAll();
    }
    @Override
    @Transactional
    public ProjectRole update(Long id, ProjectRoleRequest request) {
        ProjectRole entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("ProjectRole not found with id " + id));
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
        return repository.save(entity);
    }
    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("ProjectRole not found with id " + id);
        }
        repository.deleteById(id);
    }
}
