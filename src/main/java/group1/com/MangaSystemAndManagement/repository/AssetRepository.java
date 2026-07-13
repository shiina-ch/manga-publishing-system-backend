package group1.com.MangaSystemAndManagement.repository;

import group1.com.MangaSystemAndManagement.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findByProjectId(Long projectId);
}
