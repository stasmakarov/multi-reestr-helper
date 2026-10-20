# ReferenceList

**Назначение.** Справочный список, редактируемый пользователем (ОКПД2 ПП 1875 прил.1, КТРУ, ключевые слова).

| Параметр | Значение |
|---|---|
| Тип | **JPA entity**, хранилище `main` — в Studio: *New → JPA Entity* |
| Java-класс | `com.digitilius.multireestr.entity.ReferenceList` |
| Имя сущности | `mtr_ReferenceList` |
| Таблица | MTR_REFERENCE_LIST |
| Id | `id` UUID (`@JmixGeneratedValue`), колонка ID |
| Трейты Studio | Versioned, Audit of creation, Audit of modification |
| Instance name | `name` |
| Урок | 6. Справочники и правила (шаблоны, версии, параметры) |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Колонка |
|---|---|---|---|---|---|---|---|---|---|
| *(трейты)* | | | | | | | | `version`, `createdBy/Date`, `lastModifiedBy/Date` | VERSION, CREATED_BY, … |
| `code` | String | `String` | да | да | 50 |  |  | Код (латиница), напр. PP1875_ANNEX1 | `CODE` |
| `name` | String | `String` | да |  | 255 |  |  | Название | `NAME` |
| `kind` | Enum | `ReferenceListKind` | да |  |  |  | [ReferenceListKind](../enums/ReferenceListKind.md) | Тип значений | `KIND` |
| `legalRef` | String | `String` |  |  | 1000 |  |  | Нормативный акт | `LEGAL_REF` |
| `description` | String | `String` |  |  | unlimited |  |  | Описание | `DESCRIPTION` |
| `items` | Composition | List<[ReferenceListItem](ReferenceListItem.md)> |  |  |  | one-to-many |  | Элементы | — |

## Индексы и ограничения

- UNIQUE (CODE)

## Связи

- `items` → List<[ReferenceListItem](ReferenceListItem.md)> (one-to-many, composition)
- используется в [ReferenceListItem](ReferenceListItem.md).`list`
- используется в [RuleParameter](RuleParameter.md).`referenceList`

## Примечания

- Начальное заполнение ПП 1875 — из legacy `okpd2_1875_zap` (ppnumber, code, prim) с исключениями `okpd2_1875_zap_exclude`.
