# LegacyConnectionSettings

**Назначение.** Параметры подключения к legacy-БД MultiReestr (PostgreSQL 9.6), которые администратор задаёт в UI: хост, порт, имя БД, логин и пароль. Сущность-«одиночка»: в таблице живёт одна запись с `code = DEFAULT`. После сохранения приложение пересобирает пул соединений `legacy` без перезапуска. Пока записи нет (первый старт), действуют `legacy.datasource.*` из `application.properties` и переменных окружения.

| Параметр | Значение |
|---|---|
| Тип | **JPA entity**, хранилище `main` — в Studio: *New → JPA Entity* |
| Java-класс | `com.digitilius.multireestr.entity.LegacyConnectionSettings` |
| Имя сущности | `mtr_LegacyConnectionSettings` |
| Таблица | MTR_LEGACY_CONNECTION_SETTINGS |
| Id | `id` UUID (`@JmixGeneratedValue`), колонка ID |
| Трейты Studio | Versioned, Audit of creation, Audit of modification (Soft delete не нужен) |
| Instance name | `host`, `port`, `databaseName` (например, «localhost:5432/reestrdb») |
| Урок | 1. Проект и подключение legacy-БД (read-only) — часть «Подключение из интерфейса администратора» |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Колонка |
|---|---|---|---|---|---|---|---|---|---|
| *(трейты)* | | | | | | | | `version`, `createdBy/Date`, `lastModifiedBy/Date` | VERSION, CREATED_BY, … |
| `code` | String | `String` | да | да | 50 |  |  | Ключ «одиночки»; всегда `DEFAULT`. Поле не показывается в UI | `CODE` |
| `enabled` | Boolean | `Boolean` | да |  |  |  |  | Подключение включено. `false` — приложение работает без legacy (как при пустом `legacy.datasource.url`) | `ENABLED` |
| `host` | String | `String` | да |  | 255 |  |  | Хост сервера PostgreSQL 9.6; на Windows-машине пользователя обычно `localhost` | `HOST` |
| `port` | Integer | `Integer` | да |  |  |  |  | Порт; по умолчанию `5432`. Допустимо 1–65535 | `PORT` |
| `databaseName` | String | `String` | да |  | 100 |  |  | Имя БД MultiReestr, обычно `reestrdb` | `DATABASE_NAME` |
| `username` | String | `String` | да |  | 100 |  |  | Логин; рекомендуется только `reestr_ro` (роль с `SELECT`), не `postgres` | `USERNAME` |
| `passwordEncrypted` | String | `String` |  |  | 1000 |  |  | Пароль в зашифрованном виде (Base64 от AES-GCM). Открытый пароль в БД не хранится и в UI не показывается | `PASSWORD_ENCRYPTED` |
| `lastCheckAt` | DateTime | `OffsetDateTime` |  |  |  |  |  | Когда последний раз нажимали «Проверить подключение» или пересобирали пул | `LAST_CHECK_AT` |
| `lastCheckOk` | Boolean | `Boolean` |  |  |  |  |  | Результат последней проверки | `LAST_CHECK_OK` |
| `lastCheckMessage` | String | `String` |  |  | 1000 |  |  | Версия сервера при успехе или текст ошибки (без пароля) | `LAST_CHECK_MESSAGE` |

## Индексы и ограничения

- UNIQUE `IDX_MTR_LEGACY_CONN_SETTINGS_UNQ` (CODE) — гарантирует одну запись.

## Примечания

- **Почему своя сущность.** Нужен шифрованный пароль, своя кнопка проверки и событие «после сохранения → пересобрать пул»; обычную JPA-сущность с одной записью проще объяснить в уроке и проверить тестом. Add-on Application Settings (`jmix-appsettings`) рассматривался и отклонён: лишняя зависимость, а шифрование и пересборку пула всё равно пришлось бы писать самим.
- **Одиночка.** Запись создаётся при первом сохранении экрана; экран всегда открывает запись `code = DEFAULT` (или новую с значениями из `legacy.datasource.*`). Список и удаление в UI не нужны.
- **Шифрование пароля (предложение).** Сервис `LegacySecretCipher` на `org.springframework.security.crypto.encrypt.Encryptors.delux(key, salt)` (AES-256-GCM, Spring Security уже в зависимостях). Ключ — переменная окружения `MTR_SECRET_KEY` (или свойство `mtr.secret-key`), в репозиторий и в БД ключ не попадает. Если ключ потерян или сменён, пароль не расшифруется: администратор вводит его заново. Если ключ не задан, сохранить пароль нельзя (экран показывает ошибку), а приложение продолжает работать на значениях из свойств.
- **Порядок источников.** 1) запись `LegacyConnectionSettings` с `enabled = true`; 2) `legacy.datasource.url/username/password` (свойства или переменные окружения `LEGACY_DATASOURCE_URL` и т. д.); 3) ничего — приложение стартует без legacy с предупреждением. JDBC-URL собирается из полей как `jdbc:postgresql://{host}:{port}/{databaseName}`; флаги read-only (`readOnly`, `readOnlyMode=always`) добавляет код, а не пользователь.
- **Параметры пула** (`maximum-pool-size`, `connection-timeout`, `initialization-fail-timeout`) остаются в `legacy.datasource.hikari.*` и в UI не выносятся.
- **Права.** Читать и менять запись может только администратор системы (`FullAccessRole`); ролям специалиста и администратора правил сущность не выдаётся (`@EntityPolicy` не перечисляет её).
- В тестовом наборе данных записи нет: тесты подключаются к Testcontainers через свойства.
