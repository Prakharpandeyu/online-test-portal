CREATE TABLE exams (
    id BIGINT NOT NULL AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    total_questions INT NOT NULL,
    duration_minutes INT NOT NULL,
    created_by BIGINT NOT NULL,
    created_by_role VARCHAR(50) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_exams_company_id (company_id)
);

CREATE TABLE exam_questions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    exam_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    position INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_eq_exam FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    INDEX idx_eq_exam_id (exam_id),
    INDEX idx_eq_question_id (question_id)
);
CREATE INDEX idx_questions_company_topic_active ON questions(company_id, topic_id, is_active);
