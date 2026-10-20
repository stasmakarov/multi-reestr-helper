# CheckRunStatus

Состояние запуска проверки.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.CheckRunStatus` (`implements EnumClass<Integer>`) |
| Тип id | **Integer** (как в коде; в БД — число в колонке `STATUS`) |
| Урок | 7. Проверка по запросу и реестр нарушений |
| Используется | [CheckRun](../entities/CheckRun.md).`status` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `RUNNING` | `10` | Выполняется |  |
| `SUCCESS` | `20` | Завершён |  |
| `PARTIAL` | `30` | Завершён с ошибками | часть объектов не проверена |
| `FAILED` | `40` | Ошибка |  |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/CheckRunStatus.RUNNING=…`; ключ сообщения — по имени константы, в БД — число), Studio генерирует их в мастере перечисления.
