# Severity

Серьёзность нарушения.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.Severity` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 6. Справочники и правила (шаблоны, версии, параметры) |
| Используется | [CheckResultDto](../entities/CheckResultDto.md).`severity`, [Rule](../entities/Rule.md).`severity`, [Violation](../entities/Violation.md).`severity` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `ERROR` | `ERROR` | Ошибка | нарушение закона, высокая вероятность замечания |
| `WARNING` | `WARNING` | Предупреждение | требует проверки человеком (эвристики, возможны ложные срабатывания) |
| `INFO` | `INFO` | Информация | напоминания, приближение срока |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/Severity.ERROR=…`), Studio генерирует их в мастере перечисления.
