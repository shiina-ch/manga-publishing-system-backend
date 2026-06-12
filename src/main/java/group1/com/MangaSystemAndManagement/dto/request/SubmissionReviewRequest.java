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
public class SubmissionReviewRequest {
    @NotNull
    private Submission submission;
    @NotNull
    private Account reviewer;
    @Size(max = 50)
    private String decision;
    private String comment;
    private Instant reviewedAt;
}
