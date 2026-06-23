package group1.com.MangaSystemAndManagement.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(
        name = "Vote",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_Vote_SubmissionReview_Voter",
                columnNames = {"SubmissionReviewId", "VoterId"}
        )
)
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

    @NotNull
    @Enumerated(EnumType.STRING)
    @Nationalized
    @Column(name = "VoteValue", nullable = false, length = 50)
    private VoteValue voteValue;

    @Nationalized
    @Lob
    @Column(name = "Comment")
    private String comment;

    @NotNull
    @Column(name = "VotedAt", nullable = false)
    private Instant votedAt;

}
