package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.dto.request.VoteRequest;
import group1.com.MangaSystemAndManagement.model.Vote;
import group1.com.MangaSystemAndManagement.repository.VoteRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {
    private final VoteRepository repository;
    @Override
    @Transactional
    public Vote create(VoteRequest request) {
        Vote entity = new Vote();
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
        return repository.save(entity);
    }
    @Override
    public Optional<Vote> findById(Long id) {
        return repository.findById(id);
    }
    @Override
    public List<Vote> findAll() {
        return repository.findAll();
    }
    @Override
    @Transactional
    public Vote update(Long id, VoteRequest request) {
        Vote entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Vote not found with id " + id));
        org.springframework.beans.BeanUtils.copyProperties(request, entity);
        return repository.save(entity);
    }
    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Vote not found with id " + id);
        }
        repository.deleteById(id);
    }
}
