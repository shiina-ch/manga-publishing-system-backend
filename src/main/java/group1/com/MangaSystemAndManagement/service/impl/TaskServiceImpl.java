package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.model.Task;
import group1.com.MangaSystemAndManagement.repository.TaskRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository repository;
    @Override
    @Transactional
    public Task create(Task entity) {
        return repository.save(entity);
    }
    @Override
    public Optional<Task> findById(Long id) {
        return repository.findById(id);
    }
    @Override
    public List<Task> findAll() {
        return repository.findAll();
    }
    @Override
    @Transactional
    public Task update(Long id, Task entity) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Task not found with id " + id);
        }
        entity.setId(id);
        return repository.save(entity);
    }
    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Task not found with id " + id);
        }
        repository.deleteById(id);
    }
}
