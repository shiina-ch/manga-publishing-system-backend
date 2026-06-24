package group1.com.MangaSystemAndManagement.dto.response;

import group1.com.MangaSystemAndManagement.model.Account;
import java.time.LocalDateTime;

public record AccountResponse(
        long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String requestedRole,
        String status,
        Long approvedById,
        LocalDateTime approvedAt,
        String rejectionReason,
        Long rejectedById,
        LocalDateTime rejectedAt) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(), account.getFirstName(), account.getLastName(), account.getEmail(),
                account.getPhoneNumber(), account.getRequestedRole(), account.getStatus(),
                account.getApprovedById(), account.getApprovedAt(), account.getRejectionReason(),
                account.getRejectedById(), account.getRejectedAt());
    }
}
