package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteSketchTaskRequest {
    private Long sketchTaskId;
    private String completedUrl;
    private Long completedById;
}
