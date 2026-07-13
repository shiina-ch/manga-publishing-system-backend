-- ============================================================
-- V2: Production Workflow Schema
-- Adds new columns to existing tables + creates feedbacks & assets
-- NOTE: Does NOT drop or recreate any existing tables.
-- ============================================================

-- -------------------------------------------------------
-- 1. Extend Project table
-- -------------------------------------------------------
ALTER TABLE Project
    ADD genre            NVARCHAR(100)  NULL,
        target_audience  NVARCHAR(100)  NULL,
        format           NVARCHAR(50)   NULL,  -- WEEKLY_SHONEN | MONTHLY_SEINEN | WEBTOON
        project_status   NVARCHAR(50)   NULL DEFAULT 'DRAFT'; -- DRAFT | ACTIVE | ON_HOLD | COMPLETED

-- -------------------------------------------------------
-- 2. Extend ProductionPlan table
-- -------------------------------------------------------
ALTER TABLE ProductionPlan
    ADD start_date           DATE          NULL,
        end_date             DATE          NULL,
        total_volume_target  INT           NULL,
        plan_status          NVARCHAR(50)  NULL DEFAULT 'PLANNING'; -- PLANNING | IN_PROGRESS | PAUSED

-- -------------------------------------------------------
-- 3. Extend Chapter table
-- -------------------------------------------------------
ALTER TABLE Chapter
    ADD target_page_count  INT           NULL,
        publish_date       DATE          NULL,
        chapter_status     NVARCHAR(50)  NULL DEFAULT 'BACKLOG'; -- BACKLOG | IN_PRODUCTION | COMPLETED | PUBLISHED

-- -------------------------------------------------------
-- 4. Extend Task table
-- -------------------------------------------------------
ALTER TABLE Task
    ADD production_task_type NVARCHAR(50)   NULL,   -- OUTLINE | NAME_WIP | LINEART | INKING | BACKGROUND
        task_status          NVARCHAR(50)   NULL DEFAULT 'TODO', -- TODO | IN_PROGRESS | REVIEW | DONE
        acceptance_criteria  NVARCHAR(MAX)  NULL,
        assignee_id          BIGINT         NULL;

ALTER TABLE Task
    ADD CONSTRAINT FK_Task_Assignee
        FOREIGN KEY (assignee_id) REFERENCES Account(id);

-- -------------------------------------------------------
-- 5. Create feedbacks table
-- -------------------------------------------------------
CREATE TABLE feedbacks (
    id             BIGINT IDENTITY(1,1) NOT NULL,
    task_id        BIGINT        NOT NULL,
    created_by     BIGINT        NOT NULL,
    content        NVARCHAR(MAX) NOT NULL,
    attachment_url NVARCHAR(512) NULL,
    decision       NVARCHAR(50)  NOT NULL,   -- APPROVED | REJECTED
    created_at     DATETIME2     NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_feedbacks PRIMARY KEY (id),
    CONSTRAINT FK_feedbacks_Task    FOREIGN KEY (task_id)    REFERENCES Task(Id),
    CONSTRAINT FK_feedbacks_Account FOREIGN KEY (created_by) REFERENCES Account(id)
);

-- -------------------------------------------------------
-- 6. Create assets table
-- -------------------------------------------------------
CREATE TABLE assets (
    id         BIGINT IDENTITY(1,1) NOT NULL,
    project_id BIGINT        NOT NULL,
    name       NVARCHAR(255) NULL,
    category   NVARCHAR(50)  NULL,   -- MODEL_3D | CHARACTER_SHEET | BRUSH | BACKGROUND_TEMPLATE
    file_url   NVARCHAR(512) NULL,

    CONSTRAINT PK_assets PRIMARY KEY (id),
    CONSTRAINT FK_assets_Project FOREIGN KEY (project_id) REFERENCES Project(Id)
);
