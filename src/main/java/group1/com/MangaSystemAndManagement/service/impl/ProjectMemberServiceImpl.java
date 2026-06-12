package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.dto.request.ProjectMemberRequest;
import group1.com.MangaSystemAndManagement.model.ProjectMember;
import group1.com.MangaSystemAndManagement.model.ProjectMemberId;
import group1.com.MangaSystemAndManagement.repository.ProjectMemberRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {
    private final ProjectMemberRepository repository;
    @Override
    @Transactional
    public ProjectMember create(ProjectMemberRequest request) {
        ProjectMember entity = new ProjectMember();
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
        return repository.save(entity);
    }
    @Override
    public Optional<ProjectMember> findById(ProjectMemberId id) {
        return repository.findById(id);
    }
    @Override
    public List<ProjectMember> findAll() {
        return repository.findAll();
    }
    @Override
    @Transactional
    public ProjectMember update(ProjectMemberId id, ProjectMemberRequest request) {
        ProjectMember entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("ProjectMember not found with id " + id));
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
        return repository.save(entity);
    }
    @Override
    @Transactional
    public void delete(ProjectMemberId id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("ProjectMember not found with id " + id);
        }
        repository.deleteById(id);
    }
}
