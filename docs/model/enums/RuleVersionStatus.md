# RuleVersionStatus

Жизненный цикл версии правила.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.RuleVersionStatus` (`implements EnumClass<Integer>`) |
| Тип id | **Integer** (как в коде; в БД — число в колонке `STATUS`) |
| Урок | 6. Справочники и правила (шаблоны, версии, параметры) |
| Используется | [RuleVersion](../entities/RuleVersion.md).`status` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `DRAFT` | `10` | Черновик | редактируется, участвует только в dry-run |
| `ACTIVE` | `20` | Действует | участвует в проверках в период validFrom–validTo |
| `ARCHIVED` | `30` | Архив | не используется, хранится для истории |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/RuleVersionStatus.DRAFT=…`; ключ сообщения — по имени константы, в БД — число), Studio генерирует их в мастере перечисления.
