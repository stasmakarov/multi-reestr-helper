# ReferenceListItem

**Назначение.** Элемент справочного списка.

| Параметр | Значение |
|---|---|
| Тип | **JPA entity**, хранилище `main` — в Studio: *New → JPA Entity* |
| Java-класс | `com.digitilius.multireestr.entity.ReferenceListItem` |
| Имя сущности | `mtr_ReferenceListItem` |
| Таблица | MTR_REFERENCE_LIST_ITEM |
| Id | `id` UUID (`@JmixGeneratedValue`), колонка ID |
| Трейты Studio | Versioned, Audit of creation, Audit of modification |
| Instance name | `value` |
| Урок | 6. Справочники и правила (шаблоны, версии, параметры) |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Колонка |
|---|---|---|---|---|---|---|---|---|---|
| *(трейты)* | | | | | | | | `version`, `createdBy/Date`, `lastModifiedBy/Date` | VERSION, CREATED_BY, … |
| `list` | Association | [ReferenceList](ReferenceList.md) | да |  |  | many-to-one (owner: ReferenceList.items) |  | Список | `LIST_ID` |
| `value` | String | `String` | да |  | 255 |  |  | Значение (код ОКПД2/КТРУ, слово) | `VALUE_` |
| `prefixMatch` | Boolean | `Boolean` |  |  |  |  |  | Совпадение по префиксу (14.1 → 14.12.30.150) | `PREFIX_MATCH` |
| `excluded` | Boolean | `Boolean` |  |  |  |  |  | Исключение из перечня | `EXCLUDED` |
| `groupCode` | String | `String` |  |  | 20 |  |  | Пункт перечня (ppnumber, «3.00») | `GROUP_CODE` |
| `caption` | String | `String` |  |  | 2000 |  |  | Наименование/примечание | `CAPTION` |
| `validFrom` | LocalDate | `LocalDate` |  |  |  |  |  | Действует с | `VALID_FROM` |
| `validTo` | LocalDate | `LocalDate` |  |  |  |  |  | Действует по | `VALID_TO` |

## Индексы и ограничения

- `IDX_MTR_REF_LIST_ITEM_LIST_VALUE` (LIST_ID, VALUE_)

## Связи

- `list` → [ReferenceList](ReferenceList.md) (many-to-one (owner: ReferenceList.items))
- используется в [ReferenceList](ReferenceList.md).`items`

## Примечания

- В legacy `code` может содержать несколько кодов через запятую — при импорте разбивать на элементы.
