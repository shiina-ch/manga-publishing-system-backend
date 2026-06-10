package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@Embeddable
public class ProjectMemberId implements java.io.Serializable {
    private static final long serialVersionUID = 6409430456308311828L;
    @NotNull
    @Column(name = "ProjectId", nullable = false)
    private Long projectId;

    @NotNull
    @Column(name = "AccountId", nullable = false)
    private Long accountId;

    @NotNull
    @Column(name = "ProjectRoleId", nullable = false)
    private Long projectRoleId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProjectMemberId entity = (ProjectMemberId) o;
        return Objects.equals(this.accountId, entity.accountId) &&
                Objects.equals(this.projectId, entity.projectId) &&
                Objects.equals(this.projectRoleId, entity.projectRoleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, projectId, projectRoleId);
    }

}