package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "Task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PageId")
    private Page page;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ChapterId")
    private Chapter chapter;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AssignedTo")
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
}
