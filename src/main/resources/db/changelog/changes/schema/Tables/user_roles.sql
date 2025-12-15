CREATE TABLE IF NOT EXISTS public.user_roles
(
    user_id bigint NOT NULL,
    roles character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT fk_user_roles_user_id FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT user_roles_roles_check CHECK (roles::text = ANY (ARRAY['ORGANIZER'::character varying, 'PARTICIPANT'::character varying, 'ADMIN'::character varying]::text[]))
)
