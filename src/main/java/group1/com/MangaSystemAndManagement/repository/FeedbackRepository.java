package group1.com.MangaSystemAndManagement.repository;

import group1.com.MangaSystemAndManagement.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByTaskId(Long taskId);

    boolean existsByTaskId(Long taskId);
}
