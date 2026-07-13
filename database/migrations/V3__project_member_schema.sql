-- ============================================================
-- V3: Project Role & Project Member Schema
-- Creates the project_roles lookup table and project_members
-- join table. Does NOT touch any existing tables.
-- ============================================================

-- -------------------------------------------------------
-- 1. Create ProjectRole lookup table
-- -------------------------------------------------------
CREATE TABLE ProjectRole (
    Id          BIGINT IDENTITY(1,1) NOT NULL,
    RoleName    NVARCHAR(100)        NOT NULL,
    Description NVARCHAR(500)        NULL,

    CONSTRAINT PK_ProjectRole PRIMARY KEY (Id)
);

-- -------------------------------------------------------
-- 2. Create project_members join table
--    (Project × Account, with an optional ProjectRole)
-- -------------------------------------------------------
CREATE TABLE project_members (
    project_id      BIGINT    NOT NULL,
    account_id      BIGINT    NOT NULL,
    project_role_id BIGINT    NULL,
    joined_at       DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_project_members      PRIMARY KEY (project_id, account_id),
    CONSTRAINT FK_pm_Project           FOREIGN KEY (project_id)      REFERENCES Project(Id),
    CONSTRAINT FK_pm_Account           FOREIGN KEY (account_id)      REFERENCES Account(id),
    CONSTRAINT FK_pm_ProjectRole       FOREIGN KEY (project_role_id) REFERENCES ProjectRole(Id)
);
