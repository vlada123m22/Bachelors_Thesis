CREATE TABLE IF NOT EXISTS submissions (
    id SERIAL PRIMARY KEY,
    assignment_id integer REFERENCES assignments(id),
    team_id integer REFERENCES teams(team_id),
    uploaded_by_id integer REFERENCES users(id),
    text_content TEXT,
    file_path VARCHAR(500),
    upload_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);