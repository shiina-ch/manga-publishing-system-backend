package group1.com.MangaSystemAndManagement.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignChapterRequest {
    @NotNull(message = "Mangaka ID is required")
    private Long mangakaId;

    @NotNull(message = "Requester ID is required")
    private Long requesterId;
}
