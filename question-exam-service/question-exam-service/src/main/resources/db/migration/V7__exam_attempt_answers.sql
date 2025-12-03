CREATE TABLE exam_attempt_answers (
  id BIGINT NOT NULL AUTO_INCREMENT,
  attempt_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  selected ENUM('A','B','C','D') NULL,
  is_correct BOOLEAN NOT NULL,
  position INT NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_attempt_answers_attempt (attempt_id),
  INDEX idx_attempt_answers_q (question_id),
  CONSTRAINT fk_attempt_answers_attempt FOREIGN KEY (attempt_id) REFERENCES exam_attempts(id) ON DELETE CASCADE
);
