package group1.com.MangaSystemAndManagement.dto.response;

import group1.com.MangaSystemAndManagement.model.VoteResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteSummaryResponse {
    private Long submissionReviewId;
    private long approveCount;
    private long rejectCount;
    private long totalVotes;
    private VoteResult result;
}
