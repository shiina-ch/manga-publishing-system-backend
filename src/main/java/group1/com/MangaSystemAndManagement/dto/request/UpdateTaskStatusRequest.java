package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.TaskWorkflowStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskStatusRequest {

    @NotNull(message = "Requester account ID is required")
    private Long requesterId;

    @NotNull(message = "New status is required")
    private TaskWorkflowStatus status;
}
