# RuleObjectType

Объект, к которому применяется правило.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.RuleObjectType` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 6. Справочники и правила (шаблоны, версии, параметры) |
| Используется | [CheckResultDto](../entities/CheckResultDto.md).`objectType`, [Rule](../entities/Rule.md).`objectType`, [CheckRun](../entities/CheckRun.md).`objectType`, [Violation](../entities/Violation.md).`objectType` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `NOTICE` | `NOTICE` | Извещение | ключ — номер извещения (19 цифр) |
| `CONTRACT` | `CONTRACT` | Контракт | ключ — reestr.id |
| `PLAN_SCHEDULE` | `PLAN_SCHEDULE` | План-график | ключ — финансовый год |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/RuleObjectType.NOTICE=…`), Studio генерирует их в мастере перечисления.
