package group1.com.MangaSystemAndManagement.model;

/**
 * Type of a {@link Submission} in the production workflow.
 *
 * <ul>
 *   <li>{@link #ROUGH_SKETCH} – first submission by Assistant on a SubTask.</li>
 *   <li>{@link #REVISION}     – re-submission after the previous one was rejected
 *       (links to {@code parentSubmissionId}).</li>
 *   <li>{@link #FINAL}        – clean copy the Assistant produces once the rough
 *       sketch is approved; approval of this version completes the SubTask.</li>
 *   <li>{@link #TASK_LEVEL}   – Mangaka submission to Tantō at the parent Task
 *       level, after every SubTask is COMPLETED.</li>
 * </ul>
 */
public enum SubmissionType {
    ROUGH_SKETCH,
    REVISION,
    FINAL,
    TASK_LEVEL
}
