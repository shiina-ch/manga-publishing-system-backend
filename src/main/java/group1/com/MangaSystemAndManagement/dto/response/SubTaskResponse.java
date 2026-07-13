package group1.com.MangaSystemAndManagement.dto.response;

import group1.com.MangaSystemAndManagement.model.SubTask;
import group1.com.MangaSystemAndManagement.model.SubTaskWorkflowStatus;
import group1.com.MangaSystemAndManagement.model.TaskType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Wire-format representation of a {@link SubTask}.
 *
 * <p>The {@code isOverdue}, {@code deadlineInstant} and any "current submission"
 * fields are transient – they are recomputed on the fly so that clients always
 * see an up-to-date picture without us having to schedule a cron that flips
 * a stored boolean.</p>
 */
@Getter
@Setter
public class SubTaskResponse {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private Long assigneeId;
    private String assigneeName;
    private String title;
    private String description;
    private TaskType productionTaskType;
    private SubTaskWorkflowStatus subtaskStatus;
    private LocalDate deadlineDate;
    private LocalTime deadlineTime;
    private Instant deadlineInstant;
    private boolean overdue;
    private Integer currentSubmissionVersion;
    private Long currentSubmissionId;
    private String currentSubmissionStatus;
    private Instant createdAt;
    private Instant updatedAt;

    public static SubTaskResponse from(SubTask s) {
        SubTaskResponse r = new SubTaskResponse();
        r.id = s.getId();
        r.taskId = s.getTask() != null ? s.getTask().getId() : null;
        r.taskTitle = s.getTask() != null ? s.getTask().getTitle() : null;
        r.assigneeId = s.getAssignee() != null ? s.getAssignee().getId() : null;
        if (s.getAssignee() != null) {
            r.assigneeName = s.getAssignee().getFirstName() + " " + s.getAssignee().getLastName();
        }
        r.title = s.getTitle();
        r.description = s.getDescription();
        r.productionTaskType = s.getProductionTaskType();
        r.subtaskStatus = s.getSubtaskStatus();
        r.deadlineDate = s.getDeadlineDate();
        r.deadlineTime = s.getDeadlineTime();
        r.deadlineInstant = s.getDeadlineInstant();
        r.overdue = s.isOverdue();
        r.createdAt = s.getCreatedAt();
        r.updatedAt = s.getUpdatedAt();
        return r;
    }
}
