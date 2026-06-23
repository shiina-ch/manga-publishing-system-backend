package group1.com.MangaSystemAndManagement.dto.response;

import group1.com.MangaSystemAndManagement.model.VoteValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponse {
    private Long id;
    private Long submissionReviewId;
    private Long voterId;
    private VoteValue voteValue;
    private String comment;
    private Instant votedAt;
}
