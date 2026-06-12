package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {
    @NotNull
    private SubmissionReview submissionReview;
    @NotNull
    private Account voter;
    @Size(max = 50)
    private String voteValue;
    private String comment;
    private Instant votedAt;
}
