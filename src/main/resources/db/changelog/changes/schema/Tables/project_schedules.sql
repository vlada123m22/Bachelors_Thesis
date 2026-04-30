CREATE TABLE IF NOT EXISTS public.project_schedules
(
    schedule_id SERIAL PRIMARY KEY,
    project_id int NOT NULL,
    day_number integer NOT NULL DEFAULT 1,
    start_time timestamp(6) with time zone NOT NULL,
    end_time timestamp(6) with time zone NOT NULL,
    activity_title character varying(255) NOT NULL,
    activity_description character varying(1000),

    CONSTRAINT fk_schedules_project_id FOREIGN KEY (project_id)
    REFERENCES public.projects (project_id) ON DELETE CASCADE
    );