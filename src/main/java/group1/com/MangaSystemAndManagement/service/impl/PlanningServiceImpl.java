package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.model.Planning;
import group1.com.MangaSystemAndManagement.repository.PlanningRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.PlanningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class PlanningServiceImpl implements PlanningService {
    private final PlanningRepository repository;
    @Override
    @Transactional
    public Planning create(Planning entity) {
        return repository.save(entity);
    }
    @Override
    public Optional<Planning> findById(Long id) {
        return repository.findById(id);
    }
    @Override
    public List<Planning> findAll() {
        return repository.findAll();
    }
    @Override
    @Transactional
    public Planning update(Long id, Planning entity) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Planning not found with id " + id);
        }
        entity.setId(id);
        return repository.save(entity);
    }
    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Planning not found with id " + id);
        }
        repository.deleteById(id);
    }
}
