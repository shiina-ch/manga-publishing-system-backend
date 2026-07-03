package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Chapter")
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @Column(name = "ChapterNumber")
    private Integer chapterNumber;

    @Size(max = 255)
    @Nationalized
    @Column(name = "Title")
    private String title;

    @Size(max = 50)
    @Nationalized
    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "Pages")
    private Integer pages;

    @Column(name = "Deadline")
    private Instant deadline;

    @Size(max = 50)
    @Nationalized
    @Column(name = "Priority", length = 50)
    private String priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OwnerId")
    private Account owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProjectId")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductionPlanId")
    private ProductionPlan productionPlan;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SketchPage> sketchPages;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;
}