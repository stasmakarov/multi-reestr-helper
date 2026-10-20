# ViolationStatus

Статус записи реестра нарушений.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.ViolationStatus` (`implements EnumClass<Integer>`) |
| Тип id | **Integer** (как в коде; в БД — число в колонке `STATUS`) |
| Урок | 7. Проверка по запросу и реестр нарушений |
| Используется | [Violation](../entities/Violation.md).`status` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `NEW` | `10` | Новое |  |
| `ACKNOWLEDGED` | `20` | Принято к сведению | квитировано пользователем |
| `FALSE_POSITIVE` | `30` | Ложное срабатывание | требует комментария |
| `FIXED` | `40` | Устранено | правило перестало срабатывать при повторной проверке |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/ViolationStatus.NEW=…`; ключ сообщения — по имени константы, в БД — число), Studio генерирует их в мастере перечисления.
