package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class AccountSystemRoleId implements Serializable {
    private static final long serialVersionUID = 5686257289928054670L;
    @NotNull
    @Column(name = "AccountId", nullable = false)
    private Long accountId;

    @NotNull
    @Column(name = "SystemRoleId", nullable = false)
    private Long systemRoleId;


}