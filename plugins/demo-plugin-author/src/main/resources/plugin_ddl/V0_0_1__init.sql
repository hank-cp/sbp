-- ref: https://stackoverflow.com/questions/18389124
-- SELECT 'CREATE DATABASE sbp'
-- WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sbp')\gexec

CREATE SCHEMA IF NOT EXISTS plugin_author;

DROP TABLE IF EXISTS "Author";
CREATE TABLE "Author" (
    id       bigserial CONSTRAINT author_pk PRIMARY KEY,
    name     varchar(255)
);

DROP TABLE IF EXISTS "AuthorBooks";
CREATE TABLE "AuthorBooks" (
    authorId bigint,
    bookId   bigint
);