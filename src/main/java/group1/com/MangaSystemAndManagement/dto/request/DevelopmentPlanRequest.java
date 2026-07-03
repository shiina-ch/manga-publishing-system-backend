package group1.com.MangaSystemAndManagement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DevelopmentPlanRequest {
    private String storyDirection;
    private String worldSetting;
    private String mainCharacters;
    private String arcPlanning;
    private Integer estimatedVolumes;
    private Integer estimatedChapters;
    private String targetAudience;
    private String releaseStrategy;
    private String businessGoal;
    private String notes;
}
