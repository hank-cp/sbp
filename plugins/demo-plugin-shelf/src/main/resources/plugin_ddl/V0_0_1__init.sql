-- ref: https://stackoverflow.com/questions/18389124
-- SELECT 'CREATE DATABASE sbp'
-- WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sbp')\gexec

CREATE SCHEMA IF NOT EXISTS plugin_shelf;

DROP TABLE IF EXISTS "Shelf";
CREATE TABLE "Shelf" (
    id       bigserial CONSTRAINT shelf_pk PRIMARY KEY,
    code     varchar(255)
);

DROP TABLE IF EXISTS "ShelfBooks";
CREATE TABLE "ShelfBooks" (
    shelfId    bigint,
    bookId     bigint
);

DROP TABLE IF EXISTS "ShelfAuthor";
CREATE TABLE "ShelfAuthor" (
    shelfId    bigint,
    authorId   bigint
);