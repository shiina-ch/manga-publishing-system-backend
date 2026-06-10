package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "ProjectMember")
public class ProjectMember {
    @EmbeddedId
    private ProjectMemberId id;

    @MapsId("projectId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ProjectId", nullable = false)
    private Project project;

    @MapsId("accountId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AccountId", nullable = false)
    private Account account;

    @ColumnDefault("getdate()")
    @Column(name = "JoinedAt")
    private Instant joinedAt;


}