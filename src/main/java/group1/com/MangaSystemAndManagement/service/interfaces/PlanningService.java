package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.dto.request.PlanningRequest;
import group1.com.MangaSystemAndManagement.model.Planning;
import java.util.List;
import java.util.Optional;
public interface PlanningService {
    Planning create(PlanningRequest request);
    Optional<Planning> findById(Long id);
    List<Planning> findAll();
    Planning update(Long id, PlanningRequest request);
    void delete(Long id);
}
