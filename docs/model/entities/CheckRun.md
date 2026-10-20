# CheckRun

**Назначение.** Журнал запусков проверок (по запросу, плановых, dry-run).

| Параметр | Значение |
|---|---|
| Тип | **JPA entity**, хранилище `main` — в Studio: *New → JPA Entity* |
| Java-класс | `com.digitilius.multireestr.entity.CheckRun` |
| Имя сущности | `mtr_CheckRun` |
| Таблица | MTR_CHECK_RUN |
| Id | `id` UUID (`@JmixGeneratedValue`), колонка ID |
| Трейты Studio | Audit of creation, Audit of modification |
| Instance name | `startedAt` |
| Урок | 7. Проверка по запросу и реестр нарушений |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Колонка |
|---|---|---|---|---|---|---|---|---|---|
| *(трейты)* | | | | | | | | `createdBy/Date`, `lastModifiedBy/Date` | CREATED_BY, … |
| `mode` | Enum | `CheckRunMode` | да |  |  |  | [CheckRunMode](../enums/CheckRunMode.md) | Режим | `MODE_` |
| `status` | Enum | `CheckRunStatus` | да |  |  |  | [CheckRunStatus](../enums/CheckRunStatus.md) | Статус; поле хранится как `Integer` (id 10/20/30…), геттер/сеттер — enum через `fromId` | `STATUS` |
| `startedAt` | DateTime | `OffsetDateTime` | да |  |  |  |  | Начало | `STARTED_AT` |
| `finishedAt` | DateTime | `OffsetDateTime` |  |  |  |  |  | Окончание | `FINISHED_AT` |
| `objectType` | Enum | `RuleObjectType` |  |  |  |  | [RuleObjectType](../enums/RuleObjectType.md) | Тип объектов (null — все) | `OBJECT_TYPE` |
| `objectKey` | String | `String` |  |  | 50 |  |  | Один объект (по запросу) | `OBJECT_KEY` |
| `periodFrom` | LocalDate | `LocalDate` |  |  |  |  |  | Период с (dry-run/скан) | `PERIOD_FROM` |
| `periodTo` | LocalDate | `LocalDate` |  |  |  |  |  | Период по | `PERIOD_TO` |
| `ruleVersion` | Association | [RuleVersion](RuleVersion.md) |  |  |  | many-to-one |  | Проверяемая версия (dry-run) | `RULE_VERSION_ID` |
| `objectsChecked` | Integer | `Integer` |  |  |  |  |  | Проверено объектов | `OBJECTS_CHECKED` |
| `violationsFound` | Integer | `Integer` |  |  |  |  |  | Найдено срабатываний | `VIOLATIONS_FOUND` |
| `errorMessage` | String | `String` |  |  | unlimited |  |  | Ошибка | `ERROR_MESSAGE` |

## Индексы и ограничения

- `IDX_MTR_CHECK_RUN_STARTED` (STARTED_AT)

## Связи

- `ruleVersion` → [RuleVersion](RuleVersion.md) (many-to-one)
- используется в [Violation](Violation.md).`lastCheckRun`

## Примечания

- Кто запустил — `createdBy` из трейта аудита.
- Результаты dry-run не сохраняются (CheckResultDto в памяти), сохраняется только статистика.
