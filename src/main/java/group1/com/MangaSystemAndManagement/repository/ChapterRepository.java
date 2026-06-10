package group1.com.MangaSystemAndManagement.repository;
import group1.com.MangaSystemAndManagement.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
}
