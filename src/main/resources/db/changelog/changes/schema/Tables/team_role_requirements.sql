CREATE TABLE IF NOT EXISTS public.team_role_requirements (
  id BIGSERIAL PRIMARY KEY,
  team_id BIGINT NOT NULL REFERENCES public.teams(team_id) ON DELETE CASCADE,
  role_code VARCHAR(100) NOT NULL,
  min_needed INT NOT NULL DEFAULT 0,
  max_needed INT NOT NULL CHECK (max_needed >= 0),
  UNIQUE(team_id, role_code)
);