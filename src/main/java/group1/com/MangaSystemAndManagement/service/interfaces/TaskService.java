package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.dto.request.TaskRequest;
import group1.com.MangaSystemAndManagement.model.Task;
import java.util.List;
import java.util.Optional;
public interface TaskService {
    Task create(TaskRequest request);
    Optional<Task> findById(Long id);
    List<Task> findAll();
    Task update(Long id, TaskRequest request);
    void delete(Long id);
}
