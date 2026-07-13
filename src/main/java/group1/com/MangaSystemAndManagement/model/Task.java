package group1.com.MangaSystemAndManagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "Task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PageId")
    @JsonIgnore
    private Page page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ChapterId")
    @JsonIgnore
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AssignedTo")
    @JsonIgnore
    private Account assignedTo;

    @Size(max = 255)
    @Nationalized
    @Column(name = "Title")
    private String title;

    @Nationalized
    @Lob
    @Column(name = "Description")
    private String description;

    @Size(max = 50)
    @Nationalized
    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "Deadline")
    private Instant deadline;

    @Size(max = 50)
    @Nationalized
    @Column(name = "TaskType", length = 50)
    private String taskType;

    @Size(max = 50)
    @Nationalized
    @Column(name = "Priority", length = 50)
    private String priority;

    @Nationalized
    @Lob
    @Column(name = "ReviewResult")
    private String reviewResult;

    // --- Production Workflow Fields ---

    @Enumerated(EnumType.STRING)
    @Column(name = "production_task_type", length = 50)
    private TaskType productionTaskType;

    @Lob
    @Column(name = "acceptance_criteria")
    private String acceptanceCriteria;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status", length = 50)
    private TaskWorkflowStatus taskWorkflowStatus = TaskWorkflowStatus.TODO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    @JsonIgnore
    private Account assignee;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Feedback> feedbacks;

    /**
     * Rolled-up completion percentage in [0, 100], recomputed every time a
     * SubTask's status changes. The number of COMPLETED SubTasks divided by
     * total SubTask count – exposed so the dashboard can render the bar
     * without a per-request aggregation query.
     */
    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    // --- V4: dedicated date+time deadline columns (kept nullable so legacy
    //     rows with only the Instant Deadline column are untouched).
    @Column(name = "deadline_date")
    private LocalDate deadlineDate;

    @Column(name = "deadline_time")
    private LocalTime deadlineTime;
}
