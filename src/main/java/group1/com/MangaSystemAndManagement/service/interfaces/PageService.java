package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.model.Page;
import java.util.List;
import java.util.Optional;
public interface PageService {
    Page create(Page entity);
    Optional<Page> findById(Long id);
    List<Page> findAll();
    Page update(Long id, Page entity);
    void delete(Long id);
}
