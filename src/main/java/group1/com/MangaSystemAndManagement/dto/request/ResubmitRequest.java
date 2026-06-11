package group1.com.MangaSystemAndManagement.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResubmitRequest {
    private Long submissionId;
    private Long submittedById;
    private String title;
    private String contentUrl;
}
