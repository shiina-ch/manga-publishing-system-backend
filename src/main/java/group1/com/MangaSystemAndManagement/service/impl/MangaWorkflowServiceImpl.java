package group1.com.MangaSystemAndManagement.service.impl;

import group1.com.MangaSystemAndManagement.dto.request.NameSubmissionRequest;
import group1.com.MangaSystemAndManagement.dto.request.ReviewRequest;
import group1.com.MangaSystemAndManagement.dto.request.ResubmitRequest;
import group1.com.MangaSystemAndManagement.model.Account;
import group1.com.MangaSystemAndManagement.model.Project;
import group1.com.MangaSystemAndManagement.model.Planning;
import group1.com.MangaSystemAndManagement.model.Submission;
import group1.com.MangaSystemAndManagement.model.SubmissionReview;
import group1.com.MangaSystemAndManagement.repository.AccountRepository;
import group1.com.MangaSystemAndManagement.repository.ProjectRepository;
import group1.com.MangaSystemAndManagement.repository.PlanningRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.MangaWorkflowService;
import group1.com.MangaSystemAndManagement.service.interfaces.SubmissionReviewService;
import group1.com.MangaSystemAndManagement.service.interfaces.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MangaWorkflowServiceImpl implements MangaWorkflowService {

    private final SubmissionService submissionService;
    private final SubmissionReviewService submissionReviewService;
    private final AccountRepository accountRepository;
    private final ProjectRepository projectRepository;
    private final PlanningRepository planningRepository;

    @Override
    @Transactional
    public Submission submitName(NameSubmissionRequest req) {
        Optional<Project> projectOpt = projectRepository.findById(req.getProjectId());
        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Project not found");
        }

        Optional<Account> accountOpt = accountRepository.findById(req.getSubmittedById());
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Submitting account not found");
        }
        Account submitter = accountOpt.get();
        boolean isMangaka = submitter.getSystemRole() != null && submitter.getSystemRole().stream()
                .anyMatch(r -> "MANGAKA".equalsIgnoreCase(r.getRoleName()));
        if (!isMangaka) {
            throw new AccessDeniedException("Only Mangaka can submit a Name");
        }

        Submission s = new Submission();
        s.setProject(projectOpt.get());
        if (req.getPlanningId() != null) {
            Optional<Planning> planningOpt = planningRepository.findById(req.getPlanningId());
            planningOpt.ifPresent(s::setPlanning);
        }
        s.setSubmittedBy(submitter);
        s.setTitle(req.getTitle());
        s.setContentUrl(req.getContentUrl());
        s.setStatus("SUBMITTED");
        s.setSubmittedAt(Instant.now());

        return submissionService.create(s);
    }

    @Override
    @Transactional
    public SubmissionReview reviewName(ReviewRequest req) {
        Optional<Submission> subOpt = submissionService.findById(req.getSubmissionId());
        if (subOpt.isEmpty()) {
            throw new RuntimeException("Submission not found");
        }
        Submission submission = subOpt.get();

        Optional<Account> reviewerOpt = accountRepository.findById(req.getReviewerId());
        if (reviewerOpt.isEmpty()) {
            throw new RuntimeException("Reviewer not found");
        }
        Account reviewer = reviewerOpt.get();
        boolean isTantor = reviewer.getSystemRole() != null && reviewer.getSystemRole().stream()
                .anyMatch(r -> "TANTOR".equalsIgnoreCase(r.getRoleName()));
        if (!isTantor) {
            throw new AccessDeniedException("Only Tantor can review Names");
        }

        SubmissionReview review = new SubmissionReview();
        review.setSubmission(submission);
        review.setReviewer(reviewer);
        review.setDecision(req.getDecision());
        StringBuilder commentBuilder = new StringBuilder();
        if (req.getPacingPass() != null) commentBuilder.append("Pacing: ").append(req.getPacingPass() ? "PASS" : "FAIL").append(". ");
        if (req.getStructurePass() != null) commentBuilder.append("Structure: ").append(req.getStructurePass() ? "PASS" : "FAIL").append(". ");
        if (req.getImageFlowPass() != null) commentBuilder.append("ImageFlow: ").append(req.getImageFlowPass() ? "PASS" : "FAIL").append(". ");
        if (req.getComment() != null && !req.getComment().isBlank()) commentBuilder.append("Notes: ").append(req.getComment());
        review.setComment(commentBuilder.toString());
        review.setReviewedAt(Instant.now());

        SubmissionReview savedReview = submissionReviewService.create(review);

        String decision = req.getDecision() != null ? req.getDecision().trim().toUpperCase() : "";
        if (decision.equals("APPROVE") || decision.equals("APPROVED")) {
            submission.setStatus("APPROVED_BY_TANTOR");
        } else {
            submission.setStatus("CHANGES_REQUESTED_BY_TANTOR");
        }
        submissionService.update(submission.getId(), submission);

        return savedReview;
    }

    @Override
    @Transactional
    public Submission resubmitName(ResubmitRequest req) {
        Optional<Submission> subOpt = submissionService.findById(req.getSubmissionId());
        if (subOpt.isEmpty()) {
            throw new RuntimeException("Original submission not found");
        }
        Submission submission = subOpt.get();

        if (submission.getSubmittedBy() == null || submission.getSubmittedBy().getId() != req.getSubmittedById()) {
            throw new AccessDeniedException("Only original submitter can resubmit");
        }

        submission.setTitle(req.getTitle());
        submission.setContentUrl(req.getContentUrl());
        submission.setStatus("RESUBMITTED");
        submission.setSubmittedAt(Instant.now());

        return submissionService.update(submission.getId(), submission);
    }

    @Override
    public List<Submission> listSubmissions(String status) {
        var all = submissionService.findAll();
        if (status == null || status.isBlank()) return all;
        return all.stream().filter(s -> status.equalsIgnoreCase(s.getStatus())).toList();
    }

    @Override
    public List<SubmissionReview> listReviewsForSubmission(Long submissionId) {
        var all = submissionReviewService.findAll();
        return all.stream().filter(r -> r.getSubmission() != null && r.getSubmission().getId().equals(submissionId)).toList();
    }
}
