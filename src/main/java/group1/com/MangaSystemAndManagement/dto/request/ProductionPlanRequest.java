package group1.com.MangaSystemAndManagement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionPlanRequest {
    private String milestones;
    private String schedule;
    private String chapterTimeline;
    private Instant deadline;
    private String resources;
    private Double budget;
    private String assistantAllocation;
    private String priority;
    private String risk;
}
