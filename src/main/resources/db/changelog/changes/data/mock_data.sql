-- Create Organizer
INSERT INTO public.users (user_name, password, email) 
VALUES ('organizer', '$2a$10$8.UnVuG9HHgffUDAlk8q6uy5QLKGvS0Wd.vX1A47.XmI6r6r6r6r6', 'organizer@example.com')
ON CONFLICT (user_name) DO NOTHING;

-- Create Project with teams_preformed = true
INSERT INTO public.projects (project_name, organizer_id, teams_preformed, project_description, start_date, end_date)
SELECT 'Hackathon 2026', id, true, 'A big hackathon with preformed teams.', '2026-06-01 09:00:00', '2026-06-03 18:00:00'
FROM public.users WHERE user_name = 'organizer'
ON CONFLICT DO NOTHING;

-- Create 30 Users
DO $$
BEGIN
    FOR i IN 1..30 LOOP
        INSERT INTO public.users (user_name, password, email)
        VALUES ('user' || i, '$2a$10$8.UnVuG9HHgffUDAlk8q6uy5QLKGvS0Wd.vX1A47.XmI6r6r6r6r6', 'user' || i || '@example.com')
        ON CONFLICT (user_name) DO NOTHING;
    END LOOP;
END $$;

-- Create 5 Teams
DO $$
DECLARE
    proj_id INT;
BEGIN
    SELECT project_id INTO proj_id FROM public.projects WHERE project_name = 'Hackathon 2026' LIMIT 1;
    FOR i IN 1..5 LOOP
        INSERT INTO public.teams (team_name, project_id, idea_title, idea_description)
        VALUES ('Team ' || i, proj_id, 'Project Idea ' || i, 'Description for project idea ' || i)
        ON CONFLICT (team_name, project_id) DO NOTHING;
    END LOOP;
END $$;

-- Create Applicants and assign them to Teams (6 members per team)
DO $$
DECLARE
    proj_id INT;
    team_rec RECORD;
    user_rec RECORD;
    counter INT := 0;
    current_team_id INT;
BEGIN
    SELECT project_id INTO proj_id FROM public.projects WHERE project_name = 'Hackathon 2026' LIMIT 1;
    
    FOR user_rec IN (SELECT id, user_name FROM public.users WHERE user_name LIKE 'user%' ORDER BY id) LOOP
        -- Rotate through teams: (0-5 -> Team 1, 6-11 -> Team 2, etc. - roughly)
        -- More accurately:
        SELECT team_id INTO current_team_id FROM public.teams WHERE project_id = proj_id ORDER BY team_id OFFSET (counter / 6) LIMIT 1;
        
        INSERT INTO public.applicants (first_name, last_name, project_id, team_id, user_id, has_applied, is_selected)
        VALUES (
            'First' || (counter + 1), 
            'Last' || (counter + 1), 
            proj_id, 
            current_team_id, 
            user_rec.id, 
            true, 
            true
        )
        ON CONFLICT DO NOTHING;
        
        counter := counter + 1;
        EXIT WHEN counter >= 30;
    END LOOP;
END $$;

-- Set lead applicants for teams
DO $$
DECLARE
    team_rec RECORD;
    lead_id INT;
BEGIN
    FOR team_rec IN (SELECT team_id FROM public.teams) LOOP
        SELECT applicant_id INTO lead_id FROM public.applicants WHERE team_id = team_rec.team_id LIMIT 1;
        UPDATE public.teams SET lead_applicant_id = lead_id WHERE team_id = team_rec.team_id;
    END LOOP;
END $$;
