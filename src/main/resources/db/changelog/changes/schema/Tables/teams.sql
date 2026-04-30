CREATE TABLE IF NOT EXISTS public.teams
(
    team_id SERIAL PRIMARY KEY,
    date_created timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    team_name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    project_id bigint NOT NULL,
    CONSTRAINT team_name_project_id UNIQUE (team_name, project_id),
    CONSTRAINT fk_teams_project_id FOREIGN KEY (project_id)
        REFERENCES public.projects (project_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

ALTER TABLE public.teams
  ADD COLUMN IF NOT EXISTS lead_applicant_id BIGINT,
  ADD COLUMN IF NOT EXISTS idea_title VARCHAR(255),
  ADD COLUMN IF NOT EXISTS idea_description TEXT;

CREATE INDEX IF NOT EXISTS idx_teams_project_name
    ON teams(project_id, team_name)
    INCLUDE (team_id);
