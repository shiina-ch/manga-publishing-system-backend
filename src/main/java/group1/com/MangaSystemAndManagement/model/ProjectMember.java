package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
public class ProjectMember {
    @EmbeddedId
    private ProjectMemberId id;

    @MapsId("projectId")
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ProjectId", nullable = false)
    private Project project;

    @MapsId("accountId")
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AccountId", nullable = false)
    private Account account;

    @ColumnDefault("getdate()")
    @Column(name = "JoinedAt")
    private Instant joinedAt;

}
