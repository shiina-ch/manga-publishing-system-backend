package group1.com.MangaSystemAndManagement.controller;

import group1.com.MangaSystemAndManagement.dto.request.*;
import group1.com.MangaSystemAndManagement.dto.response.ResponseBase;
import group1.com.MangaSystemAndManagement.model.ChapterStatus;
import group1.com.MangaSystemAndManagement.model.ProjectWorkflowStatus;
import group1.com.MangaSystemAndManagement.service.interfaces.ProductionWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow")
@Tag(name = "Production Workflow", description = "Manga production pipeline and task management")
@RequiredArgsConstructor
public class ProductionWorkflowController {

    private final ProductionWorkflowService workflowService;

    // --- Project & Plan Management ---

    @PostMapping("/projects")
    @Operation(summary = "Create a new project and assign Tantou (Editorial Board only)")
    public ResponseEntity<ResponseBase> createProject(@Valid @RequestBody CreateProjectRequest req,
            @RequestParam Long editorId) {
        try {
            var res = workflowService.createProject(req, editorId);
            return ResponseEntity.status(201).body(new ResponseBase(201, "Project created", res));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @PutMapping("/projects/{projectId}/status")
    @Operation(summary = "Update project status -> Triggers auto-plan creation if ACTIVE (Tantou only)")
    public ResponseEntity<ResponseBase> activateProject(@PathVariable Long projectId, @RequestParam Long tantouId) {
        try {
            var res = workflowService.activateProject(projectId, tantouId);
            return ResponseEntity.ok(new ResponseBase(200, "Project status updated", res));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @GetMapping("/production-plans/{planId}/dashboard")
    @Operation(summary = "Get full timeline, chapters, and progress of a Plan")
    public ResponseEntity<ResponseBase> getPlanDashboard(@PathVariable Long planId, @RequestParam Long requesterId) {
        try {
            var res = workflowService.getPlanDashboard(planId, requesterId);
            return ResponseEntity.ok(new ResponseBase(200, "Dashboard retrieved", res));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    // --- Production Pipeline ---

    @PostMapping("/chapters")
    @Operation(summary = "Create a new chapter -> Triggers auto-generation of 4 default tasks (Tantou only)")
    public ResponseEntity<ResponseBase> createChapter(@Valid @RequestBody CreateChapterRequest req,
            @RequestParam Long requesterId) {
        try {
            var res = workflowService.createChapter(req, requesterId);
            return ResponseEntity.status(201).body(new ResponseBase(201, "Chapter and default tasks created", res));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @PostMapping("/chapters/{chapterId}/assign")
    @Operation(summary = "Assign a Chapter to a Mangaka -> Auto-assigns all tasks in the chapter to the Mangaka")
    public ResponseEntity<ResponseBase> assignChapter(@PathVariable Long chapterId, @Valid @RequestBody AssignChapterRequest req) {
        try {
            var res = workflowService.assignChapter(chapterId, req);
            return ResponseEntity.ok(new ResponseBase(200, "Chapter assigned to Mangaka", res));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @PutMapping("/chapters/{chapterId}/status")
    @Operation(summary = "Update chapter status -> Roll-up validation prevents COMPLETED if tasks aren't DONE (Tantou only)")
    public ResponseEntity<ResponseBase> updateChapterStatus(@PathVariable Long chapterId, @RequestParam ChapterStatus status,
            @RequestParam Long requesterId) {
        try {
            var res = workflowService.updateChapterStatus(chapterId, status, requesterId);
            return ResponseEntity.ok(new ResponseBase(200, "Chapter status updated", res));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(400).body(new ResponseBase(400, ise.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @PutMapping("/tasks/{taskId}/status")
    @Operation(summary = "Update task status -> Setting to REVIEW locks the task for Feedback")
    public ResponseEntity<ResponseBase> updateTaskStatus(@PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest req) {
        try {
            var res = workflowService.updateTaskStatus(taskId, req);
            return ResponseEntity.ok(new ResponseBase(200, "Task status updated", res));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(400).body(new ResponseBase(400, ise.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @PostMapping("/tasks/{taskId}/feedback")
    @Operation(summary = "Submit Tantou feedback on a task -> Triggers task status update (APPROVED=DONE, REJECTED=IN_PROGRESS)")
    public ResponseEntity<ResponseBase> submitFeedback(@PathVariable Long taskId,
            @Valid @RequestBody CreateFeedbackRequest req) {
        try {
            var res = workflowService.createFeedback(taskId, req);
            return ResponseEntity.status(201).body(new ResponseBase(201, "Feedback submitted and task updated", res));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(400).body(new ResponseBase(400, ise.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @PostMapping("/tasks/{taskId}/assign")
    @Operation(summary = "Assign a task to a user")
    public ResponseEntity<ResponseBase> assignTask(@PathVariable Long taskId, @Valid @RequestBody AssignTaskRequest req) {
        try {
            var res = workflowService.assignTask(taskId, req);
            return ResponseEntity.ok(new ResponseBase(200, "Task assigned", res));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @GetMapping("/projects/{projectId}/assets")
    @Operation(summary = "List project assets")
    public ResponseEntity<ResponseBase> getProjectAssets(@PathVariable Long projectId, @RequestParam Long requesterId) {
        try {
            var res = workflowService.getProjectAssets(projectId, requesterId);
            return ResponseEntity.ok(new ResponseBase(200, "Assets retrieved", res));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }
}
