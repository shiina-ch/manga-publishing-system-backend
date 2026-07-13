package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.FeedbackDecision;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFeedbackRequest {

    @NotNull(message = "Requester account ID is required")
    private Long createdById;

    @NotBlank(message = "Content is required")
    private String content;

    private String attachmentUrl;

    @NotNull(message = "Decision is required")
    private FeedbackDecision decision;
}
