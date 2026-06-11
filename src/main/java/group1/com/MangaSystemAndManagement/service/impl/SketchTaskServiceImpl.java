package group1.com.MangaSystemAndManagement.service.impl;

import group1.com.MangaSystemAndManagement.model.SketchTask;
import group1.com.MangaSystemAndManagement.repository.SketchTaskRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.SketchTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SketchTaskServiceImpl implements SketchTaskService {
    private final SketchTaskRepository repository;

    @Override
    @Transactional
    public SketchTask create(SketchTask entity) {
        return repository.save(entity);
    }

    @Override
    public Optional<SketchTask> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<SketchTask> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public SketchTask update(Long id, SketchTask entity) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("SketchTask not found with id " + id);
        }
        entity.setId(id);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("SketchTask not found with id " + id);
        }
        repository.deleteById(id);
    }
}
