# ProtocolDto

**Назначение.** Протокол закупки.

| Параметр | Значение |
|---|---|
| Тип | **DTO entity** (`@JmixEntity`, не JPA, без `@Store`) — в Studio: *New → DTO Entity* |
| Java-класс | `com.digitilius.multireestr.entity.legacy.ProtocolDto` |
| Имя сущности | `mtr_ProtocolDto` |
| Таблица | — (не хранится) |
| Id | legacy-ключ Integer `@JmixId` (в мастере заменить тип id с UUID на Integer) |
| Трейты Studio | нет |
| Instance name | `number` |
| Урок | 3. DTO протоколов, контракта и исполнения |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Источник (legacy) |
|---|---|---|---|---|---|---|---|---|---|
| `id` | Integer | `Integer` | да | да |  |  |  | Ключ `@JmixId` (legacy id) | `protocols.id` |
| `number` | String | `String` | да |  | 500 |  |  | Номер протокола | `protocols.protocolnumber` |
| `date` | LocalDate | `LocalDate` |  |  |  |  |  | Дата (может быть null) | `protocols.protocoldate` |
| `typeCode` | Integer | `Integer` |  |  |  |  |  | Код типа | `protocols.protocoltype` |
| `typeName` | String | `String` |  |  | 500 |  |  | Тип | `protocol_types.name; для 0 — «нет в справочнике»` |

## Связи

- используется в [NoticeDto](NoticeDto.md).`protocols`
