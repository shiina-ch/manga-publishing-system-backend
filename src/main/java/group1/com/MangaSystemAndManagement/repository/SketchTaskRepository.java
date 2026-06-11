package group1.com.MangaSystemAndManagement.repository;

import group1.com.MangaSystemAndManagement.model.SketchTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SketchTaskRepository extends JpaRepository<SketchTask, Long> {
}
