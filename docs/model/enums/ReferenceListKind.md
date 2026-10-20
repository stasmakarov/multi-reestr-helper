# ReferenceListKind

Тип значений справочного списка.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.ReferenceListKind` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 6. Справочники и правила (шаблоны, версии, параметры) |
| Используется | [ReferenceList](../entities/ReferenceList.md).`kind` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `OKPD2` | `OKPD2` | Коды ОКПД2 | сравнение по префиксу или точно |
| `KTRU` | `KTRU` | Коды КТРУ |  |
| `KEYWORD` | `KEYWORD` | Ключевые слова | эвристики, замечание 8 |
| `TEXT` | `TEXT` | Произвольные значения |  |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/ReferenceListKind.OKPD2=…`), Studio генерирует их в мастере перечисления.
