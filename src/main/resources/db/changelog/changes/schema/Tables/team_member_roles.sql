CREATE TABLE IF NOT EXISTS public.team_member_roles (
  id BIGSERIAL PRIMARY KEY,
  team_id BIGINT NOT NULL REFERENCES public.teams(team_id) ON DELETE CASCADE,
  member_id BIGINT NOT NULL REFERENCES public.team_members(id) ON DELETE CASCADE,
  role_code VARCHAR(100) NOT NULL
);