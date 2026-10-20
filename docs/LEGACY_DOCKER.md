# Legacy-БД `reestrdb` в Docker (PostgreSQL 9.6)

Файл дампа: `reestrdb.dump` (custom-формат `pg_dump -Fc -O -x`, без владельцев и прав, ~4.5 МБ).
SHA-256: `25f12e8fd12bce77c02cecd72d3b7467fb7b94098785397d0da7bbcb2986ffc3`

## 1. Запуск контейнера

```bash
docker run -d --name reestr-pg96 \
  -e POSTGRES_PASSWORD=postgres \
  -p 5435:5432 \
  -v reestr-pg96:/var/lib/postgresql/data \
  postgres:9.6
```

Apple Silicon: тег `postgres:9.6` на Docker Hub содержит образ `arm64/v8`, поэтому флаг
`--platform` обычно не нужен. Если `docker pull` всё же пожалуется на платформу,
добавьте `--platform linux/amd64` (будет работать через эмуляцию Rosetta, медленнее).

На машине автора порты 5432 (локальный Postgres), 5433 и 5434 заняты, поэтому контейнер
пробрасывается на **5435** (`-p 5435:5432`), и в `legacy.datasource.url` указан `localhost:5435`.
Если выбираете другой порт, поправьте URL в `application.properties`.

Дождитесь готовности:

```bash
until docker exec reestr-pg96 pg_isready -U postgres; do sleep 1; done
```

## 2. Создание БД и восстановление дампа

```bash
docker exec reestr-pg96 createdb -U postgres reestrdb
docker cp reestrdb.dump reestr-pg96:/tmp/reestrdb.dump
docker exec reestr-pg96 pg_restore -U postgres -O -x --no-owner -d reestrdb /tmp/reestrdb.dump
docker exec reestr-pg96 rm /tmp/reestrdb.dump
```

`pg_restore` должен завершиться без ошибок. (Дамп содержит функции `uuid_generate_*`,
ссылающиеся на `$libdir/uuid-ossp`; в официальном образе эта библиотека есть.)

## 3. Read-only пользователь `reestr_ro`

```bash
docker exec -i reestr-pg96 psql -U postgres -d reestrdb <<'SQL'
CREATE ROLE reestr_ro LOGIN PASSWORD 'reestr_ro';
GRANT CONNECT ON DATABASE reestrdb TO reestr_ro;
GRANT USAGE ON SCHEMA public TO reestr_ro;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO reestr_ro;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO reestr_ro;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO reestr_ro;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON SEQUENCES TO reestr_ro;
SQL
```

(Default privileges действуют для объектов, которые в будущем создаст `postgres`.)

## 4. Переменные окружения приложения

```
LEGACY_DB_URL=jdbc:postgresql://localhost:5435/reestrdb
LEGACY_DB_USER=reestr_ro
LEGACY_DB_PASSWORD=reestr_ro
```

## 5. Проверка

```bash
docker exec -e PGPASSWORD=reestr_ro reestr-pg96 psql -h localhost -U reestr_ro -d reestrdb -c "
SELECT (SELECT count(*) FROM pg_tables WHERE schemaname='public') AS tables,   -- ожидается 335
       (SELECT count(*) FROM notices)      AS notices,      -- 40
       (SELECT count(*) FROM reestr)       AS reestr,       -- 28
       (SELECT count(*) FROM okdp)         AS okdp,         -- 44482
       (SELECT count(*) FROM spdrugs)      AS spdrugs,      -- 17566
       (SELECT count(*) FROM honestsign)   AS honestsign;   -- 2097
"
```

Проверка, что пользователь действительно только на чтение (должна быть ошибка `permission denied`):

```bash
docker exec -e PGPASSWORD=reestr_ro reestr-pg96 psql -h localhost -U reestr_ro -d reestrdb -c "DELETE FROM notices WHERE false;"
```

## Сброс

```bash
docker rm -f reestr-pg96 && docker volume rm reestr-pg96
```

## Примечание о происхождении дампа

Исходный кластер был создан 32-битной сборкой PostgreSQL 9.6, поэтому сохранённые в каталоге
деревья выражений (значения по умолчанию колонок, правила системных представлений) не читались
64-битной сборкой, и `pg_dump` падал с `expected "]" to end datum`. Деревья были сконвертированы
в копии кластера (исходные файлы не изменялись), после чего дамп снят и проверен восстановлением
в чистый кластер 9.6: 335 таблиц, количество строк во всех таблицах совпадает.
