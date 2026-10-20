-- Read-only role for the MultiReestr PostgreSQL 9.6 database (reestrdb).
-- Run with psql as a superuser or the owner of reestrdb, from this directory or with -f:
--   psql -h <host> -U postgres -d reestrdb -f docs/sql/legacy_readonly_user.sql
-- Change the password before use.
-- Do not grant INSERT, UPDATE, DELETE, TRUNCATE, CREATE, or other write privileges.
-- Do not use GRANT SELECT ON ALL TABLES or ALTER DEFAULT PRIVILEGES: the role sees only
-- the tables listed in dev/legacy/grant-readonly.sql (the ones read by src/main/resources/sql/legacy/**).

CREATE ROLE reestr_ro LOGIN PASSWORD 'CHANGE_ME';

GRANT CONNECT ON DATABASE reestrdb TO reestr_ro;

-- USAGE on schema public and SELECT on the listed tables; same script as the dev stand uses.
\ir ../../dev/legacy/grant-readonly.sql
