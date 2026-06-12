package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.dto.request.SubmissionRequest;
import group1.com.MangaSystemAndManagement.model.Project;
import group1.com.MangaSystemAndManagement.model.ProjectMember;
import group1.com.MangaSystemAndManagement.model.ProjectMemberId;
import group1.com.MangaSystemAndManagement.model.ProjectRole;
import group1.com.MangaSystemAndManagement.model.Submission;
import group1.com.MangaSystemAndManagement.repository.ProjectMemberRepository;
import group1.com.MangaSystemAndManagement.repository.ProjectRepository;
import group1.com.MangaSystemAndManagement.repository.ProjectRoleRepository;
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
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRoleRepository projectRoleRepository;
    @Override
    @Transactional
    public Submission create(SubmissionRequest request) {
        Submission entity = new Submission();
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
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
    public Submission update(Long id, SubmissionRequest request) {
        Submission entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Submission not found with id " + id));
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
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

    @Override
    @Transactional
    public Submission approveAndCreateProject(Long submissionId) {
        Submission submission = repository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        if ("APPROVED".equalsIgnoreCase(submission.getStatus())) {
            throw new RuntimeException("Submission is already approved");
        }

        // 1. Create Project
        Project project = new Project();
        project.setTitle(submission.getTitle());
        project.setDescription(submission.getContentUrl());
        project.setStatus("ACTIVE");
        project = projectRepository.save(project);

        // 2. Link Submission to Project & Approve
        submission.setStatus("APPROVED");
        submission.setProject(project);
        repository.save(submission);

        // 3. Add Submitter as Project Member
        ProjectRole leadRole = projectRoleRepository.findByRoleName("LEAD_MANGAKA")
                .orElseThrow(() -> new RuntimeException("ProjectRole LEAD_MANGAKA not found"));

        ProjectMemberId memberId = new ProjectMemberId();
        memberId.setProjectId(project.getId());
        memberId.setAccountId(submission.getSubmittedBy().getId());
        memberId.setProjectRoleId(leadRole.getId());

        ProjectMember projectMember = new ProjectMember();
        projectMember.setId(memberId);
        projectMember.setProject(project);
        projectMember.setAccount(submission.getSubmittedBy());
        projectMemberRepository.save(projectMember);

        return submission;
    }
}
