package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
public class ProjectRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "RoleName", nullable = false, length = 100)
    private String roleName;

    @Size(max = 500)
    @Nationalized
    @Column(name = "Description", length = 500)
    private String description;

}