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
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @NotNull
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SubmissionReviewId", nullable = false)
    private SubmissionReview submissionReview;

    @NotNull
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "VoterId", nullable = false)
    private Account voter;

    @Size(max = 50)
    @Nationalized
    @Column(name = "VoteValue", length = 50)
    private String voteValue;

    @Nationalized
    @Lob
    @Column(name = "Comment")
    private String comment;

    @ColumnDefault("getdate()")
    @Column(name = "VotedAt")
    private Instant votedAt;

}
