package group1.com.MangaSystemAndManagement.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import group1.com.MangaSystemAndManagement.model.SubTask;
import group1.com.MangaSystemAndManagement.model.SubTaskWorkflowStatus;
import group1.com.MangaSystemAndManagement.model.TaskType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression: confirms the DTO wire-format preserves the exact hour/minute/second
 * parsed from user input (e.g. "8am" → 08:00), without truncation or timezone drift.
 */
class SubTaskResponseSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void deadlineTime8amIsSerializedAs08Colon00() throws Exception {
        SubTask sub = new SubTask();
        sub.setId(1L);
        sub.setTitle("Background");
        sub.setProductionTaskType(TaskType.BACKGROUND);
        sub.setSubtaskStatus(SubTaskWorkflowStatus.TODO);
        sub.setDeadlineDate(LocalDate.of(2026, 7, 16));
        // Simulating what SubTaskServiceImpl.parseDeadlineTime("8am") produced
        sub.setDeadlineTime(LocalTime.of(8, 0));

        SubTaskResponse r = SubTaskResponse.from(sub);
        String json = mapper.writeValueAsString(r);

        // Jackson serializes LocalTime as ISO-8601 HH:mm:ss (always 3 segments
        // when using jsr310 default) - confirms 08:00:00, not 8:00 and not 08:00
        assertTrue(json.contains("\"deadlineTime\":\"08:00:00\""),
                "Expected \"deadlineTime\":\"08:00:00\" in wire JSON, got: " + json);
    }

    @Test
    void deadlineTimeTwentyPastEightPmIsSerializedAs20Colon20() throws Exception {
        SubTask sub = new SubTask();
        sub.setId(2L);
        sub.setSubtaskStatus(SubTaskWorkflowStatus.TODO);
        sub.setDeadlineDate(LocalDate.of(2026, 7, 16));
        sub.setDeadlineTime(LocalTime.of(20, 20));

        SubTaskResponse r = SubTaskResponse.from(sub);
        String json = mapper.writeValueAsString(r);

        assertTrue(json.contains("\"deadlineTime\":\"20:20:00\""), json);
    }

    @Test
    void deadlineTimeWithSecondsIsSerializedExactly() throws Exception {
        SubTask sub = new SubTask();
        sub.setId(3L);
        sub.setSubtaskStatus(SubTaskWorkflowStatus.TODO);
        sub.setDeadlineDate(LocalDate.of(2026, 7, 16));
        sub.setDeadlineTime(LocalTime.of(8, 30, 15));

        SubTaskResponse r = SubTaskResponse.from(sub);
        String json = mapper.writeValueAsString(r);

        assertTrue(json.contains("\"deadlineTime\":\"08:30:15\""), json);
    }

    @Test
    void deadlineDateIsSerializedAsIsoLocalDate() throws Exception {
        SubTask sub = new SubTask();
        sub.setId(5L);
        sub.setSubtaskStatus(SubTaskWorkflowStatus.TODO);
        sub.setDeadlineDate(LocalDate.of(2026, 7, 16));
        sub.setDeadlineTime(LocalTime.of(8, 0));

        SubTaskResponse r = SubTaskResponse.from(sub);
        String json = mapper.writeValueAsString(r);

        assertTrue(json.contains("\"deadlineDate\":\"2026-07-16\""), json);
    }

    @Test
    void deadlineInstantIsStableAcrossTimezones() throws Exception {
        // Regression: deadlineInstant previously used ZoneId.systemDefault(),
        // causing 8am UTC+7 to drift to "2026-07-16T01:00:00Z" on a UTC server.
        // After the fix it must always be anchored to UTC.
        SubTask sub = new SubTask();
        sub.setId(6L);
        sub.setSubtaskStatus(SubTaskWorkflowStatus.TODO);
        sub.setDeadlineDate(LocalDate.of(2026, 7, 16));
        sub.setDeadlineTime(LocalTime.of(8, 0));

        SubTaskResponse r = SubTaskResponse.from(sub);
        String json = mapper.writeValueAsString(r);

        assertTrue(json.contains("\"deadlineInstant\":\"2026-07-16T08:00:00Z\""),
                "Expected UTC-anchored 08:00:00Z, got: " + json);
    }
}
