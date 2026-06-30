package group1.com.MangaSystemAndManagement.dto.response;

import group1.com.MangaSystemAndManagement.model.ReviewStage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionReviewResponse {
    private Long id;
    private Long submissionId;
    private Long reviewerId;
    private String reviewerEmail;
    private ReviewStage stage;
    private String decision;
    private String comment;
    private Instant reviewedAt;
}
