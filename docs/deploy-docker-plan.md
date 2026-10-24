# План: Docker-развёртывание multi-reestr (Mac и Windows)

> **Статус: реализовано.** Инструкция по запуску: [deploy-docker.md](deploy-docker.md). Файлы: `Dockerfile`, `docker-compose.yml`, `.env.example` в корне репозитория.
>
> Уже действующий путь `java -jar` (и при желании только PostgreSQL 16 в контейнере) остаётся: [deploy-windows.md](deploy-windows.md), скрипт [dev/deploy/main-db-docker.ps1](../dev/deploy/main-db-docker.ps1). Docker приложения — **дополнительный** путь, не замена.
>
> Учебный контейнер дампа MultiReestr: [LEGACY_DOCKER.md](LEGACY_DOCKER.md). Модель двух БД: [model/OVERVIEW.md §8](model/OVERVIEW.md#8-развёртывание-и-базы-данных). Экран настроек: [UI.md, «Подключение к MultiReestr»](UI.md#35-администрирование).

## Содержание

1. [Цели](#1-цели)
2. [Состав стека](#2-состав-стека)
3. [Схема: main PostgreSQL 16 и legacy 9.6](#3-схема-main-postgresql-16-и-legacy-96)
4. [Переменные окружения и секреты](#4-переменные-окружения-и-секреты)
5. [Настройки legacy в интерфейсе](#5-настройки-legacy-в-интерфейсе)
6. [Mac и Windows: что отличается](#6-mac-и-windows-что-отличается)
7. [Реализация](#7-реализация)
8. [Критерии готовности](#8-критерии-готовности)
9. [Вне объёма](#9-вне-объёма)
10. [Согласовать с текущими документами](#10-согласовать-с-текущими-документами)

## 1. Цели

- Один и тот же образ приложения и один и тот же compose на Mac и на Windows.
- В контейнерах **нет HSQLDB**. HSQLDB остаётся только для локальной разработки в Studio / `bootRun` на Mac.
- Основная БД Jmix в Docker — всегда **PostgreSQL 16** (как в продуктиве на Windows: порт хоста `5435`, БД `multireestr`).
- Живую БД MultiReestr (PostgreSQL 9.6) **не** упаковываем в compose как продуктивный сервис. Приложение ходит к ней только чтением через второй JDBC-источник `legacy` (без JPA и Liquibase).
- Пароль и JDBC URL legacy **не** кладём в `.env` и не в секреты compose. Хост, порт, пользователь и пароль задаёт администратор на экране «Подключение к MultiReestr»; пароль хранится зашифрованным в основной БД.
- Из контейнера `app` до БД на хосте — имя `host.docker.internal` (Docker Desktop на Windows и на Mac).

## 2. Состав стека

Один файл compose, ядро одинаково везде:

| Служба | Образ | Назначение |
|---|---|---|
| `app` | **один** свой образ (multi-stage Dockerfile от production-jar) | Jmix 3 / Spring Boot, порт `8080` |
| `postgres` | официальный `postgres:16` | основная БД Jmix (`multireestr`), публикация на хост **`5435`** — как [deploy-windows.md §3](deploy-windows.md#3-подготовка-windows-jdk-21-и-postgresql-16-порт-5435) и `main-db-docker.ps1` |

Имена роли и БД — те же, что уже в скриптах: пользователь `multireestr`, БД `multireestr`, контейнер/том в духе `multireestr-pg16` (точное имя службы в compose — на шаге реализации, смысл не менять).

Необязательный профиль compose (для учеников на Mac, **не** для живого продукта): служба вроде `reestr-pg96` по [LEGACY_DOCKER.md](LEGACY_DOCKER.md) / `dev/legacy/legacy-db.sh` (дамп `reestrdb`, порт хоста **`5436`**). Профиль не меняет службы `app` и `postgres` и не делает «mac-compose» отдельно от Windows.

HSQLDB в образе и в compose не запускаем.

## 3. Схема: main PostgreSQL 16 и legacy 9.6

```mermaid
flowchart LR
    B["Браузер :8080"] --> APP
    subgraph COMPOSE["Один compose: Mac и Windows"]
        APP["app<br/>один образ"]
        PG16[("postgres:16<br/>БД multireestr<br/>хост :5435")]
    end
    APP -->|"main: JPA + Liquibase"| PG16
    APP -->|"legacy JDBC, только чтение"| LEG
    LEG[("PostgreSQL 9.6 на хосте<br/>не служба compose")]
```

| Хранилище | Где в Docker-пути | Как приложение ходит | Что пишет |
|---|---|---|---|
| `main` | служба `postgres` в том же compose | Jmix data store: JPA, Liquibase | пользователи, календарь, правила, журнал, реестр нарушений, ручные данные |
| `legacy` | **вне** ядра compose: живая 9.6 на Windows или учебный `reestr-pg96` на Mac | второй `DataSource` + `legacyJdbc`, обычный JDBC | ничего |

Из контейнера `app` имя `localhost` — это сам контейнер, не хост. До PostgreSQL 9.6 на машине:

| Стенд | Хост в форме UI | Порт | БД / роль |
|---|---|---|---|
| Windows, живая MultiReestr | `host.docker.internal` | `5432` (уточнить на месте, как в [deploy-windows.md](deploy-windows.md)) | `reestrdb` / `reestr_ro` |
| Mac, тестовый набор в `reestr-pg96` | `host.docker.internal` | `5436` | `reestrdb` / `reestr_ro` |

`reestr-pg96` можно поднять скриптом `legacy-db.sh` или профилем compose `legacy-pg96`. В обоих случаях `app` видит его через `host.docker.internal:5436`, если порт проброшен на хост. Для единообразия с Windows основной путь — **`host.docker.internal` + порт на хосте**.

Роль `reestr_ro` на живой БД по-прежнему создаётся вне Docker приложения: [deploy-windows.md §4](deploy-windows.md#4-роль-reestr_ro-на-живой-бд-96), SQL из `dev/legacy/grant-readonly.sql`.

## 4. Переменные окружения и секреты

В `.env` (или `environment` / `env_file` compose) **только**:

| Переменная | Зачем |
|---|---|
| `MTR_SECRET_KEY` | ключ шифрования пароля legacy, который лежит в основной БД (`Encryptors.delux`, как в уроке 1) |
| учётные данные PostgreSQL 16 | то, без чего compose не поднимет `postgres` и `app` не подключится к **своей** БД: как минимум пароль роли `multireestr`; при необходимости имя БД и пользователя (`POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` и зеркало `MAIN_DATASOURCE_*`) |

В `.env` и в секретах compose **не** держим:

- пароль `reestr_ro` и любой другой пароль legacy;
- `LEGACY_DATASOURCE_URL` / JDBC URL legacy;
- хост, порт, имя БД MultiReestr.

Запасной путь `legacy.datasource.*` из свойств (как в jar-установке) для Docker-пути **не используем как постоянный источник**: после старта `app` настройки вводятся в UI. Если для самого первого старта без UI понадобится временный URL — согласовать отдельно; в согласованном плане пароль legacy в файл окружения не кладётся.

`MTR_SECRET_KEY` — 64 hex-символа (как генерирует `set-env.ps1`). Потеряли ключ — пароль в основной БД не расшифровать: задать новый ключ и ввести пароль `reestr_ro` на экране заново.

Файл `.env` в git не коммитим (шаблон `.env.example` без секретов).

## 5. Настройки legacy в интерфейсе

После входа `admin` (и смены пароля): Администрирование → **«Подключение к MultiReestr»**.

1. Включить подключение.
2. Указать хост, порт, БД, логин, пароль — см. таблицу в §3. Не `localhost`, если приложение в контейнере.
3. «Проверить подключение» — проба из формы, рабочий пул не меняется.
4. «Сохранить и применить» — пароль шифруется `MTR_SECRET_KEY`, пишется в `MTR_LEGACY_CONNECTION_SETTINGS` в PostgreSQL 16, пул `legacy` пересобирается без перезапуска контейнера.

Порядок источников (уже в модели): включённая запись в основной БД сильнее свойств и переменных окружения. Менять порт 9.6 или пароль `reestr_ro` — снова этот экран, без правки compose.

## 6. Mac и Windows: что отличается

**Не отличается:** образ `app`, файл compose, службы `app` + `postgres`, порт main `5435`, набор переменных в `.env`.

**Отличается только цель JDBC legacy** (куда смотрит форма в UI):

- Windows: живая MultiReestr на хосте, обычно `host.docker.internal:5432`.
- Mac (учёба): дамп в `reestr-pg96`, `host.docker.internal:5436`.

Путь `java -jar` на Windows не отменяется. `main-db-docker.ps1` остаётся способом поднять **только** PostgreSQL 16, если приложение по-прежнему запускают как jar. Полный compose (`app` + `postgres`) — соседний вариант.

Ограничение Docker Desktop на Windows то же, что уже записано в [deploy-windows.md §3.3](deploy-windows.md#33-postgresql-16--docker-альтернатива): Desktop поднимается после входа пользователя.

## 7. Реализация

Историю уроков не переписывать.

1. **Dockerfile** (multi-stage): сборка production-jar (`bootJar` + `-Pvaadin.productionMode=true`, как `dev/deploy/build-jar.sh`) → runtime-образ с JRE 21. Main URL в compose указывает на PostgreSQL; HSQLDB в этом пути не используется.
2. **compose** в корне: службы `app` и `postgres:16`, том данных, порты `8080` и `5435`, `depends_on` + healthcheck Postgres. Один файл для обеих ОС. `extra_hosts: host.docker.internal:host-gateway` у `app`.
3. **`.env.example`**: только `MTR_SECRET_KEY` и поля основной БД. Без legacy URL и пароля.
4. Необязательный профиль `legacy-pg96` — не ломает ядро.
5. Документы: [deploy-docker.md](deploy-docker.md), ссылки из [deploy-windows.md](deploy-windows.md), [README.md](../README.md) (порт main **`5435`**), [LEGACY_DOCKER.md](LEGACY_DOCKER.md).
6. Проверка на Docker Desktop: Windows — `app` + `postgres`, UI на живую `:5432`; Mac — тот же compose, UI на `:5436`.

Код приложения (второй DataSource, шифрование, экран) не менялся.

## 8. Критерии готовности

- [x] Один образ и один compose поднимают `app` + PostgreSQL 16 на Mac и на Windows без правок файла под ОС.
- [x] В контейнерах нет HSQLDB как основной БД; `MAIN_DATASOURCE_URL` указывает на `postgres:16`, таблицы `MTR_*` — в `multireestr` на `:5435`.
- [x] Живая MultiReestr не описана как служба ядра compose и не требует своего контейнера в продуктиве.
- [x] В `.env.example` нет пароля и URL legacy; задаются `MTR_SECRET_KEY` и пароль main-Postgres.
- [x] Инструкция UI: Windows — `host.docker.internal:5432`, Mac — `host.docker.internal:5436`.
- [x] Инструкция `java -jar` в [deploy-windows.md](deploy-windows.md) сохранена.
- [x] Теги уроков `lesson-01`…`lesson-10` не переписаны.

Поиск извещения с живой/учебной 9.6 проверяется на машине с Docker Desktop и настроенным UI.

## 9. Вне объёма

- Docker-образ или служба compose для **живой** БД MultiReestr (PostgreSQL 9.6 в продуктиве).
- Пароль legacy, JDBC URL legacy, логин `reestr_ro` в `.env` / секретах compose / образе.
- Замена HSQLDB в Studio / `bootRun` на Mac.
- Служба Windows, Kubernetes, отдельный compose «только для Mac».
- Перенос `reestr-pg96` с дампом как обязательной части продуктивного стека.
- Правки истории уроков и тегов.

## 10. Согласовать с текущими документами

| Где | Что | Как в этом плане |
|---|---|---|
| [README.md](../README.md) | продуктивный main | **`5435`**, как [deploy-windows.md](deploy-windows.md) и `main-db-docker.ps1` |
| [dev/deploy/application-windows.properties.example](../dev/deploy/application-windows.properties.example) | комментарий про порт в jar | в `application.properties` сейчас `:5436` (Mac `reestr-pg96`); на Windows jar-путь по-прежнему переопределяет legacy на `:5432` |
| [LEGACY_DOCKER.md](LEGACY_DOCKER.md) §4 | имена `LEGACY_DB_URL` / `LEGACY_DB_USER` / `LEGACY_DB_PASSWORD` | в приложении — `legacy.datasource.*` / `LEGACY_DATASOURCE_*`; для Docker-пути постоянный источник — UI, не эти переменные |
| `main-db-docker.ps1` vs compose | скрипт только Postgres 16 | compose добавляет `app`; скрипт остаётся для jar-пути |

Пометки **проверить** из [deploy-windows.md](deploy-windows.md) (поведение Desktop после входа, `pg_hba` только localhost) относятся и к compose.
