package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.dto.request.PageRequest;
import group1.com.MangaSystemAndManagement.model.Page;
import java.util.List;
import java.util.Optional;
public interface PageService {
    Page create(PageRequest request);
    Optional<Page> findById(Long id);
    List<Page> findAll();
    Page update(Long id, PageRequest request);
    void delete(Long id);
}
