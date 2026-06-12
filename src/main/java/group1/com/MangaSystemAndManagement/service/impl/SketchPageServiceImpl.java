package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.dto.request.SketchPageRequest;

import group1.com.MangaSystemAndManagement.model.SketchPage;
import group1.com.MangaSystemAndManagement.repository.SketchPageRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.SketchPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SketchPageServiceImpl implements SketchPageService {
    private final SketchPageRepository repository;

    @Override
    @Transactional
    public SketchPage create(SketchPageRequest request) {
        SketchPage entity = new SketchPage();
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
        return repository.save(entity);
    }

    @Override
    public Optional<SketchPage> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<SketchPage> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public SketchPage update(Long id, SketchPageRequest request) {
        SketchPage entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("SketchPage not found with id " + id));
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("SketchPage not found with id " + id);
        }
        repository.deleteById(id);
    }
}
