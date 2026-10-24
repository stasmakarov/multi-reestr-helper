# Развёртывание multi-reestr в Docker (Mac и Windows)

> Один `Dockerfile` и один `docker-compose.yml` на обе ОС. Путь `java -jar` на Windows не отменяется: [deploy-windows.md](deploy-windows.md).
>
> Согласованный план: [deploy-docker-plan.md](deploy-docker-plan.md). Учебный контейнер дампа 9.6: [LEGACY_DOCKER.md](LEGACY_DOCKER.md).

## Что поднимается

| Служба | Образ | Порт на хосте | Назначение |
|---|---|---|---|
| `app` | свой multi-stage образ (JDK 21 / Gradle → JRE 21 + jar) | `8080` | Jmix 3 |
| `postgres` | `postgres:16` | `5435` | основная БД Jmix (`multireestr`) |

HSQLDB в этом пути не используется. Пароль и JDBC URL MultiReestr в `.env` и compose **не** задаются.

## Запуск

Нужен Docker Desktop (Windows / Mac) или Docker Engine + Compose (Linux).

```bash
cp .env.example .env
# Заполнить MTR_SECRET_KEY (64 hex) и POSTGRES_PASSWORD.
# Пример ключа: openssl rand -hex 32

docker compose up --build
```

Фоном: `docker compose up --build -d`.

Открыть http://localhost:8080 , войти `admin` / `admin`, сразу сменить пароль администратора.

Остановка: `docker compose down`. Том `multireestr-pg16` сохраняет БД. Полный сброс: `docker compose down -v`.

## Подключение к MultiReestr из контейнера

Из `app` имя `localhost` — это сам контейнер. До PostgreSQL 9.6 на машине хоста — `host.docker.internal` (`extra_hosts: host.docker.internal:host-gateway` в compose, нужно в том числе на Linux).

Администрирование → **«Подключение к MultiReestr»**:

| Стенд | Хост | Порт | БД / роль |
|---|---|---|---|
| Windows, живая MultiReestr | `host.docker.internal` | `5432` (уточнить на месте) | `reestrdb` / `reestr_ro` |
| Mac, дамп в `reestr-pg96` | `host.docker.internal` | `5436` | `reestrdb` / `reestr_ro` |

Пароль вводится только в форме. «Сохранить и применить» шифрует его ключом `MTR_SECRET_KEY` и пишет в основную БД.

Роль `reestr_ro` на живой 9.6 — по [deploy-windows.md §4](deploy-windows.md#4-роль-reestr_ro-на-живой-бд-96), не через этот compose.

## Необязательный профиль `legacy-pg96` (учёба на Mac)

Ядро (`app` + `postgres`) не меняется.

```bash
docker compose --profile legacy-pg96 up --build
```

Поднимается `postgres:9.6` на хост-порту `5436` (имя контейнера `reestr-pg96`, как в [LEGACY_DOCKER.md](LEGACY_DOCKER.md)). Дамп восстановить скриптом `dev/legacy/legacy-db.sh` или шагами из того документа. Если контейнер уже запущен скриптом — профиль не включать.

## Только PostgreSQL 16 без контейнера приложения

По-прежнему: `dev/deploy/main-db-docker.ps1` и `java -jar` — [deploy-windows.md §3.3](deploy-windows.md#33-postgresql-16--docker-альтернатива). Не поднимайте одновременно тот скрипт и службу `postgres` этого compose: оба занимают `5435`.

## Переменные `.env`

Только то, что в `.env.example`: `MTR_SECRET_KEY`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`. Compose пробрасывает их в `MAIN_DATASOURCE_*` для `app`.
