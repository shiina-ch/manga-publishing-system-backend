package group1.com.MangaSystemAndManagement.dto.response;

import group1.com.MangaSystemAndManagement.model.DevelopmentPlan;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DevelopmentPlanResponse {
    private Long id;
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
    private String approvalStatus;

    public static DevelopmentPlanResponse from(DevelopmentPlan dp) {
        if (dp == null) return null;
        DevelopmentPlanResponse r = new DevelopmentPlanResponse();
        r.id = dp.getId();
        r.storyDirection = dp.getStoryDirection();
        r.worldSetting = dp.getWorldSetting();
        r.mainCharacters = dp.getMainCharacters();
        r.arcPlanning = dp.getArcPlanning();
        r.estimatedVolumes = dp.getEstimatedVolumes();
        r.estimatedChapters = dp.getEstimatedChapters();
        r.targetAudience = dp.getTargetAudience();
        r.releaseStrategy = dp.getReleaseStrategy();
        r.businessGoal = dp.getBusinessGoal();
        r.notes = dp.getNotes();
        r.approvalStatus = dp.getApprovalStatus();
        return r;
    }
}
