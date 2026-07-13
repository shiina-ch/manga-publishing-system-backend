-- ============================================================
-- V4: SubTask & Polymorphic Submission Schema
-- Creates SubTask table; converts Submission to polymorphic over
-- (Task, SubTask); adds version/parent/reviewer metadata to
-- Submission; adds file_type/file_order to SubmissionFile.
-- Does NOT drop any existing tables; old ProjectId/PlanningId FKs
-- on Submission are kept nullable for audit/legacy data.
-- ============================================================

-- -------------------------------------------------------
-- 1. Create SubTask table
-- -------------------------------------------------------
CREATE TABLE SubTask (
    Id                   BIGINT IDENTITY(1,1) NOT NULL,
    parent_task_id       BIGINT          NOT NULL,
    assignee_id          BIGINT          NULL,
    title                NVARCHAR(255)   NULL,
    description          NVARCHAR(MAX)   NULL,
    production_task_type NVARCHAR(50)    NULL,        -- OUTLINE|NAME_WIP|LINEART|INKING|BACKGROUND
    subtask_status       NVARCHAR(50)    NOT NULL DEFAULT 'TODO',
                                                      -- TODO|IN_PROGRESS|SUBMITTED|NEEDS_REVISION|COMPLETED
    deadline_date        DATE            NULL,
    deadline_time        TIME(0)         NULL,
    version              INT             NOT NULL DEFAULT 1,
    created_at           DATETIME2       NOT NULL DEFAULT GETDATE(),
    updated_at           DATETIME2       NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_SubTask         PRIMARY KEY (Id),
    CONSTRAINT FK_SubTask_Task    FOREIGN KEY (parent_task_id) REFERENCES Task(Id),
    CONSTRAINT FK_SubTask_Assignee FOREIGN KEY (assignee_id)  REFERENCES Account(id)
);

CREATE INDEX IX_SubTask_Parent   ON SubTask(parent_task_id);
CREATE INDEX IX_SubTask_Assignee ON SubTask(assignee_id);
CREATE INDEX IX_SubTask_Status   ON SubTask(subtask_status);

-- -------------------------------------------------------
-- 2. Task: optional dedicated date/time columns (nullable,
--    keeps legacy Deadline Instant untouched for backward compat).
-- -------------------------------------------------------
IF COL_LENGTH('dbo.Task', 'deadline_date') IS NULL
    ALTER TABLE Task ADD deadline_date DATE NULL;
IF COL_LENGTH('dbo.Task', 'deadline_time') IS NULL
    ALTER TABLE Task ADD deadline_time TIME(0) NULL;

-- V4 (GĐ3): roll-up cache columns
IF COL_LENGTH('dbo.Task', 'progress_percentage') IS NULL
    ALTER TABLE Task ADD progress_percentage INT NOT NULL DEFAULT 0;

IF COL_LENGTH('dbo.ProductionPlan', 'completion_percentage') IS NULL
    ALTER TABLE ProductionPlan ADD completion_percentage INT NOT NULL DEFAULT 0;

-- -------------------------------------------------------
-- 3. Submission: convert to polymorphic over Task / SubTask.
--    Legacy ProjectId/PlanningId columns preserved (nullable).
-- -------------------------------------------------------
IF COL_LENGTH('dbo.Submission', 'submittable_task_id') IS NULL BEGIN
    ALTER TABLE Submission ADD
        submittable_task_id     BIGINT NULL,
        submittable_subtask_id  BIGINT NULL,
        submission_type         NVARCHAR(50) NOT NULL DEFAULT 'TASK_LEVEL',
                                                        -- ROUGH_SKETCH|REVISION|FINAL|TASK_LEVEL
        parent_submission_id    BIGINT NULL,
        reviewer_id             BIGINT NULL,
        reviewed_at             DATETIME2 NULL;
END

IF COL_LENGTH('dbo.Submission', 'version') IS NOT NULL
BEGIN
    -- Drop the indexes that reference `version` first
    IF EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Sub_Submittable_Task')
        DROP INDEX IX_Sub_Submittable_Task ON Submission;
    IF EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Sub_Submittable_SubTask')
        DROP INDEX IX_Sub_Submittable_SubTask ON Submission;

    -- Drop the column itself
    ALTER TABLE Submission DROP COLUMN version;
END

-- New polymorphic FKs
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_Submission_SubTask')
    ALTER TABLE Submission ADD CONSTRAINT FK_Submission_SubTask
        FOREIGN KEY (submittable_subtask_id) REFERENCES SubTask(Id);

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_Submission_Task')
    ALTER TABLE Submission ADD CONSTRAINT FK_Submission_Task
        FOREIGN KEY (submittable_task_id) REFERENCES Task(Id);

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_Submission_Parent')
    ALTER TABLE Submission ADD CONSTRAINT FK_Submission_Parent
        FOREIGN KEY (parent_submission_id) REFERENCES Submission(Id);

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_Submission_Reviewer')
    ALTER TABLE Submission ADD CONSTRAINT FK_Submission_Reviewer
        FOREIGN KEY (reviewer_id) REFERENCES Account(id);

-- Polymorphic integrity: exactly one of (task, subtask) must be set
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_Submission_Polymorphic')
    ALTER TABLE Submission ADD CONSTRAINT CK_Submission_Polymorphic CHECK (
        (submittable_task_id    IS NOT NULL AND submittable_subtask_id IS NULL) OR
        (submittable_task_id    IS NULL     AND submittable_subtask_id IS NOT NULL) OR
        (submittable_task_id    IS NULL     AND submittable_subtask_id IS NULL)  -- legacy rows
    );

CREATE INDEX IX_Sub_Submittable_Task    ON Submission(submittable_task_id, submitted_at);
CREATE INDEX IX_Sub_Submittable_SubTask ON Submission(submittable_subtask_id, submitted_at);

-- -------------------------------------------------------
-- 4. SubmissionFile: file_type enum + ordering
-- -------------------------------------------------------
IF COL_LENGTH('dbo.SubmissionFile', 'file_type') IS NULL
    ALTER TABLE SubmissionFile ADD file_type NVARCHAR(50) NULL;
                                                -- ROUGH_SKETCH|REVISION|FINAL|COMPILATION

IF COL_LENGTH('dbo.SubmissionFile', 'file_order') IS NULL
    ALTER TABLE SubmissionFile ADD file_order INT NULL;

CREATE INDEX IX_SF_Submission_Order ON SubmissionFile(submission_id, file_order);
