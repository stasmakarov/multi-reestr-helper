# LotDto

**Назначение.** Лот извещения.

| Параметр | Значение |
|---|---|
| Тип | **DTO entity** (`@JmixEntity`, не JPA, без `@Store`) — в Studio: *New → DTO Entity* |
| Java-класс | `com.digitilius.multireestr.entity.legacy.LotDto` |
| Имя сущности | `mtr_LotDto` |
| Таблица | — (не хранится) |
| Id | legacy-ключ Integer `@JmixId` (в мастере заменить тип id с UUID на Integer) |
| Трейты Studio | нет |
| Instance name | `number` |
| Урок | 2. DTO-модель извещения и поиск |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Источник (legacy) |
|---|---|---|---|---|---|---|---|---|---|
| `id` | Integer | `Integer` | да | да |  |  |  | Ключ `@JmixId` (legacy id) | `noticelots.id` |
| `number` | Integer | `Integer` |  |  |  |  |  | Номер лота | `noticelots.lotnumber` |
| `maxPrice` | BigDecimal | `BigDecimal` |  |  | 18,2 |  |  | НМЦ лота | `noticelots.maxprice` |
| `currency` | String | `String` |  |  | 3 |  |  | Валюта | `noticelots.currencycode` |

## Связи

- используется в [NoticeDto](NoticeDto.md).`lots`
