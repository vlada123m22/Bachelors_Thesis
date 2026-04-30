CREATE TABLE IF NOT EXISTS public.projects
(
    project_id SERIAL PRIMARY KEY,
    created_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    max_nr_participants integer,
    min_nr_participants integer,
    project_name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    organizer_id bigint NOT NULL,
    end_date timestamp(6) without time zone,
    project_description character varying(5000) COLLATE pg_catalog."default",
    start_date timestamp(6) without time zone,
    teams_preformed boolean DEFAULT false,

    CONSTRAINT fk_projects_organizer_id FOREIGN KEY (organizer_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

ALTER TABLE public.projects
    ADD COLUMN IF NOT EXISTS schedule_visibility character varying(50) DEFAULT 'EVERYBODY';

ALTER TABLE projects
  ADD COLUMN IF NOT EXISTS roles_options VARCHAR(1000),
  ADD COLUMN IF NOT EXISTS background_options VARCHAR(1000),
  ADD COLUMN IF NOT EXISTS roles_question_text VARCHAR(500) DEFAULT 'What are your preferred roles in the team?',
  ADD COLUMN IF NOT EXISTS background_question_text VARCHAR(500) DEFAULT 'What is your background?';