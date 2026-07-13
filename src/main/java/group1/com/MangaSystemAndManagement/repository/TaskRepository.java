package group1.com.MangaSystemAndManagement.repository;

import group1.com.MangaSystemAndManagement.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByChapterId(Long chapterId);

    List<Task> findByAssigneeId(Long assigneeId);

    boolean existsByChapterIdAndTaskWorkflowStatusNot(Long chapterId, group1.com.MangaSystemAndManagement.model.TaskWorkflowStatus status);
}
