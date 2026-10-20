# RuleParameter

**Назначение.** Параметр версии правила (ключ-значение, тип задаётся шаблоном).

| Параметр | Значение |
|---|---|
| Тип | **JPA entity**, хранилище `main` — в Studio: *New → JPA Entity* |
| Java-класс | `com.digitilius.multireestr.entity.RuleParameter` |
| Имя сущности | `mtr_RuleParameter` |
| Таблица | MTR_RULE_PARAMETER |
| Id | `id` UUID (`@JmixGeneratedValue`), колонка ID |
| Трейты Studio | Versioned, Audit of creation, Audit of modification |
| Instance name | `code` |
| Урок | 6. Справочники и правила (шаблоны, версии, параметры) |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Колонка |
|---|---|---|---|---|---|---|---|---|---|
| *(трейты)* | | | | | | | | `version`, `createdBy/Date`, `lastModifiedBy/Date` | VERSION, CREATED_BY, … |
| `ruleVersion` | Association | [RuleVersion](RuleVersion.md) | да |  |  | many-to-one (owner: RuleVersion.parameters) |  | Версия | `RULE_VERSION_ID` |
| `code` | String | `String` | да |  | 50 |  |  | Код параметра (из схемы шаблона) | `CODE` |
| `paramType` | Enum | `ParameterType` | да |  |  |  | [ParameterType](../enums/ParameterType.md) | Тип | `PARAM_TYPE` |
| `value` | String | `String` |  |  | 4000 |  |  | Значение строкой | `VALUE_` |
| `referenceList` | Association | [ReferenceList](ReferenceList.md) |  |  |  | many-to-one |  | Список (для LIST) | `REFERENCE_LIST_ID` |

## Индексы и ограничения

- UNIQUE (RULE_VERSION_ID, CODE)

## Связи

- `ruleVersion` → [RuleVersion](RuleVersion.md) (many-to-one (owner: RuleVersion.parameters))
- `referenceList` → [ReferenceList](ReferenceList.md) (many-to-one)
- используется в [RuleVersion](RuleVersion.md).`parameters`

## Примечания

- Пример: R03 → `okpdList`=LIST PP1875_ANNEX1; R06 (зам.5) → `allowedIfKtruPermits`=BOOLEAN true.
