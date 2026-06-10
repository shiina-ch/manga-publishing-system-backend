package group1.com.MangaSystemAndManagement.repository;
import group1.com.MangaSystemAndManagement.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
}
