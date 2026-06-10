package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @Column(name = "ChapterNumber")
    private Integer chapterNumber;

    @Size(max = 255)
    @Nationalized
    @Column(name = "Title")
    private String title;

    @Size(max = 50)
    @Nationalized
    @Column(name = "Status", length = 50)
    private String status;

}