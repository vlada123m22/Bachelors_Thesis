CREATE TABLE IF NOT EXISTS team_applications (
    team_application_id SERIAL PRIMARY KEY,

    team_id integer NOT NULL,
    applicant_id integer NOT NULL,

    status VARCHAR(20) NOT NULL,

    applied_at TIMESTAMPTZ NOT NULL,
    decision_at TIMESTAMPTZ,

    CONSTRAINT uq_team_applicant UNIQUE (team_id, applicant_id),

    CONSTRAINT fk_team_applications_team
        FOREIGN KEY (team_id)
            REFERENCES teams(team_id),

    CONSTRAINT fk_team_applications_applicant
        FOREIGN KEY (applicant_id)
            REFERENCES applicants(applicant_id)
);