package group1.com.MangaSystemAndManagement.dto.response;

import group1.com.MangaSystemAndManagement.model.ProjectFormat;
import group1.com.MangaSystemAndManagement.model.ProjectWorkflowStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ProjectResponse {
    private Long id;
    private String title;
    private String description;
    private String genre;
    private String targetAudience;
    private ProjectFormat format;
    private ProjectWorkflowStatus projectWorkflowStatus;
    private Instant createdAt;
}
