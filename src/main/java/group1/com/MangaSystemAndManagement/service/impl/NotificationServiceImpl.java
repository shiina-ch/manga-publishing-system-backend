package group1.com.MangaSystemAndManagement.service.impl;

import group1.com.MangaSystemAndManagement.model.Account;
import group1.com.MangaSystemAndManagement.model.Notification;
import group1.com.MangaSystemAndManagement.repository.NotificationRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void createNotification(Account account, String message) {
        Notification notification = new Notification(account, message);
        notificationRepository.save(notification);
    }
}
