CREATE TABLE assignments (
                             id BIGSERIAL PRIMARY KEY,
                             title VARCHAR(255) NOT NULL,
                             description TEXT,
                             due_date TIMESTAMP WITH TIME ZONE,
                             mentor_id BIGINT REFERENCES users(id),
                             project_id BIGINT REFERENCES projects(project_id)
);