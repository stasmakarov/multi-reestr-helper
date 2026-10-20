# ContractDto

**Назначение.** Контракт из `reestr`; вход для правил по исполнению (замечания 9, 10).

| Параметр | Значение |
|---|---|
| Тип | **DTO entity** (`@JmixEntity`, не JPA, без `@Store`) — в Studio: *New → DTO Entity* |
| Java-класс | `com.digitilius.multireestr.entity.legacy.ContractDto` |
| Имя сущности | `mtr_ContractDto` |
| Таблица | — (не хранится) |
| Id | legacy-ключ Integer `@JmixId` (в мастере заменить тип id с UUID на Integer) |
| Трейты Studio | нет |
| Instance name | `number` |
| Урок | 3. DTO протоколов, контракта и исполнения |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Источник (legacy) |
|---|---|---|---|---|---|---|---|---|---|
| `id` | Integer | `Integer` | да | да |  |  |  | Ключ `@JmixId` (legacy id) | `reestr.id` |
| `number` | String | `String` | да |  | 100 |  |  | Номер контракта | `trim(reestr.contractnumber)` |
| `purchaseNumber` | String | `String` |  |  | 19 |  |  | Номер извещения | `trim(reestr.tendernumber)` |
| `registryNumber` | String | `String` |  |  | 50 |  |  | Реестровый номер | `reestr.reestrnumber` |
| `ikz` | String | `String` |  |  | 36 |  |  | ИКЗ | `reestr.ikz` |
| `date` | LocalDate | `LocalDate` |  |  |  |  |  | Дата заключения | `reestr.contractdate` |
| `endDate` | LocalDate | `LocalDate` |  |  |  |  |  | Окончание действия | `reestr.contractdateend` |
| `price` | BigDecimal | `BigDecimal` |  |  | 18,2 |  |  | Цена | `reestr.contractcena` |
| `reductionPct` | BigDecimal | `BigDecimal` |  |  | 5,1 |  |  | Снижение, % | `(1 − contractcena / firstmaxcena)·100` |
| `paidTotal` | BigDecimal | `BigDecimal` |  |  | 21,2 |  |  | Оплачено | `sum(paid) where executiontype = 1` |
| `customer` | Association | [OrganizationDto](OrganizationDto.md) |  |  |  | many-to-one |  | Заказчик | `reestr.ownerid → owners` |
| `supplier` | Association | [OrganizationDto](OrganizationDto.md) |  |  |  | many-to-one |  | Поставщик | `reestr.winnerid → owners` |
| `items` | Composition | List<[ContractItemDto](ContractItemDto.md)> |  |  |  | one-to-many |  | Позиции | `reestrspec` |
| `executions` | Composition | List<[ExecutionDto](ExecutionDto.md)> |  |  |  | one-to-many |  | Приёмка и оплаты | `reestrprocedures → reestrprocexecutions` |

## Связи

- `customer` → [OrganizationDto](OrganizationDto.md) (many-to-one)
- `supplier` → [OrganizationDto](OrganizationDto.md) (many-to-one)
- `items` → List<[ContractItemDto](ContractItemDto.md)> (one-to-many, composition)
- `executions` → List<[ExecutionDto](ExecutionDto.md)> (one-to-many, composition)
- используется в [NoticeDto](NoticeDto.md).`contract`

## Примечания

- Отбор: `coalesce(isdeleted,0)=0`, по `contractdate DESC, id DESC`.
- `contractdateend` может не совпадать со сроком поставки по формуле контракта (25-000020: 21.01 против 22.01.2026) — для правил брать ContractTerm.
