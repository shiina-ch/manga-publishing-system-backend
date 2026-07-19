package group1.com.MangaSystemAndManagement.dto.response;

import group1.com.MangaSystemAndManagement.model.Account;
import group1.com.MangaSystemAndManagement.model.Project;
import group1.com.MangaSystemAndManagement.model.ProjectFormat;
import group1.com.MangaSystemAndManagement.model.ProjectWorkflowStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ProjectResponse {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String currentPhase;
    private String genre;
    private String targetAudience;
    private ProjectFormat format;
    private ProjectWorkflowStatus projectWorkflowStatus;
    private Instant createdAt;
    private Instant startDate;
    private Instant expectedEndDate;

    private Long ownerId;
    private String ownerName;
    private Long tantouId;
    private String tantouName;
    private Long mangakaId;
    private String mangakaName;

    private DevelopmentPlanResponse developmentPlan;
    private ProductionPlanResponse productionPlan;

    private static String fullName(Account a) {
        if (a == null) return null;
        String fn = a.getFirstName() == null ? "" : a.getFirstName();
        String ln = a.getLastName() == null ? "" : a.getLastName();
        return (fn + " " + ln).trim();
    }

    public static ProjectResponse from(Project p) {
        ProjectResponse r = new ProjectResponse();
        r.id = p.getId();
        r.title = p.getTitle();
        r.description = p.getDescription();
        r.status = p.getStatus();
        r.currentPhase = p.getCurrentPhase();
        r.genre = p.getGenre();
        r.targetAudience = p.getTargetAudience();
        r.format = p.getFormat();
        r.projectWorkflowStatus = p.getProjectWorkflowStatus();
        r.createdAt = p.getCreatedAt();
        r.startDate = p.getStartDate();
        r.expectedEndDate = p.getExpectedEndDate();

        if (p.getOwner() != null) {
            r.ownerId = p.getOwner().getId();
            r.ownerName = fullName(p.getOwner());
        }
        if (p.getTantou() != null) {
            r.tantouId = p.getTantou().getId();
            r.tantouName = fullName(p.getTantou());
        }
        if (p.getMangaka() != null) {
            r.mangakaId = p.getMangaka().getId();
            r.mangakaName = fullName(p.getMangaka());
        }

        r.developmentPlan = DevelopmentPlanResponse.from(p.getDevelopmentPlan());
        r.productionPlan = ProductionPlanResponse.from(p.getProductionPlan());
        return r;
    }
}
