package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.model.Vote;
import java.util.List;
import java.util.Optional;
public interface VoteService {
    Vote create(Vote entity);
    Optional<Vote> findById(Long id);
    List<Vote> findAll();
    Vote update(Long id, Vote entity);
    void delete(Long id);
}
