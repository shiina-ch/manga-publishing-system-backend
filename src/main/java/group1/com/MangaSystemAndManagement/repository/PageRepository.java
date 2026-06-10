package group1.com.MangaSystemAndManagement.repository;
import group1.com.MangaSystemAndManagement.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
}
