package group1.com.MangaSystemAndManagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "ProductionPlan")
public class ProductionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProjectId", nullable = false, unique = true)
    @JsonIgnore
    private Project project;

    @Nationalized
    @Lob
    @Column(name = "Milestones")
    private String milestones;

    @Nationalized
    @Lob
    @Column(name = "Schedule")
    private String schedule;

    @Nationalized
    @Lob
    @Column(name = "ChapterTimeline")
    private String chapterTimeline;

    @Column(name = "Deadline")
    private Instant deadline;

    @Nationalized
    @Lob
    @Column(name = "Resources")
    private String resources;

    @Column(name = "Budget")
    private Double budget;

    @Nationalized
    @Lob
    @Column(name = "AssistantAllocation")
    private String assistantAllocation;

    @Nationalized
    @Column(name = "Priority", length = 50)
    private String priority;

    @Nationalized
    @Lob
    @Column(name = "Risk")
    private String risk;

    @Nationalized
    @Column(name = "ApprovalStatus", length = 50)
    private String approvalStatus;

    // --- Production Workflow Fields ---

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "total_volume_target")
    private Integer totalVolumeTarget;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_status", length = 50)
    private PlanStatus planStatus = PlanStatus.PLANNING;

    /**
     * Rolled-up completion across all chapters of this Plan (0–100).
     * Recomputed every time a chapter transitions to COMPLETED/PUBLISHED.
     */
    @Column(name = "completion_percentage")
    private Integer completionPercentage = 0;

    @OneToMany(mappedBy = "productionPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chapter> chapters;
}
