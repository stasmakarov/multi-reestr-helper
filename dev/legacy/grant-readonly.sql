-- Права роли reestr_ro на legacy-БД MultiReestr: только чтение и только то, что читает приложение.
-- Выполнять под postgres (или владельцем reestrdb) после CREATE ROLE и GRANT CONNECT:
--   dev/legacy/grant-readonly.sh                  -- локальный стенд reestr-pg96
--   psql ... -f dev/legacy/grant-readonly.sql     -- любая другая база
--
-- Правило: SELECT выдаётся только на таблицы, которые читают SQL-файлы src/main/resources/sql/legacy/**.
-- Никаких GRANT ... ON ALL TABLES и ALTER DEFAULT PRIVILEGES: остальные таблицы дампа
-- (в том числе userinfo с учётными данными) роли не видны.
-- Новый запрос читает ещё одну таблицу → добавьте её в список ниже и выполните скрипт заново.
-- planzakuppackets: GRANT ниже отдельным блоком — в тестовом дампе таблицы нет.
-- Скрипт идемпотентен: сначала снимает все табличные права роли, затем выдаёт список заново.

REVOKE ALL ON ALL TABLES IN SCHEMA public FROM reestr_ro;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM reestr_ro;

GRANT USAGE ON SCHEMA public TO reestr_ro;

GRANT SELECT ON TABLE
    notices,
    owners,
    spdogovortypes,
    noticeprocedureinfo,
    noticelots,
    noticecustomerrequirements,
    noticepurchaseprefs,
    sppurchasepreferences,
    noticeextdocs,
    noticespec,
    noticektruharacts,
    noticektruharactvalues,
    noticefinances,
    protocols,
    protocol_types,
    reestr,
    reestrspec,
    reestrprocedures,
    reestrprocexecutions
TO reestr_ro;

-- planzakuppackets есть на живой MultiReestr; в тестовом дампе таблицы нет.
DO $$
BEGIN
    IF to_regclass('public.planzakuppackets') IS NOT NULL THEN
        EXECUTE 'GRANT SELECT ON TABLE planzakuppackets TO reestr_ro';
    END IF;
END
$$;
