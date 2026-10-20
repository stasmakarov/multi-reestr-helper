# PlanScheduleDto

**Назначение.** Версия плана-графика из legacy — подсказка для ввода PlanScheduleEvent.

| Параметр | Значение |
|---|---|
| Тип | **DTO entity** (`@JmixEntity`, не JPA, без `@Store`) — в Studio: *New → DTO Entity* |
| Java-класс | `com.digitilius.multireestr.entity.legacy.PlanScheduleDto` |
| Имя сущности | `mtr_PlanScheduleDto` |
| Таблица | — (не хранится) |
| Id | legacy-ключ Integer `@JmixId` (в мастере заменить тип id с UUID на Integer) |
| Трейты Studio | нет |
| Instance name | `finYear` |
| Урок | 8. Ручные данные: условия контрактов, даты ЛБО/ПГ |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Источник (legacy) |
|---|---|---|---|---|---|---|---|---|---|
| `id` | Integer | `Integer` | да | да |  |  |  | Ключ `@JmixId` (legacy id) | `planzakuppackets.id` |
| `finYear` | Integer | `Integer` | да |  |  |  |  | Год | `planzakuppackets.finyear` |
| `version` | Integer | `Integer` |  |  |  |  |  | Версия ЕИС | `planzakuppackets.oosversion` |
| `publishDate` | LocalDateTime | `LocalDateTime` |  |  |  |  |  | Размещение | `planzakuppackets.publishdate` |
| `approveDate` | LocalDateTime | `LocalDateTime` |  |  |  |  |  | Утверждение (в данных пусто) | `planzakuppackets.approvedate` |

## Примечания

- В БД только текущие версии (2024, 2025), `approvedate` пуст; даты ЛБО нет — поэтому ручной ввод.
