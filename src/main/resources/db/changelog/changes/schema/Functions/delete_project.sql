CREATE OR REPLACE FUNCTION public.delete_project(
    IN p_project_id INT,
    IN p_user_id INT,
    OUT p_result VARCHAR(50))
LANGUAGE plpgsql
COST 10
VOLATILE PARALLEL UNSAFE
AS $BODY$
	DECLARE err_code INT;
	DECLARE msg_text VARCHAR(1000);
	DECLARE exc_context VARCHAR(1000);
	DECLARE msg_detail VARCHAR(1000);
	DECLARE exc_hint VARCHAR(1000);
BEGIN

    IF NOT EXISTS (SELECT 1 FROM projects p 
                   WHERE p.project_id = p_project_id 
                   AND p.organizer_id = p_user_id) THEN
        p_result := 'Project Not Found'; -- project does not exist or user does not have access
        RETURN;
END IF;

BEGIN
DROP TABLE IF EXISTS question_ids;
CREATE TEMP TABLE question_ids (question_id INT PRIMARY KEY);

INSERT INTO question_ids (question_id)
SELECT q.question_id
FROM form_questions q
WHERE q.project_id = p_project_id;

DELETE FROM question_answers
    USING question_ids
WHERE question_answers.question_id = question_ids.question_id;

DELETE FROM form_questions
    USING question_ids
WHERE form_questions.question_id = question_ids.question_id;

DELETE FROM applicants
WHERE project_id = p_project_id;

DELETE FROM projects
WHERE project_id = p_project_id;
p_result := 'Success';
    	RETURN;
EXCEPTION WHEN OTHERS
	THEN
	        GET STACKED DIAGNOSTICS
            err_code := RETURNED_SQLSTATE,
            msg_text := MESSAGE_TEXT,
            exc_context := PG_CONTEXT,
            msg_detail := PG_EXCEPTION_DETAIL,
            exc_hint := PG_EXCEPTION_HINT;

        -- Build a single string using FORMAT
        p_result := FORMAT('ERROR CODE: %s MESSAGE: %s CONTEXT: %s DETAIL: %s HINT: %s',
                           err_code, msg_text, exc_context, msg_detail, exc_hint);
END;


END;
$BODY$;