package group1.com.MangaSystemAndManagement.service.impl;

import group1.com.MangaSystemAndManagement.dto.request.AssignSketchTaskRequest;
import group1.com.MangaSystemAndManagement.dto.request.CompleteSketchTaskRequest;
import group1.com.MangaSystemAndManagement.dto.request.CreateSketchPageRequest;
import group1.com.MangaSystemAndManagement.dto.request.SubmitSketchReviewRequest;
import group1.com.MangaSystemAndManagement.model.*;
import group1.com.MangaSystemAndManagement.repository.AccountRepository;
import group1.com.MangaSystemAndManagement.repository.ChapterRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SketchWorkflowServiceImpl implements SketchWorkflowService {

    private final SketchPageService sketchPageService;
    private final SketchTaskService sketchTaskService;
    private final SketchReviewService sketchReviewService;
    private final AccountRepository accountRepository;
    private final ChapterRepository chapterRepository;

    @Override
    @Transactional
    public SketchPage createSketchPage(CreateSketchPageRequest req) {
        // Validate chapter exists
        Optional<Chapter> chapterOpt = chapterRepository.findById(req.getChapterId());
        if (chapterOpt.isEmpty()) {
            throw new RuntimeException("Chapter not found with id " + req.getChapterId());
        }

        // Validate creator exists and is MANGAKA
        Optional<Account> creatorOpt = accountRepository.findById(req.getCreatedById());
        if (creatorOpt.isEmpty()) {
            throw new RuntimeException("Account not found with id " + req.getCreatedById());
        }
        Account creator = creatorOpt.get();
        boolean isMangaka = creator.getSystemRole() != null && creator.getSystemRole().stream()
                .anyMatch(r -> "MANGAKA".equalsIgnoreCase(r.getRoleName()));
        if (!isMangaka) {
            throw new AccessDeniedException("Only Mangaka can create sketch pages");
        }

        // Create sketch page
        SketchPage sketchPage = new SketchPage();
        sketchPage.setChapter(chapterOpt.get());
        sketchPage.setPageNumber(req.getPageNumber());
        sketchPage.setInitialSketchUrl(req.getInitialSketchUrl());
        sketchPage.setCreatedBy(creator);
        sketchPage.setStatus("SKETCH_CREATED");
        sketchPage.setCreatedAt(Instant.now());

        group1.com.MangaSystemAndManagement.dto.request.SketchPageRequest reqDto = new group1.com.MangaSystemAndManagement.dto.request.SketchPageRequest();
        org.springframework.beans.BeanUtils.copyProperties(sketchPage, reqDto);
        return sketchPageService.create(reqDto);
    }

    @Override
    @Transactional
    public void assignTasksToAssistants(AssignSketchTaskRequest req) {
        // Validate sketch page exists
        Optional<SketchPage> sketchPageOpt = sketchPageService.findById(req.getSketchPageId());
        if (sketchPageOpt.isEmpty()) {
            throw new RuntimeException("SketchPage not found with id " + req.getSketchPageId());
        }
        SketchPage sketchPage = sketchPageOpt.get();

        // Validate that the requester is the original creator (Mangaka) - optional but recommended
        // For now, we'll skip this check as per architectural flexibility

        // Create tasks for each assignment
        if (req.getTasks() != null && !req.getTasks().isEmpty()) {
            for (AssignSketchTaskRequest.SketchTaskDetail taskDetail : req.getTasks()) {
                Optional<Account> assigneeOpt = accountRepository.findById(taskDetail.getAssignedToId());
                if (assigneeOpt.isEmpty()) {
                    throw new RuntimeException("Account not found with id " + taskDetail.getAssignedToId());
                }

                SketchTask task = new SketchTask();
                task.setSketchPage(sketchPage);
                task.setTaskType(taskDetail.getTaskType());
                task.setDescription(taskDetail.getDescription());
                task.setAssignedTo(assigneeOpt.get());
                task.setStatus("ASSIGNED");

                group1.com.MangaSystemAndManagement.dto.request.SketchTaskRequest taskReq = new group1.com.MangaSystemAndManagement.dto.request.SketchTaskRequest();
                org.springframework.beans.BeanUtils.copyProperties(task, taskReq);
                sketchTaskService.create(taskReq);
            }
        }

        // Update sketch page status
        sketchPage.setStatus("TASKS_ASSIGNED");
        sketchPage.setUpdatedAt(Instant.now());
        group1.com.MangaSystemAndManagement.dto.request.SketchPageRequest spReq1 = new group1.com.MangaSystemAndManagement.dto.request.SketchPageRequest();
        org.springframework.beans.BeanUtils.copyProperties(sketchPage, spReq1);
        sketchPageService.update(sketchPage.getId(), spReq1);
    }

    @Override
    @Transactional
    public SketchTask completeSketchTask(CompleteSketchTaskRequest req) {
        // Validate task exists
        Optional<SketchTask> taskOpt = sketchTaskService.findById(req.getSketchTaskId());
        if (taskOpt.isEmpty()) {
            throw new RuntimeException("SketchTask not found with id " + req.getSketchTaskId());
        }
        SketchTask task = taskOpt.get();

        // Validate that the completer is the assigned person
        if (!req.getCompletedById().equals(task.getAssignedTo().getId())) {
            throw new AccessDeniedException("Only the assigned assistant can complete this task");
        }

        // Update task
        task.setStatus("COMPLETED");
        task.setCompletedUrl(req.getCompletedUrl());
        task.setCompletedAt(Instant.now());
        group1.com.MangaSystemAndManagement.dto.request.SketchTaskRequest taskReq2 = new group1.com.MangaSystemAndManagement.dto.request.SketchTaskRequest();
        org.springframework.beans.BeanUtils.copyProperties(task, taskReq2);
        SketchTask updated = sketchTaskService.update(task.getId(), taskReq2);

        // Check if all tasks for this sketch page are completed
        SketchPage sketchPage = task.getSketchPage();
        List<SketchTask> allTasks = sketchTaskService.findAll().stream()
                .filter(t -> t.getSketchPage().getId().equals(sketchPage.getId()))
                .toList();

        boolean allCompleted = allTasks.stream()
                .allMatch(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()));

        if (allCompleted) {
            sketchPage.setStatus("SKETCHES_COMPLETED");
            sketchPage.setUpdatedAt(Instant.now());
            group1.com.MangaSystemAndManagement.dto.request.SketchPageRequest spReq2 = new group1.com.MangaSystemAndManagement.dto.request.SketchPageRequest();
            org.springframework.beans.BeanUtils.copyProperties(sketchPage, spReq2);
            sketchPageService.update(sketchPage.getId(), spReq2);
        }

        return updated;
    }

    @Override
    @Transactional
    public void submitSketchForReview(Long sketchPageId, Long mangakaId) {
        // Validate sketch page exists
        Optional<SketchPage> sketchPageOpt = sketchPageService.findById(sketchPageId);
        if (sketchPageOpt.isEmpty()) {
            throw new RuntimeException("SketchPage not found with id " + sketchPageId);
        }
        SketchPage sketchPage = sketchPageOpt.get();

        // Validate that all tasks are completed
        List<SketchTask> allTasks = sketchTaskService.findAll().stream()
                .filter(t -> t.getSketchPage().getId().equals(sketchPageId))
                .toList();

        boolean allCompleted = allTasks.stream()
                .allMatch(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()));

        if (!allCompleted) {
            throw new RuntimeException("Cannot submit for review: not all tasks are completed");
        }

        // Validate that the submitter is the original creator
        if (!mangakaId.equals(sketchPage.getCreatedBy().getId())) {
            throw new AccessDeniedException("Only the original creator can submit this sketch for review");
        }

        // Update status to REVIEW_PENDING
        sketchPage.setStatus("REVIEW_PENDING");
        sketchPage.setUpdatedAt(Instant.now());
        group1.com.MangaSystemAndManagement.dto.request.SketchPageRequest spReq3 = new group1.com.MangaSystemAndManagement.dto.request.SketchPageRequest();
        org.springframework.beans.BeanUtils.copyProperties(sketchPage, spReq3);
        sketchPageService.update(sketchPage.getId(), spReq3);
    }

    @Override
    @Transactional
    public SketchReview reviewSketch(SubmitSketchReviewRequest req) {
        // Validate sketch page exists
        Optional<SketchPage> sketchPageOpt = sketchPageService.findById(req.getSketchPageId());
        if (sketchPageOpt.isEmpty()) {
            throw new RuntimeException("SketchPage not found with id " + req.getSketchPageId());
        }
        SketchPage sketchPage = sketchPageOpt.get();

        // Validate reviewer exists and is TANTOR
        Optional<Account> reviewerOpt = accountRepository.findById(req.getReviewerId());
        if (reviewerOpt.isEmpty()) {
            throw new RuntimeException("Account not found with id " + req.getReviewerId());
        }
        Account reviewer = reviewerOpt.get();
        boolean isTantor = reviewer.getSystemRole() != null && reviewer.getSystemRole().stream()
                .anyMatch(r -> "TANTOR".equalsIgnoreCase(r.getRoleName()));
        if (!isTantor) {
            throw new AccessDeniedException("Only Tantor can review sketches");
        }

        // Create review
        SketchReview review = new SketchReview();
        review.setSketchPage(sketchPage);
        review.setReviewer(reviewer);
        review.setDecision(req.getDecision());
        review.setComment(req.getComment());
        review.setLayoutFeedback(req.getLayoutFeedback());
        review.setDetailsFeedback(req.getDetailsFeedback());
        review.setReviewedAt(Instant.now());

        group1.com.MangaSystemAndManagement.dto.request.SketchReviewRequest revReq1 = new group1.com.MangaSystemAndManagement.dto.request.SketchReviewRequest();
        org.springframework.beans.BeanUtils.copyProperties(review, revReq1);
        SketchReview savedReview = sketchReviewService.create(revReq1);

        // Update sketch page status based on decision
        String decision = req.getDecision() != null ? req.getDecision().trim().toUpperCase() : "";
        if (decision.equals("APPROVE") || decision.equals("APPROVED")) {
            sketchPage.setStatus("APPROVED");
        } else if (decision.equals("REQUEST_CHANGES") || decision.equals("CHANGES_REQUESTED")) {
            sketchPage.setStatus("CHANGES_REQUESTED");
        }
        sketchPage.setUpdatedAt(Instant.now());
        group1.com.MangaSystemAndManagement.dto.request.SketchPageRequest spReq4 = new group1.com.MangaSystemAndManagement.dto.request.SketchPageRequest();
        org.springframework.beans.BeanUtils.copyProperties(sketchPage, spReq4);
        sketchPageService.update(sketchPage.getId(), spReq4);

        return savedReview;
    }

    @Override
    @Transactional
    public void requestSketchChanges(Long sketchPageId, Long reviewerId, String comment) {
        // Validate sketch page exists
        Optional<SketchPage> sketchPageOpt = sketchPageService.findById(sketchPageId);
        if (sketchPageOpt.isEmpty()) {
            throw new RuntimeException("SketchPage not found with id " + sketchPageId);
        }
        SketchPage sketchPage = sketchPageOpt.get();

        // Validate reviewer exists and is TANTOR
        Optional<Account> reviewerOpt = accountRepository.findById(reviewerId);
        if (reviewerOpt.isEmpty()) {
            throw new RuntimeException("Account not found with id " + reviewerId);
        }
        Account reviewer = reviewerOpt.get();
        boolean isTantor = reviewer.getSystemRole() != null && reviewer.getSystemRole().stream()
                .anyMatch(r -> "TANTOR".equalsIgnoreCase(r.getRoleName()));
        if (!isTantor) {
            throw new AccessDeniedException("Only Tantor can request changes");
        }

        // Update sketch page status
        sketchPage.setStatus("CHANGES_REQUESTED");
        sketchPage.setUpdatedAt(Instant.now());
        group1.com.MangaSystemAndManagement.dto.request.SketchPageRequest spReq5 = new group1.com.MangaSystemAndManagement.dto.request.SketchPageRequest();
        org.springframework.beans.BeanUtils.copyProperties(sketchPage, spReq5);
        sketchPageService.update(sketchPage.getId(), spReq5);

        // Create a review record for tracking
        SketchReview review = new SketchReview();
        review.setSketchPage(sketchPage);
        review.setReviewer(reviewer);
        review.setDecision("REQUEST_CHANGES");
        review.setComment(comment);
        review.setReviewedAt(Instant.now());
        group1.com.MangaSystemAndManagement.dto.request.SketchReviewRequest revReq2 = new group1.com.MangaSystemAndManagement.dto.request.SketchReviewRequest();
        org.springframework.beans.BeanUtils.copyProperties(review, revReq2);
        sketchReviewService.create(revReq2);
    }

    @Override
    public SketchPage getSketchPageStatus(Long sketchPageId) {
        Optional<SketchPage> sketchPageOpt = sketchPageService.findById(sketchPageId);
        if (sketchPageOpt.isEmpty()) {
            throw new RuntimeException("SketchPage not found with id " + sketchPageId);
        }
        return sketchPageOpt.get();
    }
}
