package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.*;

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
