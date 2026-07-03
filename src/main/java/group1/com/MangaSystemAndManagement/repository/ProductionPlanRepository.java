package group1.com.MangaSystemAndManagement.repository;

import group1.com.MangaSystemAndManagement.model.ProductionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, Long> {
    Optional<ProductionPlan> findByProjectId(Long projectId);
}
