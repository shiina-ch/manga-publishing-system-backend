package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "Registration")
public class Registration {
    @Id
    @Size(max = 20)
    @Column(name = "username", nullable = false, length = 20)
    private String username;

    @Size(max = 30)
    @NotNull
    @Column(name = "password", nullable = false, length = 30)
    private String password;

    @Size(max = 100)
    @Nationalized
    @Column(name = "lastname", length = 100)
    private String lastname;

    @ColumnDefault("0")
    @Column(name = "isAdmin")
    private Boolean isAdmin;

}