# ContractTermType

Вид условия контракта, вводимого вручную.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.ContractTermType` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 8. Ручные данные: условия контрактов, даты ЛБО/ПГ |
| Используется | [ContractTerm](../entities/ContractTerm.md).`termType` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `DELIVERY` | `DELIVERY` | Срок поставки / оказания | замечание 9 |
| `ACCEPTANCE` | `ACCEPTANCE` | Срок приёмки | замечание 10 (п.3.5 контракта) |
| `PAYMENT` | `PAYMENT` | Срок оплаты |  |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/ContractTermType.DELIVERY=…`), Studio генерирует их в мастере перечисления.
