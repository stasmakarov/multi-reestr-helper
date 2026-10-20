# ViolationStatus

Статус записи реестра нарушений.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.ViolationStatus` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 7. Проверка по запросу и реестр нарушений |
| Используется | [Violation](../entities/Violation.md).`status` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `NEW` | `NEW` | Новое |  |
| `ACKNOWLEDGED` | `ACKNOWLEDGED` | Принято к сведению | квитировано пользователем |
| `FALSE_POSITIVE` | `FALSE_POSITIVE` | Ложное срабатывание | требует комментария |
| `FIXED` | `FIXED` | Устранено | правило перестало срабатывать при повторной проверке |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/ViolationStatus.NEW=…`), Studio генерирует их в мастере перечисления.
