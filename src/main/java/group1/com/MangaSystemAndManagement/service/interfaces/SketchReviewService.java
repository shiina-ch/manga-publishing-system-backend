package group1.com.MangaSystemAndManagement.service.interfaces;

import group1.com.MangaSystemAndManagement.model.SketchReview;
import java.util.List;
import java.util.Optional;

public interface SketchReviewService {
    SketchReview create(SketchReview entity);
    Optional<SketchReview> findById(Long id);
    List<SketchReview> findAll();
    SketchReview update(Long id, SketchReview entity);
    void delete(Long id);
}
