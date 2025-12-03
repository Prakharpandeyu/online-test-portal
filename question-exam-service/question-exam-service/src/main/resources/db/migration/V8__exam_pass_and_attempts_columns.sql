ALTER TABLE exams
  ADD COLUMN passing_percentage INT NULL;

ALTER TABLE exam_assignments
  ADD COLUMN attempts_used INT NOT NULL DEFAULT 0,
  ADD COLUMN last_result VARCHAR(15) NULL,
  ADD COLUMN last_percentage INT NULL,
  ADD COLUMN last_submitted_at TIMESTAMP NULL;
