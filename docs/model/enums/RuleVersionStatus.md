# RuleVersionStatus

Жизненный цикл версии правила.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.RuleVersionStatus` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 6. Справочники и правила (шаблоны, версии, параметры) |
| Используется | [RuleVersion](../entities/RuleVersion.md).`status` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `DRAFT` | `DRAFT` | Черновик | редактируется, участвует только в dry-run |
| `ACTIVE` | `ACTIVE` | Действует | участвует в проверках в период validFrom–validTo |
| `ARCHIVED` | `ARCHIVED` | Архив | не используется, хранится для истории |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/RuleVersionStatus.DRAFT=…`), Studio генерирует их в мастере перечисления.
