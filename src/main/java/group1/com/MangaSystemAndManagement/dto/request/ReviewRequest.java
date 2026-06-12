package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequest {
    private Long submissionId;
    private Long reviewerId;
    private String decision; // APPROVE or REQUEST_CHANGES
    private String comment;
    private Boolean pacingPass;
    private Boolean structurePass;
    private Boolean imageFlowPass;
}
