package group1.com.MangaSystemAndManagement.controller;

import group1.com.MangaSystemAndManagement.dto.request.SubmissionRequest;
import group1.com.MangaSystemAndManagement.model.Submission;
import group1.com.MangaSystemAndManagement.service.interfaces.SubmissionService;
import group1.com.MangaSystemAndManagement.dto.response.ResponseBase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@Tag(name = "Submission", description = "Submission management APIs")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService service;

    /**
     * POST /api/submissions/{userId}
     *
     * Mangaka (userId) nộp bài submission kèm các file PSD.
     * @ModelAttribute tự binding toàn bộ form-data fields vào SubmissionRequest DTO:
     *   - title      : String
     *   - note       : String  (optional)
     *   - planningId : Long    (optional)
     *   - files      : List<MultipartFile>
     */
    @PostMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Submit files",
        description = "Mangaka tạo submission và upload các file PSD. " +
                      "userId truyền qua path param. Gửi form-data với fields: title, note, planningId, files[]"
    )
    public ResponseEntity<ResponseBase> submitFiles(
            @PathVariable Long userId,
            @ModelAttribute SubmissionRequest request) {
        try {
            Submission result = service.submitFiles(userId, request);
            return ResponseEntity.status(201).body(new ResponseBase(201, "Submission created successfully", result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(new ResponseBase(409, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @GetMapping
    @Operation(summary = "Get all submissions")
    public ResponseEntity<ResponseBase> findAll() {
        try {
            List<Submission> result = service.findAll();
            return ResponseEntity.status(200).body(new ResponseBase(200, "Success", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get submission by ID")
    public ResponseEntity<ResponseBase> findById(@PathVariable Long id) {
        try {
            return service.findById(id)
                    .map(result -> ResponseEntity.status(200).body(new ResponseBase(200, "Success", result)))
                    .orElseGet(() -> ResponseEntity.status(404).body(new ResponseBase(404, "Submission not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update submission metadata (title, note)")
    public ResponseEntity<ResponseBase> update(
            @PathVariable Long id,
            @ModelAttribute SubmissionRequest request) {
        try {
            Submission result = service.update(id, request);
            return ResponseEntity.status(200).body(new ResponseBase(200, "Updated successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(409).body(new ResponseBase(409, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete submission")
    public ResponseEntity<ResponseBase> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.status(200).body(new ResponseBase(200, "Deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(409).body(new ResponseBase(409, e.getMessage(), null));
        }
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve submission and auto-create Project")
    public ResponseEntity<ResponseBase> approveAndCreateProject(@PathVariable Long id) {
        try {
            Submission result = service.approveAndCreateProject(id);
            return ResponseEntity.status(200).body(new ResponseBase(200, "Submission approved and project created successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(409).body(new ResponseBase(409, e.getMessage(), null));
        }
    }
}
