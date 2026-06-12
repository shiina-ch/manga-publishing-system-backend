package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NameSubmissionRequest {
    private Long projectId;
    private Long planningId;
    private Long submittedById;
    private String title;
    private String contentUrl;
}
