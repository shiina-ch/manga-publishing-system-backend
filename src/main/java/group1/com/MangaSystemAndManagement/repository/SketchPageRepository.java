package group1.com.MangaSystemAndManagement.repository;

import group1.com.MangaSystemAndManagement.model.SketchPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SketchPageRepository extends JpaRepository<SketchPage, Long> {
}
