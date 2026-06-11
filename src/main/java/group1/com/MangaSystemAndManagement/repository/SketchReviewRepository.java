package group1.com.MangaSystemAndManagement.repository;

import group1.com.MangaSystemAndManagement.model.SketchReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SketchReviewRepository extends JpaRepository<SketchReview, Long> {
}
