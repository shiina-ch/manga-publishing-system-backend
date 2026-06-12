package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.*;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterRequest {
    private Integer chapterNumber;
    @Size(max = 255)
    private String title;
    @Size(max = 50)
    private String status;
    private List<SketchPage> sketchPages;
}
