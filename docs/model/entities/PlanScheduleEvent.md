# PlanScheduleEvent

**Назначение.** Даты доведения ЛБО и утверждения плана-графика (замечание 1) — в legacy их нет.

| Параметр | Значение |
|---|---|
| Тип | **JPA entity**, хранилище `main` — в Studio: *New → JPA Entity* |
| Java-класс | `com.digitilius.multireestr.entity.PlanScheduleEvent` |
| Имя сущности | `mtr_PlanScheduleEvent` |
| Таблица | MTR_PLAN_SCHEDULE_EVENT |
| Id | `id` UUID (`@JmixGeneratedValue`), колонка ID |
| Трейты Studio | Versioned, Audit of creation, Audit of modification |
| Instance name | `finYear` |
| Урок | 8. Ручные данные: условия контрактов, даты ЛБО/ПГ |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Колонка |
|---|---|---|---|---|---|---|---|---|---|
| *(трейты)* | | | | | | | | `version`, `createdBy/Date`, `lastModifiedBy/Date` | VERSION, CREATED_BY, … |
| `finYear` | Integer | `Integer` | да |  |  |  |  | Финансовый год | `FIN_YEAR` |
| `planVersion` | Integer | `Integer` |  |  |  |  |  | Версия ПГ (oosversion) | `PLAN_VERSION` |
| `legacyPlanId` | Integer | `Integer` |  |  |  |  |  | planzakuppackets.id | `LEGACY_PLAN_ID` |
| `lboDate` | LocalDate | `LocalDate` | да |  |  |  |  | Дата доведения ЛБО | `LBO_DATE` |
| `approvalDate` | LocalDate | `LocalDate` |  |  |  |  |  | Дата утверждения ПГ | `APPROVAL_DATE` |
| `basisDocument` | String | `String` |  |  | 255 |  |  | Документ (расписание, уведомление о ЛБО) | `BASIS_DOCUMENT` |
| `note` | String | `String` |  |  | unlimited |  |  | Примечание | `NOTE` |

## Индексы и ограничения

- UNIQUE (FIN_YEAR, PLAN_VERSION)

## Примечания

- Пример: ЛБО 26.03.2025, утверждение 10.04.2025 → срок 09.04.2025, просрочка 1 раб. день.
