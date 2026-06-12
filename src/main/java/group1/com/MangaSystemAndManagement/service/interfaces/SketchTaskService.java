package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.dto.request.SketchTaskRequest;

import group1.com.MangaSystemAndManagement.model.SketchTask;
import java.util.List;
import java.util.Optional;

public interface SketchTaskService {
    SketchTask create(SketchTaskRequest request);
    Optional<SketchTask> findById(Long id);
    List<SketchTask> findAll();
    SketchTask update(Long id, SketchTaskRequest request);
    void delete(Long id);
}
