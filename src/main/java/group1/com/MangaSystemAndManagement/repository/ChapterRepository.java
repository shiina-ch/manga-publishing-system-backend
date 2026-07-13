package group1.com.MangaSystemAndManagement.repository;

import group1.com.MangaSystemAndManagement.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    List<Chapter> findByProductionPlanId(Long productionPlanId);

    boolean existsByProductionPlanIdAndChapterStatusNot(Long productionPlanId, group1.com.MangaSystemAndManagement.model.ChapterStatus status);
}
