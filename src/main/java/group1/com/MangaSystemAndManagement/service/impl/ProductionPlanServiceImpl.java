package group1.com.MangaSystemAndManagement.service.impl;

import group1.com.MangaSystemAndManagement.dto.request.ProductionPlanRequest;
import group1.com.MangaSystemAndManagement.model.ProductionPlan;
import group1.com.MangaSystemAndManagement.model.Project;
import group1.com.MangaSystemAndManagement.repository.ProductionPlanRepository;
import group1.com.MangaSystemAndManagement.repository.ProjectRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.ProductionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionPlanServiceImpl implements ProductionPlanService {

    private final ProductionPlanRepository productionPlanRepository;
    private final ProjectRepository projectRepository;

    @Override
    public ProductionPlan createProductionPlan(Long projectId, ProductionPlanRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        ProductionPlan plan = new ProductionPlan();
        plan.setProject(project);
        plan.setMilestones(request.getMilestones());
        plan.setSchedule(request.getSchedule());
        plan.setChapterTimeline(request.getChapterTimeline());
        plan.setDeadline(request.getDeadline());
        plan.setResources(request.getResources());
        plan.setBudget(request.getBudget());
        plan.setAssistantAllocation(request.getAssistantAllocation());
        plan.setPriority(request.getPriority());
        plan.setRisk(request.getRisk());
        plan.setApprovalStatus("PENDING");

        return productionPlanRepository.save(plan);
    }

    @Override
    public ProductionPlan approveProductionPlan(Long id) {
        ProductionPlan plan = productionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Production Plan not found"));
        plan.setApprovalStatus("APPROVED");
        return productionPlanRepository.save(plan);
    }

    @Override
    public ProductionPlan getProductionPlan(Long id) {
        return productionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Production Plan not found"));
    }

    @Override
    public List<ProductionPlan> getAllProductionPlans() {
        return productionPlanRepository.findAll();
    }
}
