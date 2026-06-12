package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.dto.request.PageRequest;
import group1.com.MangaSystemAndManagement.model.Page;
import group1.com.MangaSystemAndManagement.repository.PageRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {
    private final PageRepository repository;
    @Override
    @Transactional
    public Page create(PageRequest request) {
        Page entity = new Page();
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
        return repository.save(entity);
    }
    @Override
    public Optional<Page> findById(Long id) {
        return repository.findById(id);
    }
    @Override
    public List<Page> findAll() {
        return repository.findAll();
    }
    @Override
    @Transactional
    public Page update(Long id, PageRequest request) {
        Page entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Page not found with id " + id));
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
        return repository.save(entity);
    }
    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Page not found with id " + id);
        }
        repository.deleteById(id);
    }
}
