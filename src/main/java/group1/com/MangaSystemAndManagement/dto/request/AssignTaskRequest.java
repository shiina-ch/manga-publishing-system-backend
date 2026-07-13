package group1.com.MangaSystemAndManagement.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignTaskRequest {

    @NotNull(message = "Assignee account ID is required")
    private Long assigneeId;
    
    @NotNull(message = "Requester account ID is required")
    private Long requesterId;

    @NotNull(message = "Deadline is required")
    private java.time.Instant deadline;
}
