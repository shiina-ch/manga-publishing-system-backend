package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.dto.request.SketchReviewRequest;

import group1.com.MangaSystemAndManagement.model.SketchReview;
import group1.com.MangaSystemAndManagement.repository.SketchReviewRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.SketchReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SketchReviewServiceImpl implements SketchReviewService {
    private final SketchReviewRepository repository;

    @Override
    @Transactional
    public SketchReview create(SketchReviewRequest request) {
        SketchReview entity = new SketchReview();
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
        return repository.save(entity);
    }

    @Override
    public Optional<SketchReview> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<SketchReview> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public SketchReview update(Long id, SketchReviewRequest request) {
        SketchReview entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("SketchReview not found with id " + id));
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("SketchReview not found with id " + id);
        }
        repository.deleteById(id);
    }
}
