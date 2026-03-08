CREATE TABLE submissions (
                             id BIGSERIAL PRIMARY KEY,
                             assignment_id BIGINT REFERENCES assignments(id),
                             team_id BIGINT REFERENCES teams(team_id),
                             uploaded_by_id BIGINT REFERENCES users(id),
                             text_content TEXT,
                             file_path VARCHAR(500),
                             upload_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);