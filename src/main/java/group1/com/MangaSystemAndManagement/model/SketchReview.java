package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "SketchReview")
public class SketchReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @NotNull
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SketchPageId", nullable = false)
    private SketchPage sketchPage;

    @NotNull
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ReviewerId", nullable = false)
    private Account reviewer;

    @Size(max = 50)
    @Nationalized
    @Column(name = "Decision", length = 50)
    private String decision;

    @Nationalized
    @Lob
    @Column(name = "Comment")
    private String comment;

    @Nationalized
    @Lob
    @Column(name = "LayoutFeedback")
    private String layoutFeedback;

    @Nationalized
    @Lob
    @Column(name = "DetailsFeedback")
    private String detailsFeedback;

    @ColumnDefault("getdate()")
    @Column(name = "ReviewedAt")
    private Instant reviewedAt;
}
