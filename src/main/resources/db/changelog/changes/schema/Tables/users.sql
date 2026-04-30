
CREATE TABLE IF NOT EXISTS public.users
(
    id SERIAL PRIMARY KEY,
    creation_date_time timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP,
    password character varying(255) COLLATE pg_catalog."default",
    user_name character varying(255) COLLATE pg_catalog."default",
    email character varying(255) UNIQUE,
    CONSTRAINT users_user_name UNIQUE (user_name)
);