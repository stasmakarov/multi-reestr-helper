# DayType

Тип дня производственного календаря.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.DayType` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 4. Производственный календарь |
| Используется | [CalendarDay](../entities/CalendarDay.md).`dayType` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `WORKING` | `WORKING` | Рабочий | обычный будний день; в таблице обычно не хранится (правило по умолчанию) |
| `SHORTENED` | `SHORTENED` | Рабочий сокращённый | предпраздничный; xmlcalendar `t=2`; **считается рабочим** |
| `WEEKEND` | `WEEKEND` | Выходной | суббота/воскресенье и перенесённые выходные; xmlcalendar `t=1` без праздника |
| `HOLIDAY` | `HOLIDAY` | Нерабочий праздничный | ст.112 ТК РФ; xmlcalendar `t=1` с `h` |
| `WORKING_TRANSFER` | `WORKING_TRANSFER` | Рабочий (перенесённый выходной) | рабочая суббота; xmlcalendar `t=3`; legacy `holidays.isworkday = true`; **приоритетнее правила по умолчанию** (аналог AdditionalBusinessDay в аддоне) |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/DayType.WORKING=…`), Studio генерирует их в мастере перечисления.

Рабочие типы: WORKING, SHORTENED, WORKING_TRANSFER — удобно завести метод `boolean isBusiness()` в enum.
