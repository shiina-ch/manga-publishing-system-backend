package group1.com.MangaSystemAndManagement.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateChapterRequest {

    @NotNull(message = "Production plan ID is required")
    private Long planId;

    @NotNull(message = "Chapter number is required")
    private Integer chapterNumber;

    private String title;

    private String status;

    private Integer targetPageCount;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private LocalDate publishDate;
}
