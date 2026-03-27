CREATE TABLE team_applications (
                                   id BIGSERIAL PRIMARY KEY,

                                   team_id BIGINT NOT NULL,
                                   applicant_id BIGINT NOT NULL,

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