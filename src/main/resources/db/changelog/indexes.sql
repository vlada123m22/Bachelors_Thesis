-- For teams lookup (checking team name within project)
CREATE INDEX idx_teams_project_name
ON teams(project_id, team_name)
    INCLUDE (team_id);

-- For applicants lookup (checking existing applicant by name/project/team)
CREATE INDEX idx_applicants_name_project_team
ON applicants(first_name, last_name, project_id, team_id)
    INCLUDE (applicant_id);

-- For applicants by project_id (faster filtering)
CREATE INDEX idx_applicants_project
ON applicants(project_id)
    INCLUDE (applicant_id, team_id);


CREATE INDEX idx_form_question_project_id
    ON form_questions(project_id)
    INCLUDE (question_id, question_Number, question_Type, question, checkbox_options);

CREATE INDEX IF NOT EXISTS idx_question_answers_question_id
    ON question_answers(question_id)