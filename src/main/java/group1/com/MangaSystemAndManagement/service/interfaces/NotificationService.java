package group1.com.MangaSystemAndManagement.service.interfaces;

import group1.com.MangaSystemAndManagement.model.Account;

public interface NotificationService {
    void createNotification(Account account, String message);
}
