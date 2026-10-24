# NoticeCandidateDto

**Назначение.** Строка списка при поиске: в гриде — оригинал номера (Q2); все кандидаты номера по-прежнему размечаются в сервисе, копии смотрят в карточке.

| Параметр | Значение |
|---|---|
| Тип | **DTO entity** (`@JmixEntity`, не JPA, без `@Store`) — в Studio: *New → DTO Entity* |
| Java-класс | `com.digitilius.multireestr.entity.legacy.NoticeCandidateDto` |
| Имя сущности | `mtr_NoticeCandidateDto` |
| Таблица | — (не хранится) |
| Id | legacy-ключ Integer `@JmixId` (в мастере заменить тип id с UUID на Integer) |
| Трейты Studio | нет |
| Instance name | `number` |
| Урок | 2. DTO-модель извещения и поиск |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Источник (legacy) |
|---|---|---|---|---|---|---|---|---|---|
| `id` | Integer | `Integer` | да | да |  |  |  | Ключ `@JmixId` (legacy id) | `notices.id` |
| `number` | String | `String` | да |  | 19 |  |  | Номер извещения | `trim(notices.purchasenumber)` |
| `publishedAt` | LocalDateTime | `LocalDateTime` |  |  |  |  |  | Дата размещения (МСК, без зоны) | `notices.docpublishdate` |
| `subject` | String | `String` |  |  | 2000 |  |  | Объект закупки | `notices.predmet` |
| `modifVersion` | Integer | `Integer` |  |  |  |  |  | Номер редакции | `notices.modifversion` |
| `deleted` | Boolean | `Boolean` |  |  |  |  |  | Удалено | `coalesce(notices.isdeleted,0) <> 0` |
| `canceled` | Boolean | `Boolean` |  |  |  |  |  | Отменено | `coalesce(notices.iscanceled,0) <> 0` |
| `localCopy` | Boolean | `Boolean` |  |  |  |  |  | Локальная копия | `srcguid LIKE '{%' OR predmet ~* '^\s*Копия'` |
| `chosen` | Boolean | `Boolean` |  |  |  |  |  | Выбрано как оригинал (правило Q2) | `вычисляется` |
| `reason` | String | `String` |  |  | 255 |  |  | Почему не выбрано / пояснение | `вычисляется` |

## Примечания

- Порядок выбора оригинала — notice_lookup_spec.md, п.2.2.
