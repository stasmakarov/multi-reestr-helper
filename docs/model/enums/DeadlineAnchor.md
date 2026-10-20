# DeadlineAnchor

Событие-якорь, от которого считается срок, или контролируемая дата.

| Параметр | Значение |
|---|---|
| Java-класс | `com.digitilius.multireestr.entity.DeadlineAnchor` (`implements EnumClass<String>`) |
| Тип id | String |
| Урок | 5. Движок сроков (ст.191–193 ГК) |
| Используется | [DeadlineSpec](../entities/DeadlineSpec.md).`anchor`, [DeadlineSpec](../entities/DeadlineSpec.md).`target` |

## Значения

| Константа | id | Заголовок (ru) | Примечание |
|---|---|---|---|
| `LBO_DATE` | `LBO_DATE` | Доведение ЛБО | PlanScheduleEvent.lboDate (ручной ввод), замечание 1 |
| `PLAN_APPROVAL_DATE` | `PLAN_APPROVAL_DATE` | Утверждение плана-графика | PlanScheduleEvent.approvalDate / planzakuppackets.approvedate |
| `NOTICE_PUBLISHED` | `NOTICE_PUBLISHED` | Размещение извещения (первая редакция) | noticeprocedureinfo.collectingstartdate |
| `NOTICE_CHANGE_PUBLISHED` | `NOTICE_CHANGE_PUBLISHED` | Размещение изменений извещения | notices.docpublishdate при modifversion > 1; замечание 6 |
| `APPLICATIONS_END` | `APPLICATIONS_END` | Окончание подачи заявок | noticeprocedureinfo.collectingenddate |
| `CONTRACT_SIGNED` | `CONTRACT_SIGNED` | Заключение контракта | reestr.contractdate; замечание 9 |
| `DELIVERY_DOCUMENT` | `DELIVERY_DOCUMENT` | Документ о приёмке (дата документа поставщика) | reestrprocexecutions.de_documentdate; замечания 9, 10 |
| `ACCEPTANCE` | `ACCEPTANCE` | Приёмка заказчиком | reestrprocexecutions.de_deliveryacceptdate; замечание 10 |
| `PAYMENT` | `PAYMENT` | Оплата | reestrprocexecutions.pd_documentdate (executiontype = 1) |
| `STAGE_END` | `STAGE_END` | Окончание этапа по реестру | reestrprocedures.stageenddate |

Заголовки — в `messages_ru.properties` (`com.digitilius.multireestr.entity/DeadlineAnchor.LBO_DATE=…`), Studio генерирует их в мастере перечисления.
