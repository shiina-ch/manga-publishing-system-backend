package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.dto.request.SketchPageRequest;

import group1.com.MangaSystemAndManagement.model.SketchPage;
import java.util.List;
import java.util.Optional;

public interface SketchPageService {
    SketchPage create(SketchPageRequest request);
    Optional<SketchPage> findById(Long id);
    List<SketchPage> findAll();
    SketchPage update(Long id, SketchPageRequest request);
    void delete(Long id);
}
