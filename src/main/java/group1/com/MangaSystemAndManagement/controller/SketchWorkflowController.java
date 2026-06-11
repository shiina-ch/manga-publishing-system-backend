package group1.com.MangaSystemAndManagement.controller;

import group1.com.MangaSystemAndManagement.dto.request.AssignSketchTaskRequest;
import group1.com.MangaSystemAndManagement.dto.request.CompleteSketchTaskRequest;
import group1.com.MangaSystemAndManagement.dto.request.CreateSketchPageRequest;
import group1.com.MangaSystemAndManagement.dto.request.SubmitSketchReviewRequest;
import group1.com.MangaSystemAndManagement.dto.response.ResponseBase;
import group1.com.MangaSystemAndManagement.model.SketchPage;
import group1.com.MangaSystemAndManagement.model.SketchReview;
import group1.com.MangaSystemAndManagement.model.SketchTask;
import group1.com.MangaSystemAndManagement.service.interfaces.SketchWorkflowService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow/sketch")
@Tag(name = "SketchWorkflow", description = "Sketch creation, task assignment, completion, and Tantor review flow")
@RequiredArgsConstructor
public class SketchWorkflowController {

    private final SketchWorkflowService workflowService;

    @PostMapping("/create")
    public ResponseEntity<ResponseBase> createSketchPage(@RequestBody CreateSketchPageRequest req) {
        try {
            SketchPage saved = workflowService.createSketchPage(req);
            return ResponseEntity.status(201).body(new ResponseBase(201, "Sketch page created", saved));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (RuntimeException re) {
            String msg = re.getMessage() == null ? "" : re.getMessage();
            if (msg.toLowerCase().contains("not found")) {
                return ResponseEntity.status(404).body(new ResponseBase(404, msg, null));
            }
            return ResponseEntity.status(400).body(new ResponseBase(400, msg, null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @PostMapping("/assign-tasks")
    public ResponseEntity<ResponseBase> assignTasksToAssistants(@RequestBody AssignSketchTaskRequest req) {
        try {
            workflowService.assignTasksToAssistants(req);
            return ResponseEntity.status(200).body(new ResponseBase(200, "Tasks assigned", null));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (RuntimeException re) {
            String msg = re.getMessage() == null ? "" : re.getMessage();
            if (msg.toLowerCase().contains("not found")) {
                return ResponseEntity.status(404).body(new ResponseBase(404, msg, null));
            }
            return ResponseEntity.status(400).body(new ResponseBase(400, msg, null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @PostMapping("/complete-task")
    public ResponseEntity<ResponseBase> completeSketchTask(@RequestBody CompleteSketchTaskRequest req) {
        try {
            SketchTask completed = workflowService.completeSketchTask(req);
            return ResponseEntity.status(200).body(new ResponseBase(200, "Task completed", completed));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (RuntimeException re) {
            String msg = re.getMessage() == null ? "" : re.getMessage();
            if (msg.toLowerCase().contains("not found")) {
                return ResponseEntity.status(404).body(new ResponseBase(404, msg, null));
            }
            return ResponseEntity.status(400).body(new ResponseBase(400, msg, null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @PostMapping("/submit-review")
    public ResponseEntity<ResponseBase> submitSketchForReview(
            @RequestParam Long sketchPageId,
            @RequestParam Long mangakaId) {
        try {
            workflowService.submitSketchForReview(sketchPageId, mangakaId);
            return ResponseEntity.status(200).body(new ResponseBase(200, "Sketch submitted for review", null));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (RuntimeException re) {
            String msg = re.getMessage() == null ? "" : re.getMessage();
            if (msg.toLowerCase().contains("not found")) {
                return ResponseEntity.status(404).body(new ResponseBase(404, msg, null));
            }
            return ResponseEntity.status(400).body(new ResponseBase(400, msg, null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @PostMapping("/review")
    public ResponseEntity<ResponseBase> reviewSketch(@RequestBody SubmitSketchReviewRequest req) {
        try {
            SketchReview review = workflowService.reviewSketch(req);
            return ResponseEntity.status(200).body(new ResponseBase(200, "Review submitted", review));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (RuntimeException re) {
            String msg = re.getMessage() == null ? "" : re.getMessage();
            if (msg.toLowerCase().contains("not found")) {
                return ResponseEntity.status(404).body(new ResponseBase(404, msg, null));
            }
            return ResponseEntity.status(400).body(new ResponseBase(400, msg, null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @PostMapping("/request-changes")
    public ResponseEntity<ResponseBase> requestSketchChanges(
            @RequestParam Long sketchPageId,
            @RequestParam Long reviewerId,
            @RequestParam(required = false) String comment) {
        try {
            workflowService.requestSketchChanges(sketchPageId, reviewerId, comment);
            return ResponseEntity.status(200).body(new ResponseBase(200, "Changes requested", null));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (RuntimeException re) {
            String msg = re.getMessage() == null ? "" : re.getMessage();
            if (msg.toLowerCase().contains("not found")) {
                return ResponseEntity.status(404).body(new ResponseBase(404, msg, null));
            }
            return ResponseEntity.status(400).body(new ResponseBase(400, msg, null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @GetMapping("/status/{sketchPageId}")
    public ResponseEntity<ResponseBase> getSketchPageStatus(@PathVariable Long sketchPageId) {
        try {
            SketchPage status = workflowService.getSketchPageStatus(sketchPageId);
            return ResponseEntity.ok(new ResponseBase(200, "Success", status));
        } catch (RuntimeException re) {
            String msg = re.getMessage() == null ? "" : re.getMessage();
            if (msg.toLowerCase().contains("not found")) {
                return ResponseEntity.status(404).body(new ResponseBase(404, msg, null));
            }
            return ResponseEntity.status(400).body(new ResponseBase(400, msg, null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }
}
