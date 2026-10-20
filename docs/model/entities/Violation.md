# Violation

**Назначение.** Реестр нарушений: срабатывание правила по объекту, со статусом и квитированием.

| Параметр | Значение |
|---|---|
| Тип | **JPA entity**, хранилище `main` — в Studio: *New → JPA Entity* |
| Java-класс | `com.digitilius.multireestr.entity.Violation` |
| Имя сущности | `mtr_Violation` |
| Таблица | MTR_VIOLATION |
| Id | `id` UUID (`@JmixGeneratedValue`), колонка ID |
| Трейты Studio | Versioned, Audit of creation, Audit of modification |
| Instance name | `message` |
| Урок | 7. Проверка по запросу и реестр нарушений |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Колонка |
|---|---|---|---|---|---|---|---|---|---|
| *(трейты)* | | | | | | | | `version`, `createdBy/Date`, `lastModifiedBy/Date` | VERSION, CREATED_BY, … |
| `rule` | Association | [Rule](Rule.md) | да |  |  | many-to-one |  | Правило | `RULE_ID` |
| `ruleVersion` | Association | [RuleVersion](RuleVersion.md) | да |  |  | many-to-one |  | Версия, давшая срабатывание | `RULE_VERSION_ID` |
| `lastCheckRun` | Association | [CheckRun](CheckRun.md) |  |  |  | many-to-one |  | Последний запуск, подтвердивший срабатывание | `LAST_CHECK_RUN_ID` |
| `objectType` | Enum | `RuleObjectType` | да |  |  |  | [RuleObjectType](../enums/RuleObjectType.md) | Объект | `OBJECT_TYPE` |
| `objectKey` | String | `String` | да |  | 50 |  |  | Номер извещения / reestr.id / год | `OBJECT_KEY` |
| `legacyId` | Integer | `Integer` |  |  |  |  |  | notices.id / reestr.id | `LEGACY_ID` |
| `objectCaption` | String | `String` |  |  | 255 |  |  | Подпись объекта (номер + предмет) | `OBJECT_CAPTION` |
| `detail` | String | `String` |  |  | 255 |  |  | Позиция/характеристика | `DETAIL` |
| `fingerprint` | String | `String` | да | да | 64 |  |  | SHA-256(ruleCode|objectKey|detail) | `FINGERPRINT` |
| `severity` | Enum | `Severity` | да |  |  |  | [Severity](../enums/Severity.md) | Серьёзность | `SEVERITY` |
| `message` | String | `String` | да |  | unlimited |  |  | Пояснение | `MESSAGE` |
| `legalRef` | String | `String` |  |  | 1000 |  |  | Норма (копия на момент срабатывания) | `LEGAL_REF` |
| `dueDate` | LocalDate | `LocalDate` |  |  |  |  |  | Рассчитанный срок | `DUE_DATE` |
| `factDate` | LocalDate | `LocalDate` |  |  |  |  |  | Фактическая дата | `FACT_DATE` |
| `deviationDays` | Integer | `Integer` |  |  |  |  |  | Отклонение, дней | `DEVIATION_DAYS` |
| `status` | Enum | `ViolationStatus` | да |  |  |  | [ViolationStatus](../enums/ViolationStatus.md) | Статус | `STATUS` |
| `firstDetectedAt` | DateTime | `OffsetDateTime` | да |  |  |  |  | Впервые найдено | `FIRST_DETECTED_AT` |
| `lastDetectedAt` | DateTime | `OffsetDateTime` | да |  |  |  |  | Последний раз найдено | `LAST_DETECTED_AT` |
| `resolvedAt` | DateTime | `OffsetDateTime` |  |  |  |  |  | Устранено | `RESOLVED_AT` |
| `acknowledgedBy` | String | `String` |  |  | 255 |  |  | Кто квитировал (username) | `ACKNOWLEDGED_BY` |
| `acknowledgedAt` | DateTime | `OffsetDateTime` |  |  |  |  |  | Когда квитировал | `ACKNOWLEDGED_AT` |
| `comment` | String | `String` |  |  | unlimited |  |  | Комментарий (обязателен для FALSE_POSITIVE) | `COMMENT_` |

## Индексы и ограничения

- UNIQUE (FINGERPRINT)
- `IDX_MTR_VIOLATION_OBJECT` (OBJECT_TYPE, OBJECT_KEY)
- `IDX_MTR_VIOLATION_STATUS` (STATUS)

## Связи

- `rule` → [Rule](Rule.md) (many-to-one)
- `ruleVersion` → [RuleVersion](RuleVersion.md) (many-to-one)
- `lastCheckRun` → [CheckRun](CheckRun.md) (many-to-one)

## Примечания

- Повторный скан: найдено → обновить lastDetectedAt; не найдено → FIXED.
- Отдельной истории изменений нет и add-on Jmix Audit (entity log) не используется: жизненный цикл — `status` + `firstDetectedAt`/`lastDetectedAt`/`resolvedAt`/`acknowledgedBy`/`acknowledgedAt`, «кто/когда изменил» — трейты аудита ядра.
- Уведомление «в приложении» — сам реестр нарушений и счётчик новых (NEW) записей в меню.
