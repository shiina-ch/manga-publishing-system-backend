package group1.com.MangaSystemAndManagement.dto.response;

import group1.com.MangaSystemAndManagement.model.PlanStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PlanDashboardResponse {
    private Long id;
    private Long projectId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalVolumeTarget;
    private PlanStatus planStatus;
    private Double completionPercentage;
    private List<ChapterWithTasksResponse> chapters;
}
