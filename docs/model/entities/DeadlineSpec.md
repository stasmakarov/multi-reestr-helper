# DeadlineSpec

**Назначение.** Декларативное описание срока: якорь + смещение + единица + перенос + сравнение. Встраивается в RuleVersion и ContractTerm.

| Параметр | Значение |
|---|---|
| Тип | **Embeddable** (`@JmixEntity` + `@Embeddable`) — в Studio: *New → JPA Entity*, тип *Embeddable* |
| Java-класс | `com.digitilius.multireestr.entity.DeadlineSpec` |
| Имя сущности | `mtr_DeadlineSpec` |
| Таблица | — (колонки во владельце) |
| Трейты Studio | — |
| Instance name | `—` |
| Урок | 5. Движок сроков (ст.191–193 ГК) |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Колонка |
|---|---|---|---|---|---|---|---|---|---|
| `anchor` | Enum | `DeadlineAnchor` |  |  |  |  | [DeadlineAnchor](../enums/DeadlineAnchor.md) | От какого события считать | `ANCHOR` |
| `offsetValue` | Integer | `Integer` |  |  |  |  |  | Смещение N | `OFFSET_VALUE` |
| `unit` | Enum | `DayCountUnit` |  |  |  |  | [DayCountUnit](../enums/DayCountUnit.md) | Единица | `UNIT` |
| `startNextDay` | Boolean | `Boolean` |  |  |  |  |  | Течение со следующего дня (ст.191 ГК), по умолч. true | `START_NEXT_DAY` |
| `shiftRule` | Enum | `ShiftRule` |  |  |  |  | [ShiftRule](../enums/ShiftRule.md) | Перенос (ст.193 ГК) | `SHIFT_RULE` |
| `target` | Enum | `DeadlineAnchor` |  |  |  |  | [DeadlineAnchor](../enums/DeadlineAnchor.md) | Контролируемая дата | `TARGET` |
| `compareOperator` | Enum | `CompareOperator` |  |  |  |  | [CompareOperator](../enums/CompareOperator.md) | Сравнение | `COMPARE_OPERATOR` |
| `warnBeforeDays` | Integer | `Integer` |  |  |  |  |  | За сколько дней предупреждать (плановый скан) | `WARN_BEFORE_DAYS` |

## Связи

- используется в [RuleVersion](RuleVersion.md).`deadline`
- используется в [ContractTerm](ContractTerm.md).`term`

## Примечания

- Все атрибуты необязательные: встраивание nullable (`@EmbeddedParameters(nullAllowed = true)`), т.к. не у каждого правила есть срок.
- Колонки получают префикс через Studio (`DEADLINE_` в RuleVersion, `TERM_` в ContractTerm) — задать в настройке embedded-атрибута.
- Примеры: зам.1 LBO_DATE +10 WORKING_DAYS NONE → PLAN_APPROVAL_DATE NOT_LATER; зам.6 NOTICE_CHANGE_PUBLISHED +7 CALENDAR_DAYS → APPLICATIONS_END NOT_EARLIER; зам.9 CONTRACT_SIGNED +30 CALENDAR_DAYS NEXT_WORKDAY → DELIVERY_DOCUMENT NOT_LATER.
