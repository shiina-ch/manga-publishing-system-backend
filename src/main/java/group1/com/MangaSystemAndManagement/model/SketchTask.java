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
@Table(name = "SketchTask")
public class SketchTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @NotNull
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SketchPageId", nullable = false)
    private SketchPage sketchPage;

    @Size(max = 100)
    @Nationalized
    @Column(name = "TaskType", length = 100)
    private String taskType;

    @Nationalized
    @Lob
    @Column(name = "Description")
    private String description;

    @NotNull
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AssignedTo", nullable = false)
    private Account assignedTo;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "CompletedUrl", length = 1000)
    private String completedUrl;

    @Size(max = 50)
    @Nationalized
    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "CompletedAt")
    private Instant completedAt;
}
