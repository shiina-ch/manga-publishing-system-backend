package group1.com.MangaSystemAndManagement.repository;

import group1.com.MangaSystemAndManagement.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
