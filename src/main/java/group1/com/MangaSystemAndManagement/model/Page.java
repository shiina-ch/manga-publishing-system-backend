package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @NotNull
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ChapterId", nullable = false)
    private Chapter chapter;

    @Column(name = "PageNumber")
    private Integer pageNumber;

    @Size(max = 50)
    @Nationalized
    @Column(name = "Status", length = 50)
    private String status;

}
