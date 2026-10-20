# CheckResultDto

**Назначение.** Результат проверки в памяти: показывается в окне «Проверить» и в dry-run; при сохранении проверки превращается в Violation.

| Параметр | Значение |
|---|---|
| Тип | **DTO entity** (`@JmixEntity`, не JPA, без `@Store`) — в Studio: *New → DTO Entity* |
| Java-класс | `com.digitilius.multireestr.entity.legacy.CheckResultDto` |
| Имя сущности | `mtr_CheckResultDto` |
| Таблица | — (не хранится) |
| Id | UUID, генерируется |
| Трейты Studio | нет |
| Instance name | `message` |
| Урок | 7. Проверка по запросу и реестр нарушений |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Источник (legacy) |
|---|---|---|---|---|---|---|---|---|---|
| `id` | UUID | `UUID` | да | да |  |  |  | Ключ `@JmixId`, генерируется | — |
| `ruleCode` | String | `String` | да |  | 50 |  |  | Код правила | `Rule.code` |
| `ruleName` | String | `String` |  |  | 255 |  |  | Правило | `Rule.name` |
| `severity` | Enum | `Severity` | да |  |  |  | [Severity](../enums/Severity.md) | Серьёзность | `RuleVersion/Rule` |
| `objectType` | Enum | `RuleObjectType` | да |  |  |  | [RuleObjectType](../enums/RuleObjectType.md) | Объект | — |
| `objectKey` | String | `String` | да |  | 50 |  |  | Ключ объекта | `номер извещения / reestr.id / год` |
| `detail` | String | `String` |  |  | 255 |  |  | Позиция/характеристика | — |
| `message` | String | `String` | да |  | unlimited |  |  | Пояснение (по шаблону) | — |
| `legalRef` | String | `String` |  |  | 1000 |  |  | Норма | `Rule.legalRef` |
| `fingerprint` | String | `String` | да |  | 64 |  |  | Отпечаток для сравнения/дедупликации | — |
| `diff` | Enum | `DryRunDiff` |  |  |  |  | [DryRunDiff](../enums/DryRunDiff.md) | Сравнение с действующей версией (dry-run) | — |
