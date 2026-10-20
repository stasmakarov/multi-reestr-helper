-- Read-only role for the MultiReestr PostgreSQL 9.6 database (reestrdb).
-- Run as a superuser or the owner of reestrdb. Change the password before use.
-- Do not grant INSERT, UPDATE, DELETE, TRUNCATE, CREATE, or other write privileges.

CREATE ROLE reestr_ro LOGIN PASSWORD 'CHANGE_ME';

GRANT CONNECT ON DATABASE reestrdb TO reestr_ro;

GRANT USAGE ON SCHEMA public TO reestr_ro;

GRANT SELECT ON ALL TABLES IN SCHEMA public TO reestr_ro;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO reestr_ro;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO reestr_ro;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON SEQUENCES TO reestr_ro;
