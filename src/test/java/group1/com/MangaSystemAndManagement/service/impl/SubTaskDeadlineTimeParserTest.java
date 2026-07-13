package group1.com.MangaSystemAndManagement.service.impl;

import group1.com.MangaSystemAndManagement.exception.WorkflowRuleViolationException;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The {@code CreateSubTaskRequest.deadlineTime} field is typed as {@code String}
 * so the service can accept both strict ISO-8601 ({@code 08:00:00}) and the
 * casual UI form ({@code 8am}). These tests verify the {@code parseDeadlineTime}
 * helper handles every form clients have been observed sending in Swagger.
 */
class SubTaskDeadlineTimeParserTest {

    // ---------- Defaults ----------
    @Test
    void nullDefaultsToEndOfDay() {
        assertEquals(LocalTime.of(23, 59, 59),
                SubTaskServiceImpl.parseDeadlineTime(null));
    }

    @Test
    void blankDefaultsToEndOfDay() {
        assertEquals(LocalTime.of(23, 59, 59),
                SubTaskServiceImpl.parseDeadlineTime("   "));
    }

    // ---------- 24-hour ISO ----------
    @Test
    void parses24HourHm() {
        assertEquals(LocalTime.of(8, 0),
                SubTaskServiceImpl.parseDeadlineTime("08:00"));
    }

    @Test
    void parses24HourHms() {
        assertEquals(LocalTime.of(8, 30, 15),
                SubTaskServiceImpl.parseDeadlineTime("08:30:15"));
    }

    @Test
    void parses24HourNoLeadingZero() {
        assertEquals(LocalTime.of(8, 0),
                SubTaskServiceImpl.parseDeadlineTime("8:00"));
    }

    // ---------- 12-hour UI ----------
    @Test
    void parses8am() {
        assertEquals(LocalTime.of(8, 0),
                SubTaskServiceImpl.parseDeadlineTime("8am"));
    }

    @Test
    void parses8pm() {
        assertEquals(LocalTime.of(20, 0),
                SubTaskServiceImpl.parseDeadlineTime("8pm"));
    }

    @Test
    void parses8AMWithSpacesAndCaseInsensitive() {
        assertEquals(LocalTime.of(8, 0),
                SubTaskServiceImpl.parseDeadlineTime(" 8 AM "));
    }

    @Test
    void parses8Colon30Pm() {
        assertEquals(LocalTime.of(20, 30),
                SubTaskServiceImpl.parseDeadlineTime("8:30 pm"));
    }

    @Test
    void parses12amAsMidnight() {
        assertEquals(LocalTime.of(0, 0),
                SubTaskServiceImpl.parseDeadlineTime("12am"));
    }

    @Test
    void parses12pmAsNoon() {
        assertEquals(LocalTime.of(12, 0),
                SubTaskServiceImpl.parseDeadlineTime("12pm"));
    }

    // ---------- Compact ----------
    @Test
    void parsesCompact4Digits() {
        assertEquals(LocalTime.of(8, 30),
                SubTaskServiceImpl.parseDeadlineTime("0830"));
    }

    @Test
    void parsesCompact6Digits() {
        assertEquals(LocalTime.of(8, 30, 15),
                SubTaskServiceImpl.parseDeadlineTime("083015"));
    }

    // ---------- Invalid → 400 ----------
    @Test
    void invalidStringThrowsWorkflowRuleViolation() {
        assertThrows(WorkflowRuleViolationException.class,
                () -> SubTaskServiceImpl.parseDeadlineTime("garbage"));
    }

    @Test
    void emptyAfterTrimThrows() {
        assertThrows(WorkflowRuleViolationException.class,
                () -> SubTaskServiceImpl.parseDeadlineTime("nn"));
    }

    @Test
    void outOfRangeHourInAmPmThrows() {
        assertThrows(WorkflowRuleViolationException.class,
                () -> SubTaskServiceImpl.parseDeadlineTime("13am"));
    }
}
