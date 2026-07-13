package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.SubmissionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Form-data body for the Mangaka submitting a <b>Task-level</b> round of files
 * up to the Tantō. The Task identity is taken from the URL
 * ({@code POST /api/workflow/tasks/{taskId}/submissions}), so this DTO
 * deliberately has <b>no</b> {@code taskId} field – the URL is the source of
 * truth.
 *
 * <p>{@code submissionType} must be {@code TASK_LEVEL} – any other value is
 * rejected by the service layer.</p>
 */
@Getter
@Setter
public class CreateTaskSubmissionRequest {

    @NotNull(message = "requesterId is required (the submitting Mangaka's account id)")
    private Long requesterId;

    @NotNull(message = "submissionType is required (must be TASK_LEVEL for Task-level submissions)")
    private SubmissionType submissionType;

    /** Free-form note shown to the Tantō alongside the file(s). */
    private String note;

    /** At least one file is required – enforced by the service layer. */
    private List<MultipartFile> files;
}