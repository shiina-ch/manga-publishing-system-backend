package group1.com.MangaSystemAndManagement.repository;
import group1.com.MangaSystemAndManagement.model.Planning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface PlanningRepository extends JpaRepository<Planning, Long> {
}
