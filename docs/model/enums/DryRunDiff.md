# DryRunDiff

Сравнение результата dry-run с действующей версией правила.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.DryRunDiff` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 10. Dry-run правил на исторических данных |
| Используется | [CheckResultDto](../entities/CheckResultDto.md).`diff` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `NEW` | `NEW` | Новое срабатывание |  |
| `UNCHANGED` | `UNCHANGED` | Без изменений |  |
| `DISAPPEARED` | `DISAPPEARED` | Исчезло | срабатывало в действующей версии, в новой нет |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/DryRunDiff.NEW=…`), Studio генерирует их в мастере перечисления.
