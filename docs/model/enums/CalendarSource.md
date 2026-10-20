# CalendarSource

Откуда загружен день календаря.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.CalendarSource` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 4. Производственный календарь |
| Используется | [CalendarDay](../entities/CalendarDay.md).`source` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `XMLCALENDAR` | `XMLCALENDAR` | xmlcalendar.ru |  |
| `LEGACY` | `LEGACY` | MultiReestr (таблица holidays) | неполный: нет переносных рабочих суббот после 2021 |
| `GENERATED` | `GENERATED` | Сгенерирован (пн–пт рабочие) | заготовка года до загрузки |
| `MANUAL` | `MANUAL` | Введён вручную |  |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/CalendarSource.XMLCALENDAR=…`), Studio генерирует их в мастере перечисления.
