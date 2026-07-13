package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.ProjectFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProjectRequest {

    @NotBlank(message = "Project title is required")
    private String title;

    private String genre;

    private String targetAudience;

    @NotNull(message = "Format is required")
    private ProjectFormat format;

    @NotNull(message = "Tantou ID is required to assign a Tantou")
    private Long tantouId;
}
