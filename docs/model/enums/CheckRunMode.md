# CheckRunMode

Режим запуска проверки.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.CheckRunMode` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 7. Проверка по запросу и реестр нарушений |
| Используется | [CheckRun](../entities/CheckRun.md).`mode` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `ON_DEMAND` | `ON_DEMAND` | По запросу | кнопка «Проверить» |
| `SCHEDULED` | `SCHEDULED` | По расписанию | Quartz-задача, урок 9 |
| `DRY_RUN` | `DRY_RUN` | Пробный прогон | результаты не пишутся в реестр нарушений, урок 10 |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/CheckRunMode.ON_DEMAND=…`), Studio генерирует их в мастере перечисления.
