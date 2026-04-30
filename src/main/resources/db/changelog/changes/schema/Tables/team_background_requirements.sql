
CREATE TABLE IF NOT EXISTS public.team_background_requirements (
  id SERIAL PRIMARY KEY,
  team_id integer NOT NULL REFERENCES public.teams(team_id) ON DELETE CASCADE,
  background_code VARCHAR(100) NOT NULL,
  min_needed INT NOT NULL DEFAULT 0,
  max_needed INT NOT NULL CHECK (max_needed >= 0),
  UNIQUE(team_id, background_code)
);