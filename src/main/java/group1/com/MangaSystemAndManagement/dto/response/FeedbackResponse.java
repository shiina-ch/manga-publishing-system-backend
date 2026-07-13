package group1.com.MangaSystemAndManagement.dto.response;

import group1.com.MangaSystemAndManagement.model.FeedbackDecision;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class FeedbackResponse {
    private Long id;
    private Long taskId;
    private Long createdById;
    private String createdByName;
    private String content;
    private String attachmentUrl;
    private FeedbackDecision decision;
    private Instant createdAt;
}
