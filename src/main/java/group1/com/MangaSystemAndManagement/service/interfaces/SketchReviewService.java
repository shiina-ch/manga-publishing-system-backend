package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.dto.request.SketchReviewRequest;

import group1.com.MangaSystemAndManagement.model.SketchReview;
import java.util.List;
import java.util.Optional;

public interface SketchReviewService {
    SketchReview create(SketchReviewRequest request);
    Optional<SketchReview> findById(Long id);
    List<SketchReview> findAll();
    SketchReview update(Long id, SketchReviewRequest request);
    void delete(Long id);
}
