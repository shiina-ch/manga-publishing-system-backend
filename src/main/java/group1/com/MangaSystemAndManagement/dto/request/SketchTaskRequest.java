package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SketchTaskRequest {
    @NotNull
    private SketchPage sketchPage;
    @Size(max = 100)
    private String taskType;
    private String description;
    @NotNull
    private Account assignedTo;
    @Size(max = 1000)
    private String completedUrl;
    @Size(max = 50)
    private String status;
    private Instant completedAt;
}
