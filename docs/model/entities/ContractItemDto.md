# ContractItemDto

**Назначение.** Позиция контракта.

| Параметр | Значение |
|---|---|
| Тип | **DTO entity** (`@JmixEntity`, не JPA, без `@Store`) — в Studio: *New → DTO Entity* |
| Java-класс | `com.digitilius.multireestr.entity.legacy.ContractItemDto` |
| Имя сущности | `mtr_ContractItemDto` |
| Таблица | — (не хранится) |
| Id | legacy-ключ Integer `@JmixId` (в мастере заменить тип id с UUID на Integer) |
| Трейты Studio | нет |
| Instance name | `name` |
| Урок | 3. DTO протоколов, контракта и исполнения |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Источник (legacy) |
|---|---|---|---|---|---|---|---|---|---|
| `id` | Integer | `Integer` | да | да |  |  |  | Ключ `@JmixId` (legacy id) | `reestrspec.id` |
| `name` | String | `String` | да |  | 2000 |  |  | Наименование (с «Товарный знак: …») | `reestrspec.articlename` |
| `okpd2` | String | `String` |  |  | 12 |  |  | ОКПД2 | `reestrspec.okpdcode` |
| `ktru` | String | `String` |  |  | 25 |  |  | КТРУ | `okpd4ktru + ktrusuffixcode, как у SpecPositionDto` |
| `qty` | BigDecimal | `BigDecimal` |  |  | 29,11 |  |  | Количество | `reestrspec.kol_vo` |
| `unit` | String | `String` |  |  | 100 |  |  | Ед. изм. | `reestrspec.unit` |
| `price` | BigDecimal | `BigDecimal` |  |  | 29,11 |  |  | Цена (до 11 знаков) | `reestrspec.cena` |
| `amount` | BigDecimal | `BigDecimal` |  |  | 18,2 |  |  | Сумма | `reestrspec.summa` |

## Связи

- используется в [ContractDto](ContractDto.md).`items`
