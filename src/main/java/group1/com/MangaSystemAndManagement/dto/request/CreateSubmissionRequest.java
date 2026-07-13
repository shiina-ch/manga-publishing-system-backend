package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.SubmissionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Body for an Assistant (or Mangaka) uploading one round of files to a SubTask
 * (or, eventually, a Task-level submission).
 *
 * <p>Exactly one of {@code subTaskId} / {@code taskId} must be supplied and the
 * supplied {@code submissionType} must respect the workflow rule chain – the
 * service translates violations into HTTP 400.</p>
 */
@Getter
@Setter
public class CreateSubmissionRequest {

    @NotNull(message = "Requester account ID is required")
    private Long requesterId;

    /** SubTask target (mutually exclusive with taskId). */
    private Long subTaskId;

    /** Task target – only valid for TASK_LEVEL submissions. */
    private Long taskId;

    @NotNull(message = "submissionType is required (ROUGH_SKETCH|REVISION|FINAL|TASK_LEVEL)")
    private SubmissionType submissionType;

    /** Free-form note shown to the reviewer alongside the file(s). */
    private String note;

    /** At least one file is required – enforced by the service layer. */
    private List<MultipartFile> files;
}
