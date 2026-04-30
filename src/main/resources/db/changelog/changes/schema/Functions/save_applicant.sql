--================================================
-- Applicant_Id error codes:
-- 0 - project not found
-- -1 - an account is required to apply
-- -2 - team already exists
-- -3 - invalid roles or backgrounds

--================================================


CREATE OR REPLACE FUNCTION public.save_applicant(
	in_user_id integer,
	in_project_id integer,
	in_first_name character varying,
	in_last_name character varying,
	in_team_name character varying,
	in_join_existent_team boolean,
	in_time_zone character varying,
	date_submitted timestamp with time zone,
	in_roles character varying,
	in_background character varying)
    RETURNS TABLE(applicant_id integer, team_id integer)
    LANGUAGE 'plpgsql'
    COST 50
    VOLATILE PARALLEL UNSAFE
    ROWS 1

AS $BODY$
	DECLARE out_team_id INT;
	DECLARE	existent_applicant_id INT;
	DECLARE v_team_id INT;
	DECLARE pr_teams_preformed BOOLEAN;
	DECLARE v_applicant_id INT;
BEGIN
DROP TABLE IF EXISTS project_info;
-- Create temp table for project info
CREATE TEMP TABLE IF NOT EXISTS project_info (
        project_id INT PRIMARY KEY,
        teams_preformed BOOLEAN,
        background_options VARCHAR(1000),
        roles_options VARCHAR(1000)
    ) ON COMMIT DROP;

   -- Insert project info
INSERT INTO project_info (project_id, teams_preformed, background_options, roles_options)
SELECT project_id, teams_preformed, background_options, roles_options
FROM projects
WHERE project_id = in_project_id;

-- Check if project exists
IF NOT FOUND THEN
        RETURN QUERY SELECT 0, NULL::integer; -- 0 - applicantId error code for Project Not Found
END IF;

SELECT teams_preformed INTO pr_teams_preformed
FROM project_info
WHERE project_id = in_project_id;

-- FIX #2: Changed 'false' (string) to false (boolean)
IF (pr_teams_preformed = false AND in_user_id IS NULL) THEN
		RETURN QUERY SELECT -1, NULL::integer; -- -1 - applicantId error code for Account Needed
END IF;

-- FIX #3: Changed `:=` to proper SELECT ... INTO syntax
SELECT t.team_id INTO out_team_id
FROM teams t
WHERE t.project_id = in_project_id
  AND t.team_name = in_team_name
    LIMIT 1;

IF (out_team_id IS NOT NULL AND NOT(in_join_existent_team))
	THEN
		RETURN QUERY SELECT -2, NULL::integer; -- -2 - applicantId error code for Duplicate Team Name
END IF;

	IF NOT EXISTS(SELECT 1
				FROM project_info
				WHERE (background_options LIKE '%|' || in_background || '|%' OR background_options LIKE '%|' || in_background OR background_options LIKE in_background || '|%')
				AND (roles_options LIKE '%|' || in_roles || '|%' OR roles_options = in_roles OR roles_options LIKE '%|' || in_roles OR roles_options LIKE in_roles || '|%'))
		THEN RETURN QUERY SELECT -3, NULL::integer; -- -3 - applicantId error code for Wrong Roles/Backgrounds
END IF;

	--Save the applicant without a team
	IF (in_team_name IS NULL) THEN
		-- FIX #1: Changed date_submited to date_submitted
		INSERT INTO applicants (first_name, last_name, has_applied, registration_timestamp, is_selected, timezone, project_id, user_id, roles, background)
		VALUES (in_first_name, in_last_name, true, date_submitted, false, in_time_zone, in_project_id, in_user_id, in_roles, in_background)
		RETURNING applicants.applicant_id INTO v_applicant_id;
RETURN QUERY SELECT v_applicant_id, NULL::integer;
RETURN;
END IF;

-- If the team with in_team_name does not exist in the DB, create it
	IF (out_team_id IS NULL) THEN
		INSERT INTO teams (team_name, project_id)
		VALUES(in_team_name, in_project_id)
		RETURNING teams.team_id INTO v_team_id;
ELSE
        v_team_id := out_team_id;

END IF;

	--=======================
	-- If the applicant has applied with a team
	--=======================
	--Check if the applicant exists
SELECT a.applicant_id INTO existent_applicant_id
FROM applicants a
WHERE a.first_name = in_first_name
  AND a.last_name = in_last_name
  AND a.project_id = in_project_id
  AND a.team_id = v_team_id
    LIMIT 1;

--If exists, update has_applied to true, otherwise create it
IF (existent_applicant_id IS NOT NULL)
	THEN
UPDATE applicants a
SET has_applied = true,
    registration_timestamp = date_submitted,
    timezone = in_time_zone
WHERE a.applicant_id = existent_applicant_id
    RETURNING a.applicant_id INTO v_applicant_id;
RETURN QUERY SELECT v_applicant_id, v_team_id;
RETURN;
ELSE
		INSERT INTO applicants (first_name, last_name, project_id, team_id, has_applied, is_selected, registration_timestamp, timezone, roles, background)
		VALUES(in_first_name, in_last_name, in_project_id, v_team_id, true, false, date_submitted, in_time_zone, in_roles, in_background)
		RETURNING applicants.applicant_id INTO v_applicant_id;
RETURN QUERY SELECT v_applicant_id, v_team_id;
RETURN;
END IF;
	RETURN;
END;
$BODY$;

