# CheckRunStatus

Состояние запуска проверки.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.CheckRunStatus` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 7. Проверка по запросу и реестр нарушений |
| Используется | [CheckRun](../entities/CheckRun.md).`status` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `RUNNING` | `RUNNING` | Выполняется |  |
| `SUCCESS` | `SUCCESS` | Завершён |  |
| `PARTIAL` | `PARTIAL` | Завершён с ошибками | часть объектов не проверена |
| `FAILED` | `FAILED` | Ошибка |  |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/CheckRunStatus.RUNNING=…`), Studio генерирует их в мастере перечисления.
