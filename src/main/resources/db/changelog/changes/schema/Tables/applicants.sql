CREATE TABLE IF NOT EXISTS public.applicants
(
    applicant_id SERIAL PRIMARY KEY,
    first_name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    has_applied boolean NOT NULL DEFAULT FALSE,
    is_selected boolean NOT NULL DEFAULT FALSE,
    last_name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    registration_timestamp timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    timezone character varying(50) COLLATE pg_catalog."default" NOT NULL DEFAULT 'Europe/Chisinau',
    project_id integer NOT NULL,
    team_id integer,
    user_id integer,
    CONSTRAINT FK_applicants_team_id FOREIGN KEY (team_id)
        REFERENCES public.teams (team_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT FK_Applicants_project_id FOREIGN KEY (project_id)
        REFERENCES public.projects (project_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT FK_applicants_user_id FOREIGN KEY (user_id)
    REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

ALTER TABLE applicants
  ADD COLUMN IF NOT EXISTS roles VARCHAR(1000),
  ADD COLUMN IF NOT EXISTS background VARCHAR(1000);


DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_teams_lead_applicant'
    ) THEN
ALTER TABLE public.teams
    ADD CONSTRAINT fk_teams_lead_applicant
        FOREIGN KEY (lead_applicant_id)
            REFERENCES public.applicants (applicant_id)
            ON UPDATE NO ACTION
            ON DELETE NO ACTION;
END IF;
END $$;

-- For applicants lookup (checking existing applicant by name/project/team)
CREATE INDEX IF NOT EXISTS idx_applicants_name_project_team
    ON applicants(first_name, last_name, project_id, team_id)
    INCLUDE (applicant_id);

-- For applicants by project_id (faster filtering)
CREATE INDEX IF NOT EXISTS idx_applicants_project
    ON applicants(project_id)
    INCLUDE (applicant_id, team_id);
