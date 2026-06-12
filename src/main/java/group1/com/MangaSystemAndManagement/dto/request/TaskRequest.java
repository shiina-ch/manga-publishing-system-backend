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
public class TaskRequest {
    @NotNull
    private Page page;
    private Account assignedTo;
    @Size(max = 255)
    private String title;
    private String description;
    @Size(max = 50)
    private String status;
    private Instant deadline;
}
