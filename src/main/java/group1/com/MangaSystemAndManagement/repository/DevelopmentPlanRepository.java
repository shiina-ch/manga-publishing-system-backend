package group1.com.MangaSystemAndManagement.repository;

import group1.com.MangaSystemAndManagement.model.DevelopmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DevelopmentPlanRepository extends JpaRepository<DevelopmentPlan, Long> {
    Optional<DevelopmentPlan> findByProjectId(Long projectId);
}
