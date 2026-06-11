package group1.com.MangaSystemAndManagement.service.interfaces;

import group1.com.MangaSystemAndManagement.model.SketchTask;
import java.util.List;
import java.util.Optional;

public interface SketchTaskService {
    SketchTask create(SketchTask entity);
    Optional<SketchTask> findById(Long id);
    List<SketchTask> findAll();
    SketchTask update(Long id, SketchTask entity);
    void delete(Long id);
}
