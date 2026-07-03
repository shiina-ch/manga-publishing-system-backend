package group1.com.MangaSystemAndManagement.service.interfaces;

import group1.com.MangaSystemAndManagement.dto.request.DevelopmentPlanRequest;
import group1.com.MangaSystemAndManagement.model.DevelopmentPlan;

public interface DevelopmentPlanService {
    DevelopmentPlan createDevelopmentPlan(Long projectId, DevelopmentPlanRequest request);
    DevelopmentPlan approveDevelopmentPlan(Long id);
    DevelopmentPlan getDevelopmentPlan(Long id);
}
