package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "DevelopmentPlan")
public class DevelopmentPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProjectId", nullable = false, unique = true)
    private Project project;

    @Nationalized
    @Lob
    @Column(name = "StoryDirection")
    private String storyDirection;

    @Nationalized
    @Lob
    @Column(name = "WorldSetting")
    private String worldSetting;

    @Nationalized
    @Lob
    @Column(name = "MainCharacters")
    private String mainCharacters;

    @Nationalized
    @Lob
    @Column(name = "ArcPlanning")
    private String arcPlanning;

    @Column(name = "EstimatedVolumes")
    private Integer estimatedVolumes;

    @Column(name = "EstimatedChapters")
    private Integer estimatedChapters;

    @Nationalized
    @Column(name = "TargetAudience", length = 255)
    private String targetAudience;

    @Nationalized
    @Column(name = "ReleaseStrategy", length = 255)
    private String releaseStrategy;

    @Nationalized
    @Column(name = "BusinessGoal", length = 255)
    private String businessGoal;

    @Nationalized
    @Lob
    @Column(name = "Notes")
    private String notes;

    @Nationalized
    @Column(name = "ApprovalStatus", length = 50)
    private String approvalStatus;
}
