# Развёртывание multi-reestr на Windows 10/11

> Документ-инструкция без кода приложения. Разработка ведётся на Mac, приложение работает на Windows-машине пользователя рядом с живой БД MultiReestr. Модель данных — [model/OVERVIEW.md §8](model/OVERVIEW.md#8-развёртывание-и-базы-данных), экран настроек — [UI.md, «Подключение к MultiReestr»](UI.md#35-администрирование), урок — 1 ([LESSONS.md](LESSONS.md), [lessons/lesson-01-legacy.md §11–12](lessons/lesson-01-legacy.md#11-подключение-из-интерфейса-администратора)).
> Пометка **проверить** — то, что нужно подтвердить на Jmix 3.0.3 или на машине пользователя. **Требует кода (урок 1, §11)** — шаг заработает только после реализации [LegacyConnectionSettings](model/entities/LegacyConnectionSettings.md).

## Содержание

1. [Схема установки](#1-схема-установки)
2. [Сборка jar на Mac](#2-сборка-jar-на-mac)
3. [Подготовка Windows: JDK 21 и PostgreSQL 16 (порт 5435)](#3-подготовка-windows-jdk-21-и-postgresql-16-порт-5435)
4. [Роль reestr_ro на живой БД 9.6](#4-роль-reestr_ro-на-живой-бд-96)
5. [Настройки и переменные окружения](#5-настройки-и-переменные-окружения)
6. [Первый запуск](#6-первый-запуск)
7. [Подключение к MultiReestr из интерфейса](#7-подключение-к-multireestr-из-интерфейса)
8. [Обновление версии](#8-обновление-версии)
9. [Скрипты dev/deploy](#9-скрипты-devdeploy)
10. [Неполадки](#10-неполадки)

## 1. Схема установки

| Компонент | Где | Порт | БД / роль |
|---|---|---|---|
| MultiReestr (существующая система) | Windows-машина пользователя | — | — |
| PostgreSQL 9.6 MultiReestr (живая БД, **не трогаем**, кроме создания `reestr_ro`) | та же машина | `5432` (уточнить на месте) | `reestrdb`, роль `reestr_ro` только с `SELECT` |
| PostgreSQL 16 для Jmix (main) | та же машина, отдельная установка | `5435` | `multireestr`, владелец `multireestr` |
| multi-reestr (`java -jar`, без службы Windows) | папка `C:\multi-reestr` | `8080` | — |

Почему отдельный PostgreSQL 16: Jmix 3 не поддерживает PostgreSQL 9.6, а размещать свои таблицы в БД MultiReestr нельзя. Порт `5435` выбран, чтобы не пересечься с 9.6 на `5432`.

> На Mac dev-контейнер `reestr-pg96` (legacy, см. `dev/legacy/legacy-db.sh`) слушает **5436**, и в `application.properties` по умолчанию `legacy.datasource.url=jdbc:postgresql://localhost:5436/reestrdb`. На Windows на `5435` стоит PostgreSQL 16 для Jmix. Порты больше не пересекаются, но **legacy URL на Windows всё равно переопределяется** на живую 9.6 (`:5432`, раздел 5).

```
C:\multi-reestr\
  multi-reestr.jar
  config\application.properties     ← из application-windows.properties.example
  sql\grant-readonly.sql             ← копия dev/legacy/grant-readonly.sql
  *.ps1                              ← скрипты из dev/deploy
  logs\                              ← создаётся при запуске
  .jmix\                             ← рабочий каталог Jmix (создаётся сам; файлы localfs)
```

## 2. Сборка jar на Mac

Требования: JDK 21, доступ к `nexus.jmix.io` (зависимости). Node.js отдельно ставить не нужно — плагин Vaadin скачает его сам при production-сборке.

```bash
dev/deploy/build-jar.sh
# то же вручную:
./gradlew clean bootJar -Pvaadin.productionMode=true
```

Скрипт собирает jar в production-режиме Vaadin (фронтенд упакован в jar) и раскладывает папку `build/deploy/multi-reestr/` (jar, `*.ps1`, `config\…example`, `sql\grant-readonly.sql`). Эту папку целиком копируем на Windows (флешка, общий диск) в `C:\multi-reestr`.

Проверить перед переносом:
- В `build.gradle` стоит `vaadin { productionMode = false }` (локальная разработка) и `developmentOnly 'com.vaadin:vaadin-dev'`. Флаг `-Pvaadin.productionMode=true` в `build-jar.sh` собирает фронтенд в jar. Проверка: `jar tf build/libs/*.jar | grep vaadin-dev` не должен ничего показать.
- Jar с HSQLDB и PostgreSQL-драйвером: оба уже в `runtimeOnly`.
- Версия и коммит попадают в jar (`generateGitProperties`) — их показывает экран «О программе». Собирать из чистого рабочего дерева на нужном теге.

## 3. Подготовка Windows: JDK 21 и PostgreSQL 16 (порт 5435)

### 3.1. JDK 21

1. Установить Eclipse Temurin 21 (MSI с adoptium.net) или Liberica 21, отметив «Set JAVA_HOME» и «Add to PATH».
2. Проверить в новом окне PowerShell: `java -version` → `21.x`.

Отдельный JDK не мешает MultiReestr: если там своя Java, `start-app.ps1 -JavaHome …` запускает приложение конкретным JDK.

### 3.2. PostgreSQL 16 — установщик (рекомендуется)

1. Установщик EDB PostgreSQL 16 для Windows. В мастере: каталог данных по умолчанию, пароль `postgres`, **порт `5435`**, локаль по умолчанию. Stack Builder не нужен.
   Без диалогов: `postgresql-16.x-windows-x64.exe --mode unattended --serverport 5435 --superpassword <пароль>` (**проверить** набор ключей на текущей версии установщика).
2. Служба `postgresql-x64-16` запускается автоматически вместе с Windows — это и есть главный плюс перед Docker.
3. Создать роль и БД: `.\init-main-db.ps1` (спросит пароль новой роли `multireestr`, затем пароль `postgres`). Таблицы создаст Liquibase при первом старте приложения.

По умолчанию `pg_hba.conf` пускает только с `localhost` — этого достаточно. Открывать `5435` в брандмауэре не нужно.

### 3.3. PostgreSQL 16 — Docker (альтернатива)

Если на машине уже есть Docker Desktop: `.\main-db-docker.ps1` поднимает `postgres:16` с `--restart unless-stopped`, порт `127.0.0.1:5435`. Минус: Docker Desktop стартует только после входа пользователя в Windows, поэтому приложение нельзя запускать раньше, чем поднимется контейнер. Для живой БД MultiReestr Docker не используем.

## 4. Роль reestr_ro на живой БД 9.6

Права роли описаны в `dev/legacy/grant-readonly.sql`: `SELECT` только на таблицы, которые читают запросы `src/main/resources/sql/legacy/**`, без `GRANT … ON ALL TABLES` и без `ALTER DEFAULT PRIVILEGES`. Скрипт идемпотентен: сначала снимает права, потом выдаёт список заново.

Перед работой с живой БД — свежая резервная копия MultiReestr средствами самой системы или `pg_dump`. Скрипт меняет только права роли `reestr_ro`, но это всё равно правка продуктивной БД.

**Вариант A — скрипт PowerShell (рекомендуется):**

```powershell
cd C:\multi-reestr
.\create-legacy-ro.ps1 -Port 5432 -Database reestrdb
```

Скрипт использует `psql.exe` из PostgreSQL 16 (клиент новее сервера — это нормально), создаёт роль `reestr_ro` с введённым паролем (или меняет пароль, если роль уже есть), выдаёт `CONNECT` и выполняет `sql\grant-readonly.sql`, затем проверяет `select count(*) from notices` под `reestr_ro`.

**Вариант B — вручную, тем же SQL, что и в репо:**

```powershell
$env:PGCLIENTENCODING = "UTF8"
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -h localhost -p 5432 -U postgres -d reestrdb -f docs\sql\legacy_readonly_user.sql
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -h localhost -p 5432 -U postgres -d reestrdb -c "ALTER ROLE reestr_ro PASSWORD '<пароль>'"
```

`legacy_readonly_user.sql` подключает `dev/legacy/grant-readonly.sql` через `\ir` (путь относительно файла), поэтому запускать из клона репо, а не из папки установки. Пароль в нём — заглушка `CHANGE_ME`, её нужно сменить второй командой.

`dev/legacy/grant-readonly.sh` на Windows не нужен: это обёртка над тем же SQL для dev-контейнера.

Порт и имя БД MultiReestr уточнить на месте: `Get-Service postgresql*` и `postgresql.conf` установки 9.6 (`port = …`). Если MultiReestr ходит в БД под `postgres`, мы этого не меняем.

## 5. Настройки и переменные окружения

Источники настроек (сильнее — выше):

1. Переменные окружения Windows (пользователя, от имени которого запускается jar).
2. `C:\multi-reestr\config\application.properties` — Spring Boot сам читает `config\` рядом с текущей папкой.
3. `application.properties` внутри jar (значения для разработки на Mac).

Для legacy, кроме этого, после урока 1 (§11) есть запись [LegacyConnectionSettings](model/entities/LegacyConnectionSettings.md) в БД Jmix: если она есть и включена, она сильнее пунктов 1–3 (**требует кода (урок 1, §11)**). Пункты 1–3 остаются запасным вариантом для первого старта.

| Свойство | Переменная окружения | Значение на Windows | Где хранить |
|---|---|---|---|
| `main.datasource.url` | `MAIN_DATASOURCE_URL` | `jdbc:postgresql://localhost:5435/multireestr` | config-файл |
| `main.datasource.username` | `MAIN_DATASOURCE_USERNAME` | `multireestr` | config-файл |
| `main.datasource.password` | `MAIN_DATASOURCE_PASSWORD` | пароль роли `multireestr` | переменная окружения |
| `legacy.datasource.url` | `LEGACY_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/reestrdb` — **обязательно**, иначе возьмётся `5436` из jar | config-файл |
| `legacy.datasource.username` | `LEGACY_DATASOURCE_USERNAME` | `reestr_ro` | config-файл |
| `legacy.datasource.password` | `LEGACY_DATASOURCE_PASSWORD` | пароль `reestr_ro` | переменная окружения (только до первой настройки в UI) |
| `mtr.secret-key` | `MTR_SECRET_KEY` | 64 hex-символа, генерирует `set-env.ps1` | переменная окружения + копия в надёжном месте |
| `server.port` | `SERVER_PORT` | `8080` или свободный | config-файл |
| `ui.login.defaultUsername` / `defaultPassword` | — | пусто (в jar стоят `admin`/`admin`) | config-файл |

Шаги:

```powershell
cd C:\multi-reestr
Copy-Item .\config\application-windows.properties.example .\config\application.properties
notepad .\config\application.properties     # проверить порты и логины, пароли оставить пустыми
.\set-env.ps1                               # пароли и MTR_SECRET_KEY → переменные пользователя
```

Права на папку: доступ к `C:\multi-reestr` — только у учётной записи, под которой запускается приложение, и у администраторов (`config\` и `.jmix\` содержат настройки и файлы).

Тип СУБД main Jmix определяет по URL; отдельное свойство вроде `main.datasource.studio.*` для jar не нужно — **проверить** на первом запуске (в логе Liquibase должны создаваться таблицы `MTR_*` в PostgreSQL).

## 6. Первый запуск

```powershell
cd C:\multi-reestr
.\start-app.ps1
```

1. В логе (`logs\multi-reestr.log` и консоль): Liquibase создаёт таблицы в `multireestr` на `:5435`, затем `Connected to legacy MultiReestr database: PostgreSQL 9.6…`. Если legacy недоступна — предупреждение `…not available… Application continues without it`, приложение продолжает работать.
2. Открыть `http://localhost:8080`, войти `admin` / `admin` и **сразу сменить пароль** admin (Администрирование → Пользователи).
3. Администрирование → «Подключение к MultiReestr» → «Проверить подключение»: статус «Подключено», пользователь `reestr_ro`, read-only «да».
4. Поиск извещения по известному номеру — карточка открывается, Excel выгружается.

Окно PowerShell должно оставаться открытым: закрыли окно — остановили приложение (служба Windows по требованию не используется). Для автозапуска можно положить ярлык на `powershell.exe -File C:\multi-reestr\start-app.ps1` в `shell:startup` (**предложение**); при Docker-варианте PostgreSQL 16 — с задержкой.

## 7. Подключение к MultiReestr из интерфейса

**Требует кода (урок 1, §11).** Экран «Администрирование → Подключение к MultiReestr» ([UI.md](UI.md#35-администрирование)):

1. Поля: включено, хост (`localhost`), порт (`5432`), БД (`reestrdb`), логин (`reestr_ro`), пароль. При первом открытии подставляются значения из `legacy.datasource.*`.
2. «Проверить подключение» — пробное соединение с параметрами из формы; рабочий пул не меняется.
3. «Сохранить и применить» — пароль шифруется ключом `MTR_SECRET_KEY` и сохраняется в `MTR_LEGACY_CONNECTION_SETTINGS`, пул `legacy` пересобирается без перезапуска; поиск и карточка извещения сразу ходят в новую БД.
4. После этого `LEGACY_DATASOURCE_PASSWORD` можно удалить из переменных окружения: при следующем старте действует запись в БД.

Сменился пароль `reestr_ro` или порт 9.6 — меняем на этом экране, без правки файлов. Потерян `MTR_SECRET_KEY` — задать новый (`set-env.ps1` после удаления старой переменной), перезапустить и ввести пароль `reestr_ro` заново.

## 8. Обновление версии

1. На Mac: `git checkout <тег>` → `dev/deploy/build-jar.sh`.
2. На Windows: остановить приложение (Ctrl+C в окне), резервная копия БД Jmix: `pg_dump -h localhost -p 5435 -U multireestr -Fc multireestr > backup\multireestr-<дата>.dump`.
3. Заменить `multi-reestr.jar` (и при необходимости `*.ps1`, `sql\grant-readonly.sql`). `config\application.properties` не перезаписывать.
4. Если в новой версии изменился `grant-readonly.sql` (новые таблицы в запросах) — повторить `.\create-legacy-ro.ps1` (пароль можно ввести прежний).
5. `.\start-app.ps1` — Liquibase применит новые ченджсеты.

## 9. Скрипты dev/deploy

| Файл | Где запускать | Что делает |
|---|---|---|
| `build-jar.sh` | Mac | production-сборка и папка `build/deploy/multi-reestr/` для переноса |
| `application-windows.properties.example` | — | шаблон `config\application.properties` |
| `init-main-db.ps1` | Windows | роль и БД `multireestr` в PostgreSQL 16 (`:5435`) |
| `main-db-docker.ps1` | Windows | альтернатива: `postgres:16` в Docker на `127.0.0.1:5435` |
| `create-legacy-ro.ps1` | Windows | `reestr_ro` на живой 9.6 + `grant-readonly.sql`, повторный запуск безопасен |
| `set-env.ps1` | Windows | пароли и `MTR_SECRET_KEY` в переменные пользователя |
| `start-app.ps1` | Windows | `java -jar` из папки установки, UTF-8, часовой пояс Москвы |

Скрипты `*.ps1` сохранены в UTF-8 **с BOM**: без BOM Windows PowerShell 5.1 читает файл как ANSI и портит русские сообщения. При правке сохранять так же. Если политика выполнения блокирует скрипты: `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned` и `Unblock-File .\*.ps1` (файлы, скопированные с другой машины, помечены как скачанные).

## 10. Неполадки

| Симптом | Причина и что делать |
|---|---|
| `password authentication failed for user "reestr_ro"` на `:5436` | Не переопределён `legacy.datasource.url` — взялся dev-URL из jar. Задать URL на `:5432` (раздел 5) |
| `Connection refused` на `:5435` при старте | Не запущена служба `postgresql-x64-16` или контейнер Docker ещё не поднялся |
| `permission denied for relation …` в legacy | Новый запрос читает таблицу, которой нет в `grant-readonly.sql`: добавить её туда (в репо) и повторить `create-legacy-ro.ps1` |
| «Проверить подключение» показывает read-only «нет» | Подключились не под `reestr_ro` или драйвер без `readOnlyMode=always`; приложение всё равно не пишет, но роль нужно проверить |
| Кракозябры в консоли | `chcp 65001` перед запуском или смотреть `logs\multi-reestr.log` |
| Белая страница / ошибка про dev-сервер Vaadin | jar собран без `-Pvaadin.productionMode=true` |
