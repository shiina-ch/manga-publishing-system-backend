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
public class SubmissionRequest {
    @NotNull
    private Project project;
    private Planning planning;
    @NotNull
    private Account submittedBy;
    @Size(max = 255)
    private String title;
    @Size(max = 1000)
    private String contentUrl;
    @Size(max = 50)
    private String status;
    private Instant submittedAt;
}
