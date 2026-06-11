package group1.com.MangaSystemAndManagement.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AssignSketchTaskRequest {
    private Long sketchPageId;
    private List<SketchTaskDetail> tasks;

    @Getter
    @Setter
    public static class SketchTaskDetail {
        private String taskType;
        private String description;
        private Long assignedToId;
    }
}
