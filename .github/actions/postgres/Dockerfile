FROM postgres:12

COPY init-db /docker-entrypoint-initdb.d
COPY init-db/postgresql.conf /etc/postgresql/postgresql.conf