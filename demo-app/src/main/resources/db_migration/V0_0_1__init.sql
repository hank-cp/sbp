-- ref: https://stackoverflow.com/questions/18389124
-- SELECT 'CREATE DATABASE sbp'
-- WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sbp')\gexec

CREATE SCHEMA IF NOT EXISTS demo_app;

DROP TABLE IF EXISTS "Book";
CREATE TABLE "Book" (
    id       bigserial CONSTRAINT book_pk PRIMARY KEY,
    name     varchar(255) UNIQUE,
    extra    jsonb
);
