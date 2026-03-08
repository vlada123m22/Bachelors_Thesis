CREATE TABLE IF NOT EXISTS public.team_members (
  id BIGSERIAL PRIMARY KEY,
  team_id BIGINT NOT NULL REFERENCES public.teams(team_id) ON DELETE CASCADE,
  applicant_id BIGINT NOT NULL REFERENCES public.applicants(applicant_id) ON DELETE CASCADE,
  joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(team_id, applicant_id)
);
