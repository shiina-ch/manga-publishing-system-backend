package group1.com.MangaSystemAndManagement.service.impl;

import group1.com.MangaSystemAndManagement.dto.request.ProjectRequest;
import group1.com.MangaSystemAndManagement.exception.ResourceNotFoundException;
import group1.com.MangaSystemAndManagement.model.Account;
import group1.com.MangaSystemAndManagement.model.Project;
import group1.com.MangaSystemAndManagement.model.SystemRoleName;
import group1.com.MangaSystemAndManagement.repository.AccountRepository;
import group1.com.MangaSystemAndManagement.repository.ProjectRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository repository;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    @Transactional
    public Project create(ProjectRequest request) {
        String title = normalizeTitle(request.getTitle());

        if (repository.findAll().stream()
                .anyMatch(p -> normalizeTitle(p.getTitle()).equalsIgnoreCase(title))) {
            throw new IllegalArgumentException("Project title already exists");
        }

        Project project = new Project();
        project.setTitle(title);
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());
        project.setCreatedAt(Instant.now());
        project.setProjectWorkflowStatus(
                group1.com.MangaSystemAndManagement.model.ProjectWorkflowStatus.DRAFT);

        return repository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Project> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public Project update(Long id, ProjectRequest request) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + id));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            project.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        return repository.save(project);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Project not found with id " + id);
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public Project assignTantou(Long projectId, Long tantouId) {
        Project project = repository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + projectId));

        Account tantou = accountRepository.findById(tantouId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id " + tantouId));

        if (!tantou.hasRole(SystemRoleName.TANTOU_EDITOR)
                && !tantou.hasRole(SystemRoleName.ADMIN)) {
            throw new AccessDeniedException(
                    "Assigned account does not hold the TANTOU_EDITOR role");
        }

        project.setTantou(tantou);
        return repository.save(project);
    }

    private String normalizeTitle(String title) {
        return title == null ? null : title.trim();
    }
}
