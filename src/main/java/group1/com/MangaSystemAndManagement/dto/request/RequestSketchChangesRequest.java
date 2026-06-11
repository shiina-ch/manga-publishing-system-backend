package group1.com.MangaSystemAndManagement.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestSketchChangesRequest {
    private Long sketchPageId;
    private Long reviewerId;
    private String comment;
    private String specificFeedback;
}
