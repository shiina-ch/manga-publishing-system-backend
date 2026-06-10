package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.model.Task;
import java.util.List;
import java.util.Optional;
public interface TaskService {
    Task create(Task entity);
    Optional<Task> findById(Long id);
    List<Task> findAll();
    Task update(Long id, Task entity);
    void delete(Long id);
}
