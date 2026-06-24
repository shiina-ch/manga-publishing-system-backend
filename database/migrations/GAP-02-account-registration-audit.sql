/*
  GAP-02 account registration audit columns for SQL Server.
  requested_role intentionally remains nullable for bootstrap ADMIN and legacy accounts.
*/

IF COL_LENGTH('dbo.Account', 'approved_by_id') IS NULL
    ALTER TABLE dbo.Account ADD approved_by_id BIGINT NULL;

IF COL_LENGTH('dbo.Account', 'approved_at') IS NULL
    ALTER TABLE dbo.Account ADD approved_at DATETIME2 NULL;

IF COL_LENGTH('dbo.Account', 'rejection_reason') IS NULL
    ALTER TABLE dbo.Account ADD rejection_reason NVARCHAR(1000) NULL;

IF COL_LENGTH('dbo.Account', 'rejected_by_id') IS NULL
    ALTER TABLE dbo.Account ADD rejected_by_id BIGINT NULL;

IF COL_LENGTH('dbo.Account', 'rejected_at') IS NULL
    ALTER TABLE dbo.Account ADD rejected_at DATETIME2 NULL;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_Account_Status_RequestedRole'
      AND object_id = OBJECT_ID('dbo.Account')
)
    CREATE INDEX IX_Account_Status_RequestedRole
        ON dbo.Account (status, requested_role);
