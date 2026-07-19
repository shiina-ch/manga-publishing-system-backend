package group1.com.MangaSystemAndManagement.service.interfaces;

import group1.com.MangaSystemAndManagement.dto.request.ProductionPlanRequest;
import group1.com.MangaSystemAndManagement.model.ProductionPlan;

import java.util.List;

public interface ProductionPlanService {
    ProductionPlan createProductionPlan(Long projectId, ProductionPlanRequest request);
    ProductionPlan approveProductionPlan(Long id);
    ProductionPlan getProductionPlan(Long id);
    List<ProductionPlan> getAllProductionPlans();
}
