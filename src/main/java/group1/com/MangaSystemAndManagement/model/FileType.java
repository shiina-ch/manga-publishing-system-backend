package group1.com.MangaSystemAndManagement.model;

/**
 * Discriminator carried on every {@link SubmissionFile} so clients can compare
 * multiple rounds of work without having to walk the submission chain.
 *
 * <p>Mirrors {@link SubmissionType} but adds {@link #COMPILATION} which is used
 * only for files attached to a {@code TASK_LEVEL} submission.</p>
 */
public enum FileType {
    ROUGH_SKETCH,
    REVISION,
    FINAL,
    COMPILATION
}
