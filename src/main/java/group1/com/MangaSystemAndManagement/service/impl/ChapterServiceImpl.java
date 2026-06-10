package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.model.Chapter;
import group1.com.MangaSystemAndManagement.repository.ChapterRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ChapterServiceImpl implements ChapterService {
    private final ChapterRepository repository;
    @Override
    @Transactional
    public Chapter create(Chapter entity) {
        return repository.save(entity);
    }
    @Override
    public Optional<Chapter> findById(Long id) {
        return repository.findById(id);
    }
    @Override
    public List<Chapter> findAll() {
        return repository.findAll();
    }
    @Override
    @Transactional
    public Chapter update(Long id, Chapter entity) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Chapter not found with id " + id);
        }
        entity.setId(id);
        return repository.save(entity);
    }
    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Chapter not found with id " + id);
        }
        repository.deleteById(id);
    }
}
