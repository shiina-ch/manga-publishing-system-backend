package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.model.Chapter;
import java.util.List;
import java.util.Optional;
public interface ChapterService {
    Chapter create(Chapter entity);
    Optional<Chapter> findById(Long id);
    List<Chapter> findAll();
    Chapter update(Long id, Chapter entity);
    void delete(Long id);
}
