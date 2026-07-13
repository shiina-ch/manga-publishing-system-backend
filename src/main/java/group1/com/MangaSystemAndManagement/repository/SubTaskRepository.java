package group1.com.MangaSystemAndManagement.repository;

import group1.com.MangaSystemAndManagement.model.SubTask;
import group1.com.MangaSystemAndManagement.model.SubTaskWorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, Long> {

    /** All sub-tasks belonging to a chapter-level task. */
    List<SubTask> findByTaskId(Long taskId);

    /** Sub-tasks assigned to an Assistant, ordered by deadline ascending for the dashboard. */
    List<SubTask> findByAssigneeIdOrderByDeadlineDateAscDeadlineTimeAsc(Long assigneeId);

    /** True when at least one sub-task still has a status other than the supplied one. */
    boolean existsByTaskIdAndSubtaskStatusNot(Long taskId, SubTaskWorkflowStatus status);

    /** Count of sub-tasks for a parent task grouped by status – used for progress roll-up. */
    long countByTaskIdAndSubtaskStatus(Long taskId, SubTaskWorkflowStatus status);

    /** Total number of sub-tasks for a parent task. */
    long countByTaskId(Long taskId);

    /** Latest sub-task for a given task + assignee pair (used to detect duplicates). */
    @Query("""
           SELECT s FROM SubTask s
           WHERE s.task.id = :taskId AND s.assignee.id = :assigneeId
           ORDER BY s.id DESC
           """)
    List<SubTask> findRecentForAssignee(Long taskId, Long assigneeId);
}
