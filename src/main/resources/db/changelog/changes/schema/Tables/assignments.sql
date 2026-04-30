CREATE TABLE IF NOT EXISTS assignments (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date TIMESTAMP WITH TIME ZONE,
    mentor_id integer REFERENCES users(id),
    project_id integer REFERENCES projects(project_id)
);