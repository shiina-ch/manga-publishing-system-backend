package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.model.Planning;
import java.util.List;
import java.util.Optional;
public interface PlanningService {
    Planning create(Planning entity);
    Optional<Planning> findById(Long id);
    List<Planning> findAll();
    Planning update(Long id, Planning entity);
    void delete(Long id);
}
