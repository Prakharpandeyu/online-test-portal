CREATE TABLE exam_assignments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  company_id BIGINT NOT NULL,
  exam_id BIGINT NOT NULL,
  employee_id BIGINT NOT NULL,
  assigned_by BIGINT NOT NULL,
  assigned_by_role VARCHAR(50) NOT NULL,
  start_time TIMESTAMP NULL,
  end_time TIMESTAMP NULL,
  max_attempts INT NOT NULL DEFAULT 1,
  status VARCHAR(30) NOT NULL DEFAULT 'ASSIGNED',
  created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_ea_exam FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
  INDEX idx_ea_company_emp (company_id, employee_id),
  INDEX idx_ea_company_exam (company_id, exam_id),
  INDEX idx_ea_status_time (status, start_time, end_time)
);
