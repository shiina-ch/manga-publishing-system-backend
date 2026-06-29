package group1.com.MangaSystemAndManagement.service.impl;

import group1.com.MangaSystemAndManagement.config.properties.StorageProperties;
import group1.com.MangaSystemAndManagement.dto.request.SubmissionRequest;
import group1.com.MangaSystemAndManagement.model.Account;
import group1.com.MangaSystemAndManagement.model.Planning;
import group1.com.MangaSystemAndManagement.model.Project;
import group1.com.MangaSystemAndManagement.model.ProjectMember;
import group1.com.MangaSystemAndManagement.model.ProjectMemberId;
import group1.com.MangaSystemAndManagement.model.ProjectRole;
import group1.com.MangaSystemAndManagement.model.Submission;
import group1.com.MangaSystemAndManagement.model.SubmissionStatus;
import group1.com.MangaSystemAndManagement.model.SubmissionFile;
import group1.com.MangaSystemAndManagement.repository.AccountRepository;
import group1.com.MangaSystemAndManagement.repository.PlanningRepository;
import group1.com.MangaSystemAndManagement.repository.ProjectMemberRepository;
import group1.com.MangaSystemAndManagement.repository.ProjectRepository;
import group1.com.MangaSystemAndManagement.repository.ProjectRoleRepository;
import group1.com.MangaSystemAndManagement.repository.SubmissionFileRepository;
import group1.com.MangaSystemAndManagement.repository.SubmissionRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository repository;
    private final SubmissionFileRepository submissionFileRepository;
    private final AccountRepository accountRepository;
    private final PlanningRepository planningRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final StorageProperties storageProperties;

    /**
     * Luồng: User (accountId) → tạo Submission → lưu SubmissionFiles
     */
    @Override
    @Transactional
    public Submission submitFiles(Long accountId, SubmissionRequest request) {
        // 1. Lookup Account – người submit
        Account submitter = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id " + accountId));

        // 2. Tạo Submission entity
        Submission submission = new Submission();
        submission.setSubmittedBy(submitter);
        submission.setTitle(request.getTitle());
        submission.setContentUrl(request.getNote());
        submission.setStatus(SubmissionStatus.PENDING);
        submission.setSubmittedAt(Instant.now());

        // 3. Link Planning nếu có
        if (request.getPlanningId() != null) {
            Planning planning = planningRepository.findById(request.getPlanningId())
                    .orElseThrow(() -> new RuntimeException("Planning not found with id " + request.getPlanningId()));
            submission.setPlanning(planning);
        }

        submission = repository.save(submission);

        // 4. Lưu các file PSD đính kèm
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            List<SubmissionFile> savedFiles = new ArrayList<>();
            Path uploadDir = storageProperties.uploadPath();

            try {
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }
            } catch (IOException e) {
                throw new RuntimeException("Cannot create upload directory: " + e.getMessage(), e);
            }

            for (MultipartFile multipartFile : request.getFiles()) {
                if (multipartFile == null || multipartFile.isEmpty()) continue;

                String originalName = multipartFile.getOriginalFilename();
                String extension = "";
                if (originalName != null && originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
                }

                String uniqueFilename = UUID.randomUUID().toString() + extension;
                Path targetPath = uploadDir.resolve(uniqueFilename);

                try {
                    Files.copy(multipartFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to store file " + originalName + ": " + e.getMessage(), e);
                }

                // Tạo public URL
                String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/uploads/")
                        .path(uniqueFilename)
                        .toUriString();

                SubmissionFile submissionFile = new SubmissionFile();
                submissionFile.setOriginalName(originalName);
                submissionFile.setFilePath(fileUrl);
                submissionFile.setFileSize(multipartFile.getSize());
                submissionFile.setContentType(multipartFile.getContentType());
                submissionFile.setSubmission(submission);

                savedFiles.add(submissionFileRepository.save(submissionFile));
            }

            submission.setFiles(savedFiles);
        }

        return submission;
    }

    @Override
    public Optional<Submission> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Submission> findAll() {
        return repository.findAllWithSubmittedBy();
    }

    @Override
    @Transactional
    public Submission update(Long id, SubmissionRequest request) {
        Submission entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with id " + id));
        if (request.getTitle() != null) entity.setTitle(request.getTitle());
        if (request.getNote() != null) entity.setContentUrl(request.getNote());
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

        if (SubmissionStatus.APPROVED == submission.getStatus()) {
            throw new RuntimeException("Submission is already approved");
        }

        // 1. Tạo Project từ Submission
        Project project = new Project();
        project.setTitle(submission.getTitle());
        project.setDescription(submission.getContentUrl());
        project.setStatus("ACTIVE");
        project = projectRepository.save(project);

        // 2. Link Submission → Project và cập nhật trạng thái
        submission.setStatus(SubmissionStatus.APPROVED);
        submission.setProject(project);
        repository.save(submission);

        // 3. Thêm người submit làm LEAD_MANGAKA của Project
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
