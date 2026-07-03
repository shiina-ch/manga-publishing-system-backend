package group1.com.MangaSystemAndManagement.service.impl;

import group1.com.MangaSystemAndManagement.dto.request.DevelopmentPlanRequest;
import group1.com.MangaSystemAndManagement.model.DevelopmentPlan;
import group1.com.MangaSystemAndManagement.model.Project;
import group1.com.MangaSystemAndManagement.repository.DevelopmentPlanRepository;
import group1.com.MangaSystemAndManagement.repository.ProjectRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.DevelopmentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DevelopmentPlanServiceImpl implements DevelopmentPlanService {

    private final DevelopmentPlanRepository developmentPlanRepository;
    private final ProjectRepository projectRepository;

    @Override
    public DevelopmentPlan createDevelopmentPlan(Long projectId, DevelopmentPlanRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        DevelopmentPlan plan = new DevelopmentPlan();
        plan.setProject(project);
        plan.setStoryDirection(request.getStoryDirection());
        plan.setWorldSetting(request.getWorldSetting());
        plan.setMainCharacters(request.getMainCharacters());
        plan.setArcPlanning(request.getArcPlanning());
        plan.setEstimatedVolumes(request.getEstimatedVolumes());
        plan.setEstimatedChapters(request.getEstimatedChapters());
        plan.setTargetAudience(request.getTargetAudience());
        plan.setReleaseStrategy(request.getReleaseStrategy());
        plan.setBusinessGoal(request.getBusinessGoal());
        plan.setNotes(request.getNotes());
        plan.setApprovalStatus("PENDING");

        return developmentPlanRepository.save(plan);
    }

    @Override
    public DevelopmentPlan approveDevelopmentPlan(Long id) {
        DevelopmentPlan plan = developmentPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Development Plan not found"));
        plan.setApprovalStatus("APPROVED");
        return developmentPlanRepository.save(plan);
    }

    @Override
    public DevelopmentPlan getDevelopmentPlan(Long id) {
        return developmentPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Development Plan not found"));
    }
}
