package group1.com.MangaSystemAndManagement.controller;
import group1.com.MangaSystemAndManagement.dto.request.VoteRequest;
import group1.com.MangaSystemAndManagement.dto.response.VoteResponse;
import group1.com.MangaSystemAndManagement.dto.response.VoteSummaryResponse;
import group1.com.MangaSystemAndManagement.exception.EntityNotFoundException;
import group1.com.MangaSystemAndManagement.service.interfaces.VoteService;
import group1.com.MangaSystemAndManagement.dto.response.ResponseBase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/votes")
@Tag(name = "Vote", description = "Vote management APIs")
@RequiredArgsConstructor
public class VoteController {
    private final VoteService service;

    @PostMapping
    public ResponseEntity<ResponseBase> create(@Valid @RequestBody VoteRequest request) {
        VoteResponse result = service.create(request);
        return ResponseEntity.status(201).body(new ResponseBase(201, "Vote submitted successfully", result));
    }

    @GetMapping
    public ResponseEntity<ResponseBase> findAll() {
        List<VoteResponse> result = service.findAll();
        return ResponseEntity.ok(new ResponseBase(200, "Success", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseBase> findById(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseBase(200, "Success", service.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseBase> update(@PathVariable Long id, @Valid @RequestBody VoteRequest request) {
        VoteResponse result = service.update(id, request);
        return ResponseEntity.ok(new ResponseBase(200, "Updated successfully", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseBase> delete(@PathVariable Long id, @RequestParam Long voterId) {
        service.delete(id, voterId);
        return ResponseEntity.ok(new ResponseBase(200, "Deleted successfully", null));
    }

    @GetMapping("/submission-review/{submissionReviewId}")
    public ResponseEntity<ResponseBase> findBySubmissionReview(
            @PathVariable Long submissionReviewId) {
        List<VoteResponse> result = service.findBySubmissionReviewId(submissionReviewId);
        return ResponseEntity.ok(new ResponseBase(200, "Success", result));
    }

    @GetMapping("/submission-review/{submissionReviewId}/summary")
    public ResponseEntity<ResponseBase> getSummary(@PathVariable Long submissionReviewId) {
        VoteSummaryResponse result = service.getSummary(submissionReviewId);
        return ResponseEntity.ok(new ResponseBase(200, "Success", result));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseBase> handleNotFound(EntityNotFoundException exception) {
        return ResponseEntity.status(404).body(new ResponseBase(404, exception.getMessage(), null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseBase> handleForbidden(AccessDeniedException exception) {
        return ResponseEntity.status(403).body(new ResponseBase(403, exception.getMessage(), null));
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ResponseBase> handleBadRequest(Exception exception) {
        String message = exception instanceof HttpMessageNotReadableException
                ? "Invalid request body or unsupported voteValue"
                : exception.getMessage();
        return ResponseEntity.badRequest().body(new ResponseBase(400, message, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseBase> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Invalid request");
        return ResponseEntity.badRequest().body(new ResponseBase(400, message, null));
    }
}
