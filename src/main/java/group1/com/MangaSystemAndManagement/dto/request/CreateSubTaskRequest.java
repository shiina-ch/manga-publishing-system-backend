package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.TaskType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Body to split a Task into smaller pieces for an Assistant.
 *
 * <p>Note: requirement #3 of the workflow mandates that the supplied
 * {@code deadlineDate} (+ optional {@code deadlineTime}) MUST NOT exceed the
 * parent Task's own deadline – the service validates this and rejects
 * with HTTP 400 otherwise.</p>
 *
 * <p>{@code deadlineTime} is intentionally typed as a raw string so the
 * service layer can accept both the strict ISO-8601 form ({@code 08:00},
 * {@code 08:00:00}) and the casual UI form ({@code 8am}, {@code 8:30pm}).
 * Parsing rules live in {@code SubTaskServiceImpl.parseDeadlineTime}.</p>
 */
@Getter
@Setter
public class CreateSubTaskRequest {

    @NotNull(message = "Requester account ID is required")
    private Long requesterId;

    @NotNull(message = "Assistant assignee ID is required")
    private Long assigneeId;

    @NotNull(message = "SubTask title is required")
    private String title;

    private String description;

    private TaskType productionTaskType;

    @NotNull(message = "Deadline date is required")
    private LocalDate deadlineDate;

    /** Optional – defaults to 23:59:59 (end-of-day) when omitted. See class javadoc. */
    private String deadlineTime;
}
