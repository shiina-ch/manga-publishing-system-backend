package group1.com.MangaSystemAndManagement.dto.response;

import group1.com.MangaSystemAndManagement.model.TaskType;
import group1.com.MangaSystemAndManagement.model.TaskWorkflowStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TaskResponse {
    private Long id;
    private String title;
    private TaskType productionTaskType;
    private TaskWorkflowStatus taskWorkflowStatus;
    private String acceptanceCriteria;
    private Instant deadline;
    private Long assigneeId;
    private String assigneeName;
}
