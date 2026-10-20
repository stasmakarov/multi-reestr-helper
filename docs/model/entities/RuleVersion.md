# RuleVersion

**Назначение.** Версия параметров правила с периодом действия (историческая проверка по нормам на дату).

| Параметр | Значение |
|---|---|
| Тип | **JPA entity**, хранилище `main` — в Studio: *New → JPA Entity* |
| Java-класс | `com.digitilius.multireestr.entity.RuleVersion` |
| Имя сущности | `mtr_RuleVersion` |
| Таблица | MTR_RULE_VERSION |
| Id | `id` UUID (`@JmixGeneratedValue`), колонка ID |
| Трейты Studio | Versioned, Audit of creation, Audit of modification |
| Instance name | `versionNumber` |
| Урок | 6. Справочники и правила (шаблоны, версии, параметры) |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Колонка |
|---|---|---|---|---|---|---|---|---|---|
| *(трейты)* | | | | | | | | `version`, `createdBy/Date`, `lastModifiedBy/Date` | VERSION, CREATED_BY, … |
| `rule` | Association | [Rule](Rule.md) | да |  |  | many-to-one (owner: Rule.versions) |  | Правило | `RULE_ID` |
| `versionNumber` | Integer | `Integer` | да |  |  |  |  | Номер версии | `VERSION_NUMBER` |
| `status` | Enum | `RuleVersionStatus` | да |  |  |  | [RuleVersionStatus](../enums/RuleVersionStatus.md) | Статус; поле хранится как `Integer` (id 10/20/30…), геттер/сеттер — enum через `fromId` | `STATUS` |
| `validFrom` | LocalDate | `LocalDate` | да |  |  |  |  | Действует с (по дате размещения/заключения) | `VALID_FROM` |
| `validTo` | LocalDate | `LocalDate` |  |  |  |  |  | Действует по (null — бессрочно) | `VALID_TO` |
| `deadline` | Embedded | [DeadlineSpec](DeadlineSpec.md) |  |  |  | embedded |  | Срок (для DEADLINE, LATE_DELIVERY_PENALTY) | `DEADLINE_*` |
| `parameters` | Composition | List<[RuleParameter](RuleParameter.md)> |  |  |  | one-to-many |  | Параметры | — |
| `changeNote` | String | `String` |  |  | unlimited |  |  | Что изменено | `CHANGE_NOTE` |
| `approvedBy` | String | `String` |  |  | 255 |  |  | Кто активировал (username) | `APPROVED_BY` |
| `approvedAt` | DateTime | `OffsetDateTime` |  |  |  |  |  | Когда активировал | `APPROVED_AT` |

## Индексы и ограничения

- UNIQUE (RULE_ID, VERSION_NUMBER)
- `IDX_MTR_RULE_VERSION_RULE_STATUS` (RULE_ID, STATUS)

## Связи

- `rule` → [Rule](Rule.md) (many-to-one (owner: Rule.versions))
- `deadline` → [DeadlineSpec](DeadlineSpec.md) (embedded)
- `parameters` → List<[RuleParameter](RuleParameter.md)> (one-to-many, composition)
- используется в [Rule](Rule.md).`versions`
- используется в [RuleParameter](RuleParameter.md).`ruleVersion`
- используется в [CheckRun](CheckRun.md).`ruleVersion`
- используется в [Violation](Violation.md).`ruleVersion`

## Примечания

- Инвариант (в сервисе, не в БД): периоды ACTIVE-версий одного правила не пересекаются.
- `approvedBy` — строка username, как в трейтах аудита Jmix.
