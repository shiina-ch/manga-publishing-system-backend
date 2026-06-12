package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.*;
import java.time.LocalDate;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanningRequest {
    @Size(max = 255)
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    @Size(max = 50)
    private String status;
}
