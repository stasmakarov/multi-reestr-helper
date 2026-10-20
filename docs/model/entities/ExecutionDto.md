# ExecutionDto

**Назначение.** Документ исполнения: приёмка или оплата (с данными этапа).

| Параметр | Значение |
|---|---|
| Тип | **DTO entity** (`@JmixEntity`, не JPA, без `@Store`) — в Studio: *New → DTO Entity* |
| Java-класс | `com.digitilius.multireestr.entity.legacy.ExecutionDto` |
| Имя сущности | `mtr_ExecutionDto` |
| Таблица | — (не хранится) |
| Id | legacy-ключ Integer `@JmixId` (в мастере заменить тип id с UUID на Integer) |
| Трейты Studio | нет |
| Instance name | `docNumber` |
| Урок | 3. DTO протоколов, контракта и исполнения |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Источник (legacy) |
|---|---|---|---|---|---|---|---|---|---|
| `id` | Integer | `Integer` | да | да |  |  |  | Ключ `@JmixId` (legacy id) | `reestrprocexecutions.id` |
| `kind` | Enum | `ExecutionKind` | да |  |  |  | [ExecutionKind](../enums/ExecutionKind.md) | Приёмка/оплата | `executiontype` |
| `stageNumber` | Integer | `Integer` |  |  |  |  |  | Номер этапа | `reestrprocedures.ordinalnumber` |
| `stageEndDate` | LocalDate | `LocalDate` |  |  |  |  |  | Окончание этапа | `reestrprocedures.stageenddate` |
| `penaltyInfo` | String | `String` |  |  | 2000 |  |  | Сведения о неустойке по этапу | `reestrprocedures.penaltyinfo` |
| `docName` | String | `String` |  |  | 1000 |  |  | Документ | `pd_documentname (оплата)` |
| `docNumber` | String | `String` |  |  | 100 |  |  | Номер документа | `de_documentnum / pd_documentnum` |
| `docDate` | LocalDate | `LocalDate` |  |  |  |  |  | Дата документа | `de_documentdate / pd_documentdate` |
| `acceptDate` | LocalDate | `LocalDate` |  |  |  |  |  | Дата приёмки | `de_deliveryacceptdate` |
| `amount` | BigDecimal | `BigDecimal` |  |  | 21,2 |  |  | Сумма | `de_fulfilmentsum / paid` |
| `improperExecution` | String | `String` |  |  | 2000 |  |  | Ненадлежащее исполнение | `improperexecutiontext` |

## Связи

- используется в [ContractDto](ContractDto.md).`executions`

## Примечания

- Для замечания 9 дата поставки — `docDate` приёмки, **не** дата платёжного поручения.
- Приёмки на 0,00 показывать, в суммы не включать.
