# RuleTemplateType

Шаблон правила — реализуется Java-классом; пользователь создаёт экземпляры (Rule) и задаёт параметры.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.RuleTemplateType` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 6. Справочники и правила (шаблоны, версии, параметры) |
| Используется | [Rule](../entities/Rule.md).`templateType` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `OKPD2_FOREIGN_BAN` | `OKPD2_FOREIGN_BAN` | ОКПД2 из перечня → нужен запрет/обоснование | замечание 3 (ПП 1875 прил.1) |
| `KTRU_REQUIRED` | `KTRU_REQUIRED` | Позиция без КТРУ при наличии кода КТРУ | замечание 4 (ПП 145) |
| `KTRU_EXTRA_CHARACTERISTICS` | `KTRU_EXTRA_CHARACTERISTICS` | Доп. характеристики к позиции КТРУ | замечание 5 |
| `NON_KTRU_NO_DESCRIPTION` | `NON_KTRU_NO_DESCRIPTION` | Нет структурированного описания (не КТРУ) | замечание 7 |
| `CONTRACT_KIND_MISMATCH` | `CONTRACT_KIND_MISMATCH` | Вид договора не соответствует предмету | замечание 8, эвристика |
| `DEADLINE` | `DEADLINE` | Контроль срока | замечания 1, 6, 10; использует DeadlineSpec |
| `LATE_DELIVERY_PENALTY` | `LATE_DELIVERY_PENALTY` | Просрочка поставки без неустойки | замечание 9; DeadlineSpec + проверка penaltyinfo |
| `DOCUMENT_STRUCTURE_MISMATCH` | `DOCUMENT_STRUCTURE_MISMATCH` | Файл ТЗ ≠ структурированная форма | замечание 2; позже, нужны файлы из ЕИС |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/RuleTemplateType.OKPD2_FOREIGN_BAN=…`), Studio генерирует их в мастере перечисления.
