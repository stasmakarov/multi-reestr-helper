# ParameterType

Тип значения параметра правила.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.ParameterType` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 6. Справочники и правила (шаблоны, версии, параметры) |
| Используется | [RuleParameter](../entities/RuleParameter.md).`paramType` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `STRING` | `STRING` | Строка |  |
| `INTEGER` | `INTEGER` | Целое |  |
| `DECIMAL` | `DECIMAL` | Число |  |
| `BOOLEAN` | `BOOLEAN` | Да/нет | значения `true`/`false` |
| `DATE` | `DATE` | Дата | ISO `yyyy-MM-dd` |
| `LIST` | `LIST` | Справочный список | значение — ссылка referenceList |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/ParameterType.STRING=…`), Studio генерирует их в мастере перечисления.
