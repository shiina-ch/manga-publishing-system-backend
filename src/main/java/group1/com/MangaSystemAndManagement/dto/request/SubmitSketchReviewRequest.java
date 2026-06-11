package group1.com.MangaSystemAndManagement.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitSketchReviewRequest {
    private Long sketchPageId;
    private Long reviewerId;
    private String decision;
    private String comment;
    private String layoutFeedback;
    private String detailsFeedback;
}
