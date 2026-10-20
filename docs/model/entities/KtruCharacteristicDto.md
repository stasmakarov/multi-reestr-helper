# KtruCharacteristicDto

**Назначение.** Характеристика позиции (КТРУ или заказчика) со значением; вход для правила «доп. характеристики» (замечание 5).

| Параметр | Значение |
|---|---|
| Тип | **DTO entity** (`@JmixEntity`, не JPA, без `@Store`) — в Studio: *New → DTO Entity* |
| Java-класс | `com.digitilius.multireestr.entity.legacy.KtruCharacteristicDto` |
| Имя сущности | `mtr_KtruCharacteristicDto` |
| Таблица | — (не хранится) |
| Id | legacy-ключ Integer `@JmixId` (в мастере заменить тип id с UUID на Integer) |
| Трейты Studio | нет |
| Instance name | `name` |
| Урок | 2. DTO-модель извещения и поиск |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Источник (legacy) |
|---|---|---|---|---|---|---|---|---|---|
| `id` | Integer | `Integer` | да | да |  |  |  | Ключ `@JmixId` (legacy id) | `noticektruharacts.id` |
| `code` | String | `String` |  |  | 10 |  |  | Код характеристики | `noticektruharacts.haractcode` |
| `name` | String | `String` | да |  | 2000 |  |  | Наименование | `noticektruharacts.haractname` |
| `value` | String | `String` |  |  | unlimited |  |  | Значение(я) через « | » | `noticektruharactvalues: qualitydesc → concretevalue → диапазон` |
| `ktruCode` | String | `String` |  |  | 25 |  |  | Код КТРУ/ОКПД2 позиции | `noticektruharacts.ktrucode` |
| `fromEis` | Boolean | `Boolean` |  |  |  |  |  | Из справочника КТРУ ЕИС (false — добавлена заказчиком) | `noticektruharacts.isfromoos = 1` |
| `required` | Boolean | `Boolean` |  |  |  |  |  | Обязательная | `noticektruharacts.isrequired = 1` |
| `charType` | Integer | `Integer` |  |  |  |  |  | Тип (код legacy) | `noticektruharacts.haracttype` |
| `charKind` | Integer | `Integer` |  |  |  |  |  | Вид (код legacy) | `noticektruharacts.haractkind` |

## Связи

- используется в [SpecPositionDto](SpecPositionDto.md).`characteristics`
