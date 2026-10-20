# ContractTerm

**Назначение.** Условие контракта, введённое вручную (срок поставки, приёмки, оплаты), которого нет в legacy-БД.

| Параметр | Значение |
|---|---|
| Тип | **JPA entity**, хранилище `main` — в Studio: *New → JPA Entity* |
| Java-класс | `com.digitilius.multireestr.entity.ContractTerm` |
| Имя сущности | `mtr_ContractTerm` |
| Таблица | MTR_CONTRACT_TERM |
| Id | `id` UUID (`@JmixGeneratedValue`), колонка ID |
| Трейты Studio | Versioned, Audit of creation, Audit of modification |
| Instance name | `termType` |
| Урок | 8. Ручные данные: условия контрактов, даты ЛБО/ПГ |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Колонка |
|---|---|---|---|---|---|---|---|---|---|
| *(трейты)* | | | | | | | | `version`, `createdBy/Date`, `lastModifiedBy/Date` | VERSION, CREATED_BY, … |
| `contractLegacyId` | Integer | `Integer` | да |  |  |  |  | reestr.id | `CONTRACT_LEGACY_ID` |
| `contractNumber` | String | `String` |  |  | 100 |  |  | Номер контракта (для отображения) | `CONTRACT_NUMBER` |
| `purchaseNumber` | String | `String` |  |  | 19 |  |  | Номер извещения | `PURCHASE_NUMBER` |
| `termType` | Enum | `ContractTermType` | да |  |  |  | [ContractTermType](../enums/ContractTermType.md) | Вид условия | `TERM_TYPE` |
| `term` | Embedded | [DeadlineSpec](DeadlineSpec.md) |  |  |  | embedded |  | Формула срока | `TERM_*` |
| `fixedDate` | LocalDate | `LocalDate` |  |  |  |  |  | Срок конкретной датой (если не формулой) | `FIXED_DATE` |
| `sourceClause` | String | `String` |  |  | 255 |  |  | Пункт контракта («п.3.5») | `SOURCE_CLAUSE` |
| `note` | String | `String` |  |  | unlimited |  |  | Примечание | `NOTE` |

## Индексы и ограничения

- UNIQUE (CONTRACT_LEGACY_ID, TERM_TYPE)

## Связи

- `term` → [DeadlineSpec](DeadlineSpec.md) (embedded)

## Примечания

- Ссылка на legacy — по id, не JPA-связь (legacy не моделируется).
- Пример: reestr.id 18 (25-000020), DELIVERY: CONTRACT_SIGNED +30 CALENDAR_DAYS, startNextDay, NEXT_WORKDAY → 22.01.2026.
