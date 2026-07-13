package group1.com.MangaSystemAndManagement.dto.response;

import group1.com.MangaSystemAndManagement.model.Account;

public record AccountSearchResponse(
        long id,
        String email,
        String firstName,
        String lastName
) {
    public static AccountSearchResponse from(Account account) {
        return new AccountSearchResponse(
                account.getId(),
                account.getEmail(),
                account.getFirstName(),
                account.getLastName()
        );
    }
}
