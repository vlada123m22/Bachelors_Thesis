CREATE TABLE IF NOT EXISTS public.team_members (
  team_member_id SERIAL PRIMARY KEY,
  team_id int NOT NULL REFERENCES public.teams(team_id) ON DELETE CASCADE,
  applicant_id int NOT NULL REFERENCES public.applicants(applicant_id) ON DELETE CASCADE,
  joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(team_id, applicant_id)
);
