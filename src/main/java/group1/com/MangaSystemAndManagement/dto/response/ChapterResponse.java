package group1.com.MangaSystemAndManagement.dto.response;

import group1.com.MangaSystemAndManagement.model.ChapterStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ChapterResponse {
    private Long id;
    private Integer chapterNumber;
    private String title;
    private Integer targetPageCount;
    private LocalDate publishDate;
    private ChapterStatus chapterStatus;
    
    private Long projectId;
    private Long ownerId;
    private String ownerName;
}
