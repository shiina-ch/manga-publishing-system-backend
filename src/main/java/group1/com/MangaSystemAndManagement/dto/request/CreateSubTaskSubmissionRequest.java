package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.SubmissionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Form-data body for the Assistant uploading a new round of files against a
 * <b>SubTask</b>. The SubTask identity is taken from the URL
 * ({@code POST /api/workflow/subtasks/{subTaskId}/submissions}), so this DTO
 * deliberately has <b>no</b> {@code subTaskId} field – the URL is the source of
 * truth.
 *
 * <p>Valid {@code submissionType} values are {@code ROUGH_SKETCH},
 * {@code REVISION}, or {@code FINAL}. {@code TASK_LEVEL} is rejected because
 * that is a different endpoint (see {@code POST /api/workflow/tasks/...}).</p>
 */
@Getter
@Setter
public class CreateSubTaskSubmissionRequest {

    @NotNull(message = "requesterId is required (the submitting Assistant's account id)")
    private Long requesterId;

    @NotNull(message = "submissionType is required (ROUGH_SKETCH|REVISION|FINAL)")
    private SubmissionType submissionType;

    /** Free-form note shown to the reviewer alongside the file(s). */
    private String note;

    /** At least one file is required – enforced by the service layer. */
    private List<MultipartFile> files;
}