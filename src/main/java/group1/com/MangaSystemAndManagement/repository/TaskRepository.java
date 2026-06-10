package group1.com.MangaSystemAndManagement.repository;
import group1.com.MangaSystemAndManagement.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}
