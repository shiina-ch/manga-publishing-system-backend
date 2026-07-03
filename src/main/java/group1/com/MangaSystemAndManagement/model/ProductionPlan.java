package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
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

    @OneToMany(mappedBy = "productionPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chapter> chapters;
}
