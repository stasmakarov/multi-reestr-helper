# Multi Reestr

This is a web application based on the [Jmix](https://www.jmix.io) framework.

## Getting Started

The newly created project requires Java 21 and uses the embedded HSQL database.

Use the following resources to learn more about Jmix:
* [Jmix Documentation](https://docs.jmix.io)
* [Online Demo Applications](https://www.jmix.io/live-demo)
* [Jmix AI Assistant](https://ai-assistant.jmix.io) (also available in the **Jmix AI** tool window of IntelliJ IDEA)

## Development

- [Setup](https://docs.jmix.io/jmix/setup.html) your development environment.
- [Open](https://docs.jmix.io/jmix/studio/project.html#opening-existing-project) the project in the IDE.
- If you want to use AI agents to develop the application, check out the [Jmix AI Agent Guidelines](https://github.com/jmix-framework/jmix-agent-guidelines) repository.

## Running

To start the application, use the **Multi Reestr Jmix Application** run configuration in your IDE, or run the following command in the project root directory:

```bash
./gradlew bootRun
```

The application will be available at <http://localhost:8080>.

The default user credentials are:
* Login: `admin`
* Password: `admin`

**WARNING**: Change admin password and remove `ui.login.defaultUsername` and `ui.login.defaultPassword` application properties when deploying the application to production.

## Подключение к MultiReestr

Сценарии использования (поиск, проверка, реестр, правила) — в [docs/USAGE.md](docs/USAGE.md).

Приложение читает извещения и связанные данные из унаследованной базы MultiReestr (`reestrdb` на PostgreSQL 9.6) через отдельный JDBC DataSource `legacy`. Это не хранилище Jmix: без JPA и Liquibase, только `NamedParameterJdbcTemplate` с квалификатором `legacyJdbc`.

Основная база приложения (Jmix store `main`) в продуктиве — **отдельный PostgreSQL 16+** на другом порту (например `localhost:5433`, БД `multireestr`). В разработке main остаётся на HSQLDB. Jmix 3 не работает с PostgreSQL 9.6, поэтому main нельзя размещать на сервере MultiReestr.

Параметры `legacy.datasource.*` задаются в `application.properties`. В разработке: `jdbc:postgresql://localhost:5436/reestrdb`, пользователь `reestr_ro` (пароль — профиль `dev` или `LEGACY_DATASOURCE_PASSWORD`). В продуктиве URL — `jdbc:postgresql://localhost:5432/reestrdb`.

Параметры драйвера `readOnly=true` и `readOnlyMode=always` задаёт приложение при сборке пула Hikari. В URL их писать не нужно.

Если URL пустой или БД недоступна, приложение всё равно стартует: в лог пишется предупреждение, экраны main работают.

### Роль `reestr_ro`

На сервере MultiReestr выполните скрипт [docs/sql/legacy_readonly_user.sql](docs/sql/legacy_readonly_user.sql) от суперпользователя или владельца `reestrdb`. Скрипт создаёт роль `reestr_ro` (LOGIN) с `CONNECT` и `SELECT` на таблицы схемы `public` и `ALTER DEFAULT PRIVILEGES` для будущих таблиц. Прав на запись нет. Смените пароль в скрипте до выполнения.

### Тесты

Нужен Docker (Testcontainers поднимает `postgres:9.6`). Без Docker тесты с контейнером падают. Пропуск: `./gradlew test -PallowNoDocker`. Локальный PostgreSQL не требуется.

```bash
./gradlew test
./gradlew build
```

Проверяется подключение и `version()` с «9.6», запрет `INSERT`/`CREATE TABLE` через `legacyJdbc`, и старт контекста Jmix без legacy DataSource. Параметры legacy-тестов задаёт Testcontainers, а не локальная БД.