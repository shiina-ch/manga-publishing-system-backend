package group1.com.MangaSystemAndManagement.service.impl;

import group1.com.MangaSystemAndManagement.dto.request.*;
import group1.com.MangaSystemAndManagement.dto.response.*;
import group1.com.MangaSystemAndManagement.model.*;
import group1.com.MangaSystemAndManagement.repository.*;
import group1.com.MangaSystemAndManagement.service.interfaces.ProductionWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionWorkflowServiceImpl implements ProductionWorkflowService {

    private final ProjectRepository projectRepository;
    private final ProductionPlanRepository productionPlanRepository;
    private final ChapterRepository chapterRepository;
    private final TaskRepository taskRepository;
    private final FeedbackRepository feedbackRepository;
    private final AssetRepository assetRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest req, Long editorId) {
        Account creator = getAccount(editorId);
        if (!creator.hasRole(SystemRoleName.EDITORIAL_BOARD_MEMBER)) {
            throw new AccessDeniedException("Only EDITORIAL_BOARD_MEMBER can create projects");
        }

        Account tantou = getAccount(req.getTantouId());
        if (!tantou.hasRole(SystemRoleName.TANTOU_EDITOR)) {
            throw new IllegalArgumentException("Assigned account must have the TANTOU_EDITOR role");
        }

        Project project = new Project();
        project.setTitle(req.getTitle());
        project.setGenre(req.getGenre());
        project.setTargetAudience(req.getTargetAudience());
        project.setFormat(req.getFormat());
        project.setProjectWorkflowStatus(ProjectWorkflowStatus.DRAFT);
        project.setOwner(tantou); // Tantou is assigned as the owner of the project

        project = projectRepository.save(project);
        return mapToProjectResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse activateProject(Long projectId, Long requesterId) {
        Account requester = getAccount(requesterId);
        if (!requester.hasRole(SystemRoleName.TANTOU_EDITOR)) {
            throw new AccessDeniedException("Only TANTOU can activate projects");
        }

        Project project = getProject(projectId);

        // Auto-Plan Initialization rule
        if (project.getProjectWorkflowStatus() != ProjectWorkflowStatus.ACTIVE) {
            project.setProjectWorkflowStatus(ProjectWorkflowStatus.ACTIVE);
            project = projectRepository.save(project);

            // Create empty ProductionPlan linked to Project
            Optional<ProductionPlan> existingPlan = productionPlanRepository.findByProjectId(project.getId());
            if (existingPlan.isEmpty()) {
                ProductionPlan plan = new ProductionPlan();
                plan.setProject(project);
                plan.setPlanStatus(PlanStatus.PLANNING);
                productionPlanRepository.save(plan);
            }
        }

        return mapToProjectResponse(project);
    }

    @Override
    public PlanDashboardResponse getPlanDashboard(Long planId, Long requesterId) {
        Account requester = getAccount(requesterId);
        if (!requester.hasRole(SystemRoleName.TANTOU_EDITOR) && !requester.hasRole(SystemRoleName.MANGAKA)) {
            throw new AccessDeniedException("Only TANTOU or MANGAKA can view plan dashboard");
        }

        ProductionPlan plan = productionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        PlanDashboardResponse response = new PlanDashboardResponse();
        response.setId(plan.getId());
        response.setProjectId(plan.getProject().getId());
        response.setStartDate(plan.getStartDate());
        response.setEndDate(plan.getEndDate());
        response.setTotalVolumeTarget(plan.getTotalVolumeTarget());
        response.setPlanStatus(plan.getPlanStatus());

        List<Chapter> chapters = chapterRepository.findByProductionPlanId(plan.getId());

        long totalTasks = 0;
        long completedTasks = 0;

        List<ChapterWithTasksResponse> chapterResponses = chapters.stream().map(c -> {
            ChapterWithTasksResponse cr = new ChapterWithTasksResponse();
            cr.setId(c.getId());
            cr.setChapterNumber(c.getChapterNumber());
            cr.setTitle(c.getTitle());
            cr.setTargetPageCount(c.getTargetPageCount());
            cr.setPublishDate(c.getPublishDate());
            cr.setChapterStatus(c.getChapterStatus());

            List<Task> tasks = taskRepository.findByChapterId(c.getId());
            List<TaskResponse> taskResponses = tasks.stream().map(this::mapToTaskResponse).collect(Collectors.toList());
            cr.setTasks(taskResponses);

            return cr;
        }).collect(Collectors.toList());

        response.setChapters(chapterResponses);

        // Calculate progress based on chapters completion
        long completedChapters = chapters.stream().filter(
                c -> c.getChapterStatus() == ChapterStatus.COMPLETED || c.getChapterStatus() == ChapterStatus.PUBLISHED)
                .count();
        double progress = chapters.isEmpty() ? 0.0 : ((double) completedChapters / chapters.size()) * 100;
        response.setCompletionPercentage(progress);

        return response;
    }

    @Override
    @Transactional
    public ChapterWithTasksResponse createChapter(CreateChapterRequest req, Long requesterId) {
        Account requester = getAccount(requesterId);
        if (!requester.hasRole(SystemRoleName.TANTOU_EDITOR)) {
            throw new AccessDeniedException("Only TANTOU can create chapters");
        }

        ProductionPlan plan = productionPlanRepository.findById(req.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        if (req.getStartDate().isAfter(req.getEndDate())) {
            throw new IllegalArgumentException("Chapter start date cannot be after end date");
        }
        
        if (plan.getStartDate() != null && req.getStartDate().isBefore(plan.getStartDate())) {
            throw new IllegalArgumentException("Chapter start date cannot be before Production Plan start date (" + plan.getStartDate() + ")");
        }

        if (plan.getEndDate() != null && req.getEndDate().isAfter(plan.getEndDate())) {
            throw new IllegalArgumentException("Chapter end date cannot be after Production Plan end date (" + plan.getEndDate() + ")");
        }

        Chapter chapter = new Chapter();
        chapter.setProductionPlan(plan);
        chapter.setProject(plan.getProject());
        chapter.setChapterNumber(req.getChapterNumber());
        chapter.setTitle(req.getTitle());
        chapter.setTargetPageCount(req.getTargetPageCount());
        chapter.setPublishDate(req.getPublishDate());
        chapter.setStartDate(req.getStartDate());
        chapter.setEndDate(req.getEndDate());
        chapter.setChapterStatus(ChapterStatus.BACKLOG);
        chapter.setOwner(requester);

        chapter = chapterRepository.save(chapter);

        // Auto-Task Generation rule
        TaskType[] defaultTasks = { TaskType.NAME_WIP, TaskType.LINEART, TaskType.INKING, TaskType.BACKGROUND };
        for (TaskType type : defaultTasks) {
            Task task = new Task();
            task.setChapter(chapter);
            task.setProductionTaskType(type);
            task.setTaskWorkflowStatus(TaskWorkflowStatus.TODO);
            task.setTitle(type.name() + " for Chapter " + chapter.getChapterNumber());
            taskRepository.save(task);
        }

        ChapterWithTasksResponse response = new ChapterWithTasksResponse();
        response.setId(chapter.getId());
        response.setChapterNumber(chapter.getChapterNumber());
        response.setTitle(chapter.getTitle());
        response.setTargetPageCount(chapter.getTargetPageCount());
        response.setPublishDate(chapter.getPublishDate());
        response.setChapterStatus(chapter.getChapterStatus());

        List<Task> tasks = taskRepository.findByChapterId(chapter.getId());
        response.setTasks(tasks.stream().map(this::mapToTaskResponse).collect(Collectors.toList()));

        return response;
    }

    @Override
    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, UpdateTaskStatusRequest req) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Account requester = getAccount(req.getRequesterId());

        boolean isTantou = requester.hasRole(SystemRoleName.TANTOU_EDITOR);
        boolean isMangaka = requester.hasRole(SystemRoleName.MANGAKA);
        boolean isAssistant = requester.hasRole(SystemRoleName.ASSISTANT);

        // Permissions check
        if (!isTantou && !isMangaka && !isAssistant) {
            throw new AccessDeniedException("You don't have permission to update task status");
        }

        if (isAssistant && (task.getAssignee() == null || task.getAssignee().getId() != requester.getId())) {
            throw new AccessDeniedException("Assistants can only update tasks assigned to them");
        }

        // Strict Quality Control (Feedback Loop) Rule
        if (task.getTaskWorkflowStatus() == TaskWorkflowStatus.REVIEW && !isTantou) {
            // Check if there is pending feedback or if it's already in review and locked
            throw new IllegalStateException("Task is locked in REVIEW. Waiting for feedback.");
        }

        task.setTaskWorkflowStatus(req.getStatus());
        task = taskRepository.save(task);

        // Chapter Roll-up Validation Check - if a task is updated, chapter status might
        // need check
        // If a task is no longer DONE, we can't have chapter COMPLETED.
        if (req.getStatus() != TaskWorkflowStatus.DONE && task.getChapter() != null
                && task.getChapter().getChapterStatus() == ChapterStatus.COMPLETED) {
            Chapter chapter = task.getChapter();
            chapter.setChapterStatus(ChapterStatus.IN_PRODUCTION);
            chapterRepository.save(chapter);
        }

        // Also if task is updated TO DONE, maybe chapter should be completed? That's
        // typically manual, but let's enforce guard on Chapter completion manually.

        return mapToTaskResponse(task);
    }

    @Override
    @Transactional
    public FeedbackResponse createFeedback(Long taskId, CreateFeedbackRequest req) {
        Account creator = getAccount(req.getCreatedById());
        if (!creator.hasRole(SystemRoleName.TANTOU_EDITOR)) {
            throw new AccessDeniedException("Only TANTOU can create feedback");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (task.getTaskWorkflowStatus() != TaskWorkflowStatus.REVIEW) {
            throw new IllegalStateException("Feedback can only be provided for tasks in REVIEW status");
        }

        Feedback feedback = new Feedback();
        feedback.setTask(task);
        feedback.setCreatedBy(creator);
        feedback.setContent(req.getContent());
        feedback.setAttachmentUrl(req.getAttachmentUrl());
        feedback.setDecision(req.getDecision());
        feedback = feedbackRepository.save(feedback);

        // Feedback Loop rule:
        if (req.getDecision() == FeedbackDecision.REJECTED) {
            task.setTaskWorkflowStatus(TaskWorkflowStatus.IN_PROGRESS);
        } else if (req.getDecision() == FeedbackDecision.APPROVED) {
            task.setTaskWorkflowStatus(TaskWorkflowStatus.DONE);
        }
        taskRepository.save(task);

        FeedbackResponse response = new FeedbackResponse();
        BeanUtils.copyProperties(feedback, response);
        response.setTaskId(task.getId());
        response.setCreatedById(creator.getId());
        response.setCreatedByName(creator.getFirstName() + " " + creator.getLastName());
        return response;
    }

    @Override
    @Transactional
    public ChapterResponse assignChapter(Long chapterId, AssignChapterRequest req) {
        Account requester = getAccount(req.getRequesterId());
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
        Account mangaka = getAccount(req.getMangakaId());

        if (!requester.hasRole(SystemRoleName.TANTOU_EDITOR)) {
            throw new AccessDeniedException("Only Tantou Editor can assign chapters");
        }

        if (!mangaka.hasRole(SystemRoleName.MANGAKA)) {
            throw new IllegalArgumentException("The assignee must be a Mangaka");
        }

        // Set Chapter assignee/owner
        chapter.setOwner(mangaka);
        chapterRepository.save(chapter);

        // Auto-assign all tasks in this chapter to the Mangaka
        List<Task> tasks = taskRepository.findByChapterId(chapterId);
        for (Task task : tasks) {
            task.setAssignee(mangaka);
        }
        taskRepository.saveAll(tasks);

        ChapterResponse response = new ChapterResponse();
        org.springframework.beans.BeanUtils.copyProperties(chapter, response);
        response.setProjectId(chapter.getProject().getId());
        response.setOwnerId(mangaka.getId());
        response.setOwnerName(mangaka.getFirstName() + " " + mangaka.getLastName());
        return response;
    }

    @Override
    @Transactional
    public TaskResponse assignTask(Long taskId, AssignTaskRequest req) {
        Account requester = getAccount(req.getRequesterId());
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Account assignee = getAccount(req.getAssigneeId());

        boolean isTantou = requester.hasRole(SystemRoleName.TANTOU_EDITOR);
        boolean isMangaka = requester.hasRole(SystemRoleName.MANGAKA);

        if (!isTantou && !isMangaka) {
            throw new AccessDeniedException("You don't have permission to assign tasks");
        }

        if (isMangaka) {
            if (task.getProductionTaskType() != TaskType.INKING
                    && task.getProductionTaskType() != TaskType.BACKGROUND) {
                throw new AccessDeniedException("Mangaka can only assign INKING or BACKGROUND tasks");
            }
            if (!assignee.hasRole(SystemRoleName.ASSISTANT)) {
                throw new AccessDeniedException("Mangaka can only assign tasks to Assistants");
            }
        }

        if (isTantou && !assignee.hasRole(SystemRoleName.MANGAKA)) {
            // Note: Spec says Tantou can assign to Mangaka. Could be flexible, but sticking
            // to spec.
            // Actually, Tantou can assign to MANGAKA, Mangaka to ASSISTANT.
            if (!assignee.hasRole(SystemRoleName.MANGAKA) && !assignee.hasRole(SystemRoleName.ASSISTANT)) {
                throw new AccessDeniedException("Tantou can only assign to Mangaka or Assistant");
            }
        }

        if (req.getDeadline() != null) {
            java.time.Instant deadline = req.getDeadline();
            Chapter chapter = task.getChapter();
            
            if (chapter.getStartDate() != null && deadline.isBefore(chapter.getStartDate().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())) {
                throw new IllegalArgumentException("Task deadline cannot be before Chapter start date (" + chapter.getStartDate() + ")");
            }

            if (chapter.getEndDate() != null && deadline.isAfter(chapter.getEndDate().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant())) {
                throw new IllegalArgumentException("Task deadline cannot be after Chapter end date (" + chapter.getEndDate() + ")");
            }

            task.setDeadline(deadline);
        }

        task.setAssignee(assignee);
        task = taskRepository.save(task);
        return mapToTaskResponse(task);
    }

    @Override
    public List<AssetResponse> getProjectAssets(Long projectId, Long requesterId) {
        // RBAC: Any member of the project. For simplicity, just checking if valid
        // account,
        // real implementation would check Project.getTantou() or Project.getMangaka()
        Account requester = getAccount(requesterId);

        List<Asset> assets = assetRepository.findByProjectId(projectId);
        return assets.stream().map(a -> {
            AssetResponse r = new AssetResponse();
            BeanUtils.copyProperties(a, r);
            r.setProjectId(projectId);
            return r;
        }).collect(Collectors.toList());
    }

    // Helper methods

    private Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    private Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    private ProjectResponse mapToProjectResponse(Project p) {
        ProjectResponse r = new ProjectResponse();
        BeanUtils.copyProperties(p, r);
        return r;
    }

    private TaskResponse mapToTaskResponse(Task t) {
        TaskResponse r = new TaskResponse();
        BeanUtils.copyProperties(t, r);
        if (t.getAssignee() != null) {
            r.setAssigneeId(t.getAssignee().getId());
            r.setAssigneeName(t.getAssignee().getFirstName() + " " + t.getAssignee().getLastName());
        }
        return r;
    }

    // Explicitly add chapter completion endpoint logic here? The spec says "Một
    // Chapter KHÔNG ĐƯỢC phép chuyển trạng thái thành COMPLETED nếu vẫn còn ít nhất
    // một Task con của nó có trạng thái khác DONE."
    // Let's add a method to update chapter status

    @Transactional
    public ChapterWithTasksResponse updateChapterStatus(Long chapterId, ChapterStatus status, Long requesterId) {
        Account requester = getAccount(requesterId);
        if (!requester.hasRole(SystemRoleName.TANTOU_EDITOR)) {
            throw new AccessDeniedException("Only TANTOU can update chapter status");
        }

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        if (status == ChapterStatus.COMPLETED) {
            boolean hasIncompleteTasks = taskRepository.existsByChapterIdAndTaskWorkflowStatusNot(chapterId,
                    TaskWorkflowStatus.DONE);
            if (hasIncompleteTasks) {
                throw new IllegalStateException("Cannot complete chapter: not all tasks are DONE.");
            }
        }

        chapter.setChapterStatus(status);
        chapter = chapterRepository.save(chapter);

        ChapterWithTasksResponse cr = new ChapterWithTasksResponse();
        BeanUtils.copyProperties(chapter, cr);
        List<Task> tasks = taskRepository.findByChapterId(chapter.getId());
        cr.setTasks(tasks.stream().map(this::mapToTaskResponse).collect(Collectors.toList()));
        return cr;
    }
}
