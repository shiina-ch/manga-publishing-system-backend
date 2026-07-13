package group1.com.MangaSystemAndManagement.controller;

import group1.com.MangaSystemAndManagement.dto.request.CreateSubTaskSubmissionRequest;
import group1.com.MangaSystemAndManagement.dto.request.CreateTaskSubmissionRequest;
import group1.com.MangaSystemAndManagement.dto.request.ReviewSubmissionRequest;
import group1.com.MangaSystemAndManagement.dto.response.ResponseBase;
import group1.com.MangaSystemAndManagement.dto.response.SubmissionResponse;
import group1.com.MangaSystemAndManagement.exception.EntityNotFoundException;
import group1.com.MangaSystemAndManagement.exception.ResourceNotFoundException;
import group1.com.MangaSystemAndManagement.exception.WorkflowRuleViolationException;
import group1.com.MangaSystemAndManagement.service.interfaces.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Polymorphic Submission resources for the production pipeline.
 *
 * <p>This controller intentionally lives at <b>{@code /api/workflow/...}</b>
 * — a fresh, dedicated URL namespace — so it never collides with the legacy
 * {@code SubmissionController} that serves the original "Mangaka nộp prototype"
 * flow at <b>{@code /api/submissions/{userId}}</b>. The two controllers are
 * owned by the same {@link SubmissionService} but serve two different business
 * flows:</p>
 *
 * <ul>
 *   <li><b>Legacy</b>: {@code POST /api/submissions/{userId}} – Mangaka tạo submission
 *       cùng PSD prototype gắn với {@code planningId}.</li>
 *   <li><b>Production pipeline (this controller)</b> – URL describes the
 *       relationship, so the request body never has to disambiguate
 *       <i>which</i> thing is being submitted to:
 *     <ul>
 *       <li>{@code POST /api/workflow/subtasks/{subTaskId}/submissions} –
 *           Assistant uploads a round against a {@code SubTask}.
 *           Body has <i>no</i> {@code subTaskId} (it comes from the URL).
 *           {@code submissionType} must be
 *           {@code ROUGH_SKETCH|REVISION|FINAL}.</li>
 *       <li>{@code POST /api/workflow/tasks/{taskId}/submissions} –
 *           Mangaka submits a {@code TASK_LEVEL} round against a {@code Task}
 *           (handed up to Tantō). Body has <i>no</i> {@code taskId}.
 *           {@code submissionType} must be {@code TASK_LEVEL}.</li>
 *       <li>{@code POST /api/workflow/submissions/{submissionId}/reviews} –
 *           Mangaka approves/rejects a SubTask round; Tantō does the same for
 *           {@code TASK_LEVEL}.</li>
 *       <li>{@code GET /api/workflow/subtasks/{subTaskId}/submissions} –
 *           history of every round for a SubTask.</li>
 *       <li>{@code GET /api/workflow/tasks/{taskId}/submissions} – history of
 *           every {@code TASK_LEVEL} round for a Task.</li>
 *     </ul>
 *   </li>
 * </ul>
 */
@RestController
@RequestMapping("/api/workflow")
@Tag(name = "Production Submissions",
     description = "Polymorphic submission rounds inside the production pipeline (SubTask / Task). "
             + "Distinct from the legacy /api/submissions endpoint.")
@RequiredArgsConstructor
public class ProductionSubmissionController {

    private final SubmissionService submissionService;

    // =====================================================================
    // 1. Assistant uploads a round against a SubTask
    // =====================================================================
    @PostMapping(value = "/subtasks/{subTaskId}/submissions",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Assistant uploads a round against a SubTask",
               description = "Form-data: requesterId, submissionType "
                       + "(ROUGH_SKETCH|REVISION|FINAL), note?, files[]. The SubTask is "
                       + "identified by {subTaskId} in the URL, not in the body.")
    public ResponseEntity<ResponseBase> createForSubTask(
            @PathVariable Long subTaskId,
            @ModelAttribute CreateSubTaskSubmissionRequest request) {
        try {
            SubmissionResponse result = submissionService.createForSubTask(subTaskId, request);
            return ResponseEntity.status(201)
                    .body(new ResponseBase(201, "Submission created", result));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (WorkflowRuleViolationException w) {
            return ResponseEntity.status(400).body(new ResponseBase(400, w.getMessage(), null));
        } catch (ResourceNotFoundException r) {
            return ResponseEntity.status(404).body(new ResponseBase(404, r.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    // =====================================================================
    // 2. Mangaka submits a TASK_LEVEL round against a Task (Mangaka → Tantō)
    // =====================================================================
    @PostMapping(value = "/tasks/{taskId}/submissions",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Mangaka submits a TASK_LEVEL round against a Task",
               description = "Form-data: requesterId, submissionType (=TASK_LEVEL), note?, files[]. "
                       + "The Task is identified by {taskId} in the URL, not in the body.")
    public ResponseEntity<ResponseBase> createForTask(
            @PathVariable Long taskId,
            @ModelAttribute CreateTaskSubmissionRequest request) {
        try {
            SubmissionResponse result = submissionService.createForTask(taskId, request);
            return ResponseEntity.status(201)
                    .body(new ResponseBase(201, "Submission created", result));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (WorkflowRuleViolationException w) {
            return ResponseEntity.status(400).body(new ResponseBase(400, w.getMessage(), null));
        } catch (ResourceNotFoundException r) {
            return ResponseEntity.status(404).body(new ResponseBase(404, r.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    // =====================================================================
    // 3. Review (approve / reject) a submission round
    // =====================================================================
    @PostMapping("/submissions/{submissionId}/reviews")
    @Operation(summary = "Record a review decision for a production submission",
               description = "Mangaka approves / rejects a SubTask round; Tantō does the same "
                       + "for TASK_LEVEL rounds. Body: { reviewerId, decision: APPROVED|REJECTED, "
                       + "note? }. Rejection requires a non-empty note.")
    public ResponseEntity<ResponseBase> review(@PathVariable Long submissionId,
                                               @Valid @RequestBody ReviewSubmissionRequest req) {
        try {
            SubmissionResponse result = submissionService.review(submissionId, req);
            return ResponseEntity.status(201)
                    .body(new ResponseBase(201, "Review recorded", result));
        } catch (AccessDeniedException ad) {
            return ResponseEntity.status(403).body(new ResponseBase(403, ad.getMessage(), null));
        } catch (WorkflowRuleViolationException w) {
            return ResponseEntity.status(400).body(new ResponseBase(400, w.getMessage(), null));
        } catch (ResourceNotFoundException r) {
            return ResponseEntity.status(404).body(new ResponseBase(404, r.getMessage(), null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(new ResponseBase(404, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    // =====================================================================
    // 4. History queries – nested under their owner
    // =====================================================================

    /** SubTask → submissions (Assistant upload rounds, Mangaka review rounds). */
    @GetMapping("/subtasks/{subTaskId}/submissions")
    @Operation(summary = "List every submission round for a SubTask (newest first)")
    public ResponseEntity<ResponseBase> listBySubTask(@PathVariable Long subTaskId) {
        try {
            List<SubmissionResponse> result = submissionService.historyBySubTask(subTaskId);
            return ResponseEntity.ok(new ResponseBase(200, "Submissions retrieved", result));
        } catch (ResourceNotFoundException r) {
            return ResponseEntity.status(404).body(new ResponseBase(404, r.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    /** Task → submissions (TASK_LEVEL rounds submitted up to Tantō). */
    @GetMapping("/tasks/{taskId}/submissions")
    @Operation(summary = "List every TASK_LEVEL submission round for a Task (newest first)")
    public ResponseEntity<ResponseBase> listByTask(@PathVariable Long taskId) {
        try {
            List<SubmissionResponse> result = submissionService.historyByTask(taskId);
            return ResponseEntity.ok(new ResponseBase(200, "Submissions retrieved", result));
        } catch (ResourceNotFoundException r) {
            return ResponseEntity.status(404).body(new ResponseBase(404, r.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }
}