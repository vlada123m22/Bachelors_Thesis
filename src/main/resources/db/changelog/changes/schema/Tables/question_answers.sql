CREATE TABLE IF NOT EXISTS public.question_answers
(
    question_answer_id SERIAL PRIMARY KEY,
    question_answer character varying(5000),
    applicant_id integer NOT NULL,
    question_id integer NOT NULL,

    CONSTRAINT fk_question_answers_applicants_id FOREIGN KEY (applicant_id)
        REFERENCES public.applicants (applicant_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_question_answers_question_id FOREIGN KEY (question_id)
        REFERENCES public.form_questions (question_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE INDEX IF NOT EXISTS idx_question_answers_question_id
    ON question_answers(question_id);