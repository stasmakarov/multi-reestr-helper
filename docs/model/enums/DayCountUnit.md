# DayCountUnit

Единица исчисления срока.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.DayCountUnit` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 5. Движок сроков (ст.191–193 ГК) |
| Используется | [DeadlineSpec](../entities/DeadlineSpec.md).`unit` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `CALENDAR_DAYS` | `CALENDAR_DAYS` | Календарные дни |  |
| `WORKING_DAYS` | `WORKING_DAYS` | Рабочие дни | по CalendarDay: WORKING, SHORTENED, WORKING_TRANSFER |
| `MONTHS` | `MONTHS` | Месяцы | ст.192 ГК — истекает в соответствующее число последнего месяца |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/DayCountUnit.CALENDAR_DAYS=…`), Studio генерирует их в мастере перечисления.
