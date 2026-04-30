CREATE TABLE IF NOT EXISTS public.form_questions
(
    created_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    question character varying(1000) NOT NULL,
    question_number integer NOT NULL,
    question_type character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    project_id integer NOT NULL,
    question_id SERIAL PRIMARY KEY,
    checkbox_options character varying(1000),

    CONSTRAINT fk_form_questions_project_id FOREIGN KEY (project_id)
        REFERENCES public.projects (project_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT form_questions_question_type_check CHECK (question_type::text = ANY (ARRAY['FILE'::character varying, 'CHECKBOX'::character varying, 'TEXT'::character varying]::text[]))
);

CREATE INDEX IF NOT EXISTS idx_form_question_project_id
    ON form_questions(project_id)
    INCLUDE (question_id, question_Number, question_Type, question, checkbox_options);