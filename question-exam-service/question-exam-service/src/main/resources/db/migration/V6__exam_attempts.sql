CREATE TABLE exam_attempts (
  id BIGINT NOT NULL AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  exam_id BIGINT NOT NULL,
  assignment_id BIGINT NOT NULL,
  employee_id BIGINT NOT NULL,
  attempt_number INT NOT NULL,
  total_questions INT NOT NULL,
  correct_answers INT NOT NULL,
  percentage INT NOT NULL,
  passed BOOLEAN NOT NULL,
  duration_seconds INT NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_attempts_assignment (assignment_id),
  INDEX idx_attempts_exam_emp (exam_id, employee_id)
);
