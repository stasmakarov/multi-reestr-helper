# ShiftRule

Перенос окончания срока, выпавшего на нерабочий день.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.ShiftRule` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 5. Движок сроков (ст.191–193 ГК) |
| Используется | [DeadlineSpec](../entities/DeadlineSpec.md).`shiftRule` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `NONE` | `NONE` | Без переноса | сроки в рабочих днях; сроки, где перенос не применяется |
| `NEXT_WORKDAY` | `NEXT_WORKDAY` | На следующий рабочий день (ст.193 ГК) | по умолчанию для календарных сроков |
| `PREVIOUS_WORKDAY` | `PREVIOUS_WORKDAY` | На предыдущий рабочий день | для внутренних контрольных сроков «не позднее» |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/ShiftRule.NONE=…`), Studio генерирует их в мастере перечисления.
