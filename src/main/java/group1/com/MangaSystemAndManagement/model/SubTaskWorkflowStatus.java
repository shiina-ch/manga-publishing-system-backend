package group1.com.MangaSystemAndManagement.model;

/**
 * Workflow states for a SubTask assigned by a Mangaka to an Assistant.
 * Distinct from {@link TaskWorkflowStatus} (used for the parent chapter-level Task)
 * so that the two lifecycles do not collide.
 */
public enum SubTaskWorkflowStatus {
    /** SubTask created but Assistant has not yet started. */
    TODO,
    /** Assistant has begun work but has not yet submitted any file. */
    IN_PROGRESS,
    /** Assistant uploaded at least one ROUGH_SKETCH pending Mangaka review. */
    SUBMITTED,
    /** Latest submission was REJECTED — Assistant must revise and re-submit. */
    NEEDS_REVISION,
    /** Final submission was APPROVED by Mangaka — SubTask is done. */
    COMPLETED
}
