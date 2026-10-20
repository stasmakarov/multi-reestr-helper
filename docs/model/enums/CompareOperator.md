# CompareOperator

Как контролируемая дата сравнивается с рассчитанным сроком.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.CompareOperator` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 5. Движок сроков (ст.191–193 ГК) |
| Используется | [DeadlineSpec](../entities/DeadlineSpec.md).`compareOperator` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `NOT_LATER` | `NOT_LATER` | Не позднее срока | target ≤ deadline (замечания 1, 9, 10) |
| `NOT_EARLIER` | `NOT_EARLIER` | Не ранее срока | target ≥ deadline (замечание 6: окончание подачи ≥ изменение + 7 дн.) |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/CompareOperator.NOT_LATER=…`), Studio генерирует их в мастере перечисления.
