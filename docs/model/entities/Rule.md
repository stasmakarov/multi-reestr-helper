# Rule

**Назначение.** Правило — экземпляр шаблона (RuleTemplateType), созданный специалистом.

| Параметр | Значение |
|---|---|
| Тип | **JPA entity**, хранилище `main` — в Studio: *New → JPA Entity* |
| Java-класс | `com.digitilius.multireestr.entity.Rule` |
| Имя сущности | `mtr_Rule` |
| Таблица | MTR_RULE |
| Id | `id` UUID (`@JmixGeneratedValue`), колонка ID |
| Трейты Studio | Versioned, Audit of creation, Audit of modification |
| Instance name | `name` |
| Урок | 6. Справочники и правила (шаблоны, версии, параметры) |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Колонка |
|---|---|---|---|---|---|---|---|---|---|
| *(трейты)* | | | | | | | | `version`, `createdBy/Date`, `lastModifiedBy/Date` | VERSION, CREATED_BY, … |
| `code` | String | `String` | да | да | 50 |  |  | Код, напр. R03_PP1875 | `CODE` |
| `name` | String | `String` | да |  | 255 |  |  | Название | `NAME` |
| `templateType` | Enum | `RuleTemplateType` | да |  |  |  | [RuleTemplateType](../enums/RuleTemplateType.md) | Шаблон | `TEMPLATE_TYPE` |
| `objectType` | Enum | `RuleObjectType` | да |  |  |  | [RuleObjectType](../enums/RuleObjectType.md) | Объект проверки | `OBJECT_TYPE` |
| `severity` | Enum | `Severity` | да |  |  |  | [Severity](../enums/Severity.md) | Серьёзность | `SEVERITY` |
| `legalRef` | String | `String` |  |  | 1000 |  |  | Норма (текст) | `LEGAL_REF` |
| `explanationTemplate` | String | `String` |  |  | unlimited |  |  | Шаблон пояснения с {плейсхолдерами} | `EXPLANATION_TEMPLATE` |
| `enabled` | Boolean | `Boolean` | да |  |  |  |  | Включено | `ENABLED` |
| `description` | String | `String` |  |  | unlimited |  |  | Описание | `DESCRIPTION` |
| `versions` | Composition | List<[RuleVersion](RuleVersion.md)> |  |  |  | one-to-many |  | Версии | — |

## Индексы и ограничения

- UNIQUE (CODE)

## Связи

- `versions` → List<[RuleVersion](RuleVersion.md)> (one-to-many, composition)
- используется в [RuleVersion](RuleVersion.md).`rule`
- используется в [Violation](Violation.md).`rule`

## Примечания

- Soft delete не используем (уникальный код + история нарушений) — отключение через `enabled`.
- Шаблоны — не сущность, а enum + Java-бины.
