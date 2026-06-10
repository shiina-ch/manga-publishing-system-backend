package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;

@Getter
@Setter
@Entity
public class Planning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Nationalized
    @Column(name = "Title")
    private String title;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

    @Size(max = 50)
    @Nationalized
    @Column(name = "Status", length = 50)
    private String status;

}