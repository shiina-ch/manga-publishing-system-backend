package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.ProjectWorkflowStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProjectStatusRequest {

    @NotNull(message = "Status is required")
    private ProjectWorkflowStatus status;
}
