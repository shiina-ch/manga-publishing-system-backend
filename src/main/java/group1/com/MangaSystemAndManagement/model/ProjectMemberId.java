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
public class ProjectMemberId implements Serializable {
    private static final long serialVersionUID = 2059023270750384037L;
    @NotNull
    @Column(name = "ProjectId", nullable = false)
    private Long projectId;

    @NotNull
    @Column(name = "AccountId", nullable = false)
    private Long accountId;

    @NotNull
    @Column(name = "ProjectRoleId", nullable = false)
    private Long projectRoleId;


}