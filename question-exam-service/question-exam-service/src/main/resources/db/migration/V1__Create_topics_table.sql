CREATE TABLE topics (
    id BIGINT NOT NULL AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_by BIGINT NOT NULL,
    created_by_role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_topics_company_id ON topics(company_id);
CREATE UNIQUE INDEX uk_topics_company_name ON topics(company_id, name);
CREATE INDEX idx_topics_role ON topics(created_by_role);
