package group1.com.MangaSystemAndManagement.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSketchPageRequest {
    private Long chapterId;
    private Integer pageNumber;
    private String initialSketchUrl;
    private Long createdById;
}
