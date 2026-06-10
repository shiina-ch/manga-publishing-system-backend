package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "AccountSystemRole")
public class AccountSystemRole {
    @EmbeddedId
    private AccountSystemRoleId id;

    @MapsId("accountId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AccountId", nullable = false)
    private Account account;


}