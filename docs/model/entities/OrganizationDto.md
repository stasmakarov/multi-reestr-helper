# OrganizationDto

**Назначение.** Организация из `owners`: заказчик извещения/контракта и поставщик (`reestr.winnerid`). В tech_solution_jmix.md называлась `SupplierDto` — переименована, т.к. используется и для заказчика.

| Параметр | Значение |
|---|---|
| Тип | **DTO entity** (`@JmixEntity`, не JPA, без `@Store`) — в Studio: *New → DTO Entity* |
| Java-класс | `com.digitilius.multireestr.entity.legacy.OrganizationDto` |
| Имя сущности | `mtr_OrganizationDto` |
| Таблица | — (не хранится) |
| Id | legacy-ключ Integer `@JmixId` (в мастере заменить тип id с UUID на Integer) |
| Трейты Studio | нет |
| Instance name | `name` |
| Урок | 2. DTO-модель извещения и поиск |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Источник (legacy) |
|---|---|---|---|---|---|---|---|---|---|
| `id` | Integer | `Integer` | да | да |  |  |  | Ключ `@JmixId` (legacy id) | `owners.id` |
| `name` | String | `String` | да |  | 500 |  |  | Краткое наименование | `owners.name` |
| `fullName` | String | `String` |  |  | 500 |  |  | Полное наименование | `owners.fullname` |
| `inn` | String | `String` |  |  | 16 |  |  | ИНН | `owners.ownerinn` |
| `kpp` | String | `String` |  |  | 9 |  |  | КПП | `owners.ownerkpp` |

## Связи

- используется в [NoticeDto](NoticeDto.md).`customer`
- используется в [ContractDto](ContractDto.md).`customer`
- используется в [ContractDto](ContractDto.md).`supplier`

## Примечания

- Поля `reestr.winnername/winnerinn` пусты — источник только `owners`.
