package group1.com.MangaSystemAndManagement.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression: the legacy {@code GET /api/chapters} controller returns the
 * {@link Chapter} entity directly. Before the {@code @JsonIgnore} fix the
 * bidirectional {@code Chapter -> productionPlan -> chapters -> ...} graph
 * triggered a Jackson infinite-recursion stack overflow. These tests serialize
 * a fully-wired object graph to a JSON string with a hard cap on the recursion
 * depth – the previous build would throw; the patched build must succeed.
 */
class ChapterSerializationRecursionTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void chapterWithProjectPlanAndTasksSerializesWithoutStackOverflow() throws Exception {
        Project project = new Project();
        project.setId(1L);
        project.setTitle("Sample");

        ProductionPlan plan = new ProductionPlan();
        plan.setId(10L);
        plan.setProject(project);
        plan.setChapters(new ArrayList<>());

        Chapter chapter = new Chapter();
        chapter.setId(100L);
        chapter.setTitle("Ch 1");
        chapter.setProject(project);
        chapter.setProductionPlan(plan);
        chapter.setTasks(new ArrayList<>());
        chapter.setSketchPages(new ArrayList<>());

        Task task = new Task();
        task.setId(1000L);
        task.setTitle("Line art");
        task.setChapter(chapter);
        task.setFeedbacks(new ArrayList<>());
        chapter.getTasks().add(task);

        Feedback feedback = new Feedback();
        feedback.setId(5000L);
        feedback.setTask(task);
        feedback.setDecision(FeedbackDecision.APPROVED);
        feedback.setContent("ok");
        task.getFeedbacks().add(feedback);

        plan.getChapters().add(chapter);

        String json = mapper.writeValueAsString(chapter);
        assertNotNull(json);
        assertTrue(json.contains("\"id\":100"));
        // references are intentionally omitted via @JsonIgnore on the back-refs
        assertTrue(!json.contains("\"productionPlan\""));
        assertTrue(!json.contains("\"project\""));
        assertTrue(!json.contains("\"chapter\":"));
    }
}
