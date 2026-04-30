CREATE TABLE IF NOT EXISTS public.team_member_roles (
  id SERIAL PRIMARY KEY,
  team_id int NOT NULL REFERENCES public.teams(team_id) ON DELETE CASCADE,
  member_id int NOT NULL REFERENCES public.team_members(team_member_id) ON DELETE CASCADE,
  role_code VARCHAR(100) NOT NULL
);