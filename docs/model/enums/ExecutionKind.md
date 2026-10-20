# ExecutionKind

Вид записи исполнения контракта (`reestrprocexecutions.executiontype`).

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.ExecutionKind` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 3. DTO протоколов, контракта и исполнения |
| Используется | [ExecutionDto](../entities/ExecutionDto.md).`kind` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `ACCEPTANCE` | `ACCEPTANCE` | Приёмка | executiontype ≠ 1 (0) |
| `PAYMENT` | `PAYMENT` | Оплата | executiontype = 1 |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/ExecutionKind.ACCEPTANCE=…`), Studio генерирует их в мастере перечисления.
