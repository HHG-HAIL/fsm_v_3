-- V6__add_completion_fields_to_service_tasks.sql (H2 Compatible)
-- Adds completed_at and work_summary columns to service_tasks table to track task completion

ALTER TABLE service_tasks ADD COLUMN completed_at TIMESTAMP;
ALTER TABLE service_tasks ADD COLUMN work_summary CLOB;

-- Index for querying tasks by completion time
CREATE INDEX IF NOT EXISTS idx_service_tasks_completed_at ON service_tasks(completed_at);
