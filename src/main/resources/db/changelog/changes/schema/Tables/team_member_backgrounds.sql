CREATE TABLE IF NOT EXISTS team_member_backgrounds (
    id SERIAL PRIMARY KEY,
    team_id int NOT NULL,
    member_id int NOT NULL,
    background_code VARCHAR(255) NOT NULL,

    CONSTRAINT fk_team_member_backgrounds_team
    FOREIGN KEY (team_id)
        REFERENCES teams(team_id),

    CONSTRAINT fk_team_member_backgrounds_member
    FOREIGN KEY (member_id)
        REFERENCES team_members(team_member_id)
);