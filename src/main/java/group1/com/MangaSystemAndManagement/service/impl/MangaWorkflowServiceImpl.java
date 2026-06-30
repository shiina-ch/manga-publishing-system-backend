package group1.com.MangaSystemAndManagement.service.impl;

import group1.com.MangaSystemAndManagement.dto.request.NameSubmissionRequest;
import group1.com.MangaSystemAndManagement.dto.request.ReviewRequest;
import group1.com.MangaSystemAndManagement.dto.request.ResubmitRequest;
import group1.com.MangaSystemAndManagement.dto.response.SubmissionReviewResponse;
import group1.com.MangaSystemAndManagement.model.*;
import group1.com.MangaSystemAndManagement.repository.AccountRepository;
import group1.com.MangaSystemAndManagement.repository.ProjectRepository;
import group1.com.MangaSystemAndManagement.repository.PlanningRepository;
import group1.com.MangaSystemAndManagement.repository.SubmissionRepository;
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
    private final SubmissionRepository submissionRepository;
    private final AccountRepository accountRepository;
    private final ProjectRepository projectRepository;
    private final PlanningRepository planningRepository;

    @Override
    @Transactional
    public Submission submitName(NameSubmissionRequest req) {
        Optional<Account> accountOpt = accountRepository.findById(req.getSubmittedById());
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Submitting account not found");
        }
        Account submitter = accountOpt.get();
        boolean isMangaka = submitter.hasRole(SystemRoleName.MANGAKA);
        if (!isMangaka) {
            throw new AccessDeniedException("Only Mangaka can submit a Name");
        }

        Submission s = new Submission();
        
        if (req.getProjectId() != null && req.getProjectId() > 0) {
            Optional<Project> projectOpt = projectRepository.findById(req.getProjectId());
            if (projectOpt.isEmpty()) {
                throw new RuntimeException("Project not found");
            }
            s.setProject(projectOpt.get());
        }

        if (req.getPlanningId() != null && req.getPlanningId() > 0) {
            Optional<Planning> planningOpt = planningRepository.findById(req.getPlanningId());
            planningOpt.ifPresent(s::setPlanning);
        }
        s.setSubmittedBy(submitter);
        s.setTitle(req.getTitle());
        s.setContentUrl(req.getContentUrl());
        s.setStatus(group1.com.MangaSystemAndManagement.model.SubmissionStatus.PENDING);
        s.setSubmittedAt(Instant.now());

        return submissionRepository.save(s);
    }

    @Override
    @Transactional
    public SubmissionReview reviewByTantou(ReviewRequest req) {
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
        if (!reviewer.hasRole(SystemRoleName.TANTOU_EDITOR)) {
            throw new AccessDeniedException("Only Tantou Editors can perform Editorial Review");
        }

        if (submission.getStatus() != group1.com.MangaSystemAndManagement.model.SubmissionStatus.PENDING && 
            submission.getStatus() != group1.com.MangaSystemAndManagement.model.SubmissionStatus.PROCESSING) {
            throw new RuntimeException("Submission must be in PENDING or PROCESSING status for Editorial Review");
        }

        SubmissionReview review = new SubmissionReview();
        review.setSubmission(submission);
        review.setReviewer(reviewer);
        review.setStage(group1.com.MangaSystemAndManagement.model.ReviewStage.EDITORIAL);
        
        String decision = req.getDecision() != null ? req.getDecision().trim().toUpperCase() : "";
        if (decision.equals("APPROVE")) decision = "APPROVED";
        review.setDecision(decision);

        StringBuilder commentBuilder = new StringBuilder();
        if (req.getPacingPass() != null) commentBuilder.append("Pacing: ").append(req.getPacingPass() ? "PASS" : "FAIL").append(". ");
        if (req.getStructurePass() != null) commentBuilder.append("Structure: ").append(req.getStructurePass() ? "PASS" : "FAIL").append(". ");
        if (req.getImageFlowPass() != null) commentBuilder.append("ImageFlow: ").append(req.getImageFlowPass() ? "PASS" : "FAIL").append(". ");
        if (req.getComment() != null && !req.getComment().isBlank()) commentBuilder.append("Notes: ").append(req.getComment());
        review.setComment(commentBuilder.toString());
        review.setReviewedAt(Instant.now());

        group1.com.MangaSystemAndManagement.dto.request.SubmissionReviewRequest reviewReq = new group1.com.MangaSystemAndManagement.dto.request.SubmissionReviewRequest();
        org.springframework.beans.BeanUtils.copyProperties(review, reviewReq);
        SubmissionReview savedReview = submissionReviewService.create(reviewReq);

        if (decision.equals("APPROVED")) {
            submission.setStatus(group1.com.MangaSystemAndManagement.model.SubmissionStatus.PENDING_BOARD_REVIEW);
        } else {
            submission.setStatus(group1.com.MangaSystemAndManagement.model.SubmissionStatus.REJECTED);
        }
        
        group1.com.MangaSystemAndManagement.dto.request.SubmissionRequest subReq = new group1.com.MangaSystemAndManagement.dto.request.SubmissionRequest();
        org.springframework.beans.BeanUtils.copyProperties(submission, subReq);
        submissionService.update(submission.getId(), subReq);

        return savedReview;
    }

    @Override
    @Transactional
    public SubmissionReview reviewByBoard(ReviewRequest req) {
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
        if (!reviewer.hasRole(SystemRoleName.EDITORIAL_BOARD_MEMBER)) {
            throw new AccessDeniedException("Only Editorial Board Members can vote");
        }

        if (submission.getStatus() != group1.com.MangaSystemAndManagement.model.SubmissionStatus.ON_GOING) {
            throw new RuntimeException("Submission must be in ON_GOING status for Board Voting");
        }

        boolean alreadyVoted = submissionReviewService.findAll().stream()
            .anyMatch(r -> submission.getId().equals(r.getSubmissionId())
                        && reviewer.getId() == r.getReviewerId()
                        && r.getStage() == group1.com.MangaSystemAndManagement.model.ReviewStage.EDITORIAL_BOARD);
        if (alreadyVoted) {
            throw new RuntimeException("Board member has already voted for this submission");
        }

        SubmissionReview review = new SubmissionReview();
        review.setSubmission(submission);
        review.setReviewer(reviewer);
        review.setStage(group1.com.MangaSystemAndManagement.model.ReviewStage.EDITORIAL_BOARD);
        
        String decision = req.getDecision() != null ? req.getDecision().trim().toUpperCase() : "";
        if (decision.equals("APPROVE")) decision = "APPROVED";
        review.setDecision(decision);

        StringBuilder commentBuilder = new StringBuilder();
        if (req.getComment() != null && !req.getComment().isBlank()) commentBuilder.append("Notes: ").append(req.getComment());
        review.setComment(commentBuilder.toString());
        review.setReviewedAt(Instant.now());

        group1.com.MangaSystemAndManagement.dto.request.SubmissionReviewRequest reviewReq = new group1.com.MangaSystemAndManagement.dto.request.SubmissionReviewRequest();
        org.springframework.beans.BeanUtils.copyProperties(review, reviewReq);
        SubmissionReview savedReview = submissionReviewService.create(reviewReq);

        var boardReviews = submissionReviewService.findAll().stream()
            .filter(r -> submission.getId().equals(r.getSubmissionId())
                      && r.getStage() == group1.com.MangaSystemAndManagement.model.ReviewStage.EDITORIAL_BOARD)
            .toList();

        long totalBoardVotes = boardReviews.size();
        
        if (totalBoardVotes == 3) {
            long approveCount = boardReviews.stream()
                .filter(r -> "APPROVED".equals(r.getDecision()))
                .count();

            if (approveCount == 2 || approveCount > 2) {
                submission.setStatus(group1.com.MangaSystemAndManagement.model.SubmissionStatus.APPROVED);
            } else {
                submission.setStatus(group1.com.MangaSystemAndManagement.model.SubmissionStatus.REJECTED);
            }
            
            group1.com.MangaSystemAndManagement.dto.request.SubmissionRequest subReq = new group1.com.MangaSystemAndManagement.dto.request.SubmissionRequest();
            org.springframework.beans.BeanUtils.copyProperties(submission, subReq);
            submissionService.update(submission.getId(), subReq);
        }

        return savedReview;
    }

    @Override
    @Transactional
    public Submission submitToBoard(Long submissionId, Long tantouId) {
        Optional<Submission> subOpt = submissionService.findById(submissionId);
        if (subOpt.isEmpty()) {
            throw new RuntimeException("Submission not found");
        }
        Submission submission = subOpt.get();

        Optional<Account> reviewerOpt = accountRepository.findById(tantouId);
        if (reviewerOpt.isEmpty()) {
            throw new RuntimeException("Tantou Editor not found");
        }
        Account reviewer = reviewerOpt.get();
        if (!reviewer.hasRole(SystemRoleName.TANTOU_EDITOR)) {
            throw new AccessDeniedException("Only Tantou Editors can submit to the Board");
        }

        if (submission.getStatus() != SubmissionStatus.PENDING_BOARD_REVIEW) {
            throw new RuntimeException("Submission must be PENDING_BOARD_REVIEW to submit to board");
        }

        submission.setStatus(group1.com.MangaSystemAndManagement.model.SubmissionStatus.ON_GOING);
        
        group1.com.MangaSystemAndManagement.dto.request.SubmissionRequest subReq = new group1.com.MangaSystemAndManagement.dto.request.SubmissionRequest();
        org.springframework.beans.BeanUtils.copyProperties(submission, subReq);
        return submissionService.update(submission.getId(), subReq);
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
        submission.setStatus(group1.com.MangaSystemAndManagement.model.SubmissionStatus.PENDING);
        submission.setSubmittedAt(Instant.now());

        group1.com.MangaSystemAndManagement.dto.request.SubmissionRequest subReq = new group1.com.MangaSystemAndManagement.dto.request.SubmissionRequest();
        org.springframework.beans.BeanUtils.copyProperties(submission, subReq);
        return submissionService.update(submission.getId(), subReq);
    }

    @Override
    public List<Submission> listSubmissions(String status) {
        var all = submissionService.findAll();
        if (status == null || status.isBlank()) return all;
        return all.stream().filter(s -> status.equalsIgnoreCase(s.getStatus().name())).toList();
    }

    @Override
    public List<SubmissionReviewResponse> listReviewsForSubmission(Long submissionId) {
        var all = submissionReviewService.findAll();
        return all.stream().filter(r -> submissionId.equals(r.getSubmissionId())).toList();
    }
}
