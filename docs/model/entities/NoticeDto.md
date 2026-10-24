# NoticeDto

**Назначение.** Карточка извещения 44-ФЗ (Q3–Q11 из notice_lookup_spec.md) и входной объект для правил по извещению.

| Параметр | Значение |
|---|---|
| Тип | **DTO entity** (`@JmixEntity`, не JPA, без `@Store`) — в Studio: *New → DTO Entity* |
| Java-класс | `com.digitilius.multireestr.entity.legacy.NoticeDto` |
| Имя сущности | `mtr_NoticeDto` |
| Таблица | — (не хранится) |
| Id | legacy-ключ Integer `@JmixId` (в мастере заменить тип id с UUID на Integer) |
| Трейты Studio | нет |
| Instance name | `number` |
| Урок | 2. DTO-модель извещения и поиск |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Источник (legacy) |
|---|---|---|---|---|---|---|---|---|---|
| `id` | Integer | `Integer` | да | да |  |  |  | Ключ `@JmixId` (legacy id) | `notices.id` |
| `number` | String | `String` | да |  | 19 |  |  | Номер извещения | `trim(notices.purchasenumber)` |
| `publishedAt` | LocalDateTime | `LocalDateTime` |  |  |  |  |  | Дата размещения текущей редакции | `notices.docpublishdate` |
| `modifVersion` | Integer | `Integer` |  |  |  |  |  | Номер редакции (>1 — были изменения) | `notices.modifversion` |
| `subject` | String | `String` |  |  | 2000 |  |  | Объект закупки | `notices.predmet` |
| `law` | String | `String` |  |  | 10 |  |  | «44-ФЗ»/«223-ФЗ» | `notices.prikaz 1/0` |
| `methodCode` | String | `String` |  |  | 20 |  |  | Способ (код ЕИС) | `notices.placingway_code` |
| `methodName` | String | `String` |  |  | 255 |  |  | Наименование способа | `spdogovortypes.name по rel44type = notices.dogovortype` |
| `contractKindCode` | Integer | `Integer` |  |  |  |  |  | Код вида договора ЕИС | `notices.dogovortype` |
| `platform` | String | `String` |  |  | 200 |  |  | ЭТП | `notices.etp_name` |
| `eisUrl` | String | `String` |  |  | 1024 |  |  | Ссылка на ЕИС | `notices.dochref` |
| `nmck` | BigDecimal | `BigDecimal` |  |  | 18,2 |  |  | НМЦК | `notices.firstmaxcena` |
| `ikz` | String | `String` |  |  | 40 |  |  | ИКЗ | `noticecustomerrequirements.ikz` |
| `planPosition` | String | `String` |  |  | 30 |  |  | Позиция плана-графика | `noticecustomerrequirements.pglotreestrnumber` |
| `applicationsFrom` | LocalDateTime | `LocalDateTime` |  |  |  |  |  | Начало подачи (= размещение 1-й редакции) | `noticeprocedureinfo.collectingstartdate` |
| `applicationsTo` | LocalDateTime | `LocalDateTime` |  |  |  |  |  | Окончание подачи | `noticeprocedureinfo.collectingenddate` |
| `biddingDate` | LocalDate | `LocalDate` |  |  |  |  |  | Дата аукциона | `noticeprocedureinfo.biddingdate::date` |
| `summarizingDate` | LocalDate | `LocalDate` |  |  |  |  |  | Подведение итогов | `noticeprocedureinfo.summarizingdate::date` |
| `appGuaranteeAmount` | BigDecimal | `BigDecimal` |  |  | 18,2 |  |  | Обеспечение заявки, руб. | `noticecustomerrequirements.aguarantee_amount` |
| `appGuaranteePct` | BigDecimal | `BigDecimal` |  |  | 8,2 |  |  | Обеспечение заявки, % | `noticecustomerrequirements.aguarantee_part` |
| `contractGuaranteePct` | BigDecimal | `BigDecimal` |  |  | 8,2 |  |  | Обеспечение контракта, % | `noticecustomerrequirements.cguarantee_part` |
| `executionTermDays` | Integer | `Integer` |  |  |  |  |  | Срок исполнения, дней | `noticecustomerrequirements.relativelen (если ≠ 0)` |
| `executionEndDate` | LocalDate | `LocalDate` |  |  |  |  |  | Дата окончания исполнения | `noticecustomerrequirements.nonrelativeenddate` |
| `preferences` | String | `String` |  |  | 1000 |  |  | Преференции/требования строкой («код 56116975; ET44; TR442») | `noticepurchaseprefs + sppurchasepreferences` |
| `customer` | Association | [OrganizationDto](OrganizationDto.md) |  |  |  | many-to-one |  | Заказчик | `notices.ownerid → owners` |
| `finances` | Composition | List<[NoticeFinanceDto](NoticeFinanceDto.md)> |  |  |  | one-to-many |  | Финансирование (КБК, год, сумма) | `noticefinances` |
| `lots` | Composition | List<[LotDto](LotDto.md)> |  |  |  | one-to-many |  | Лоты | `noticelots` |
| `items` | Composition | List<[SpecPositionDto](SpecPositionDto.md)> |  |  |  | one-to-many |  | Позиции спецификации | `noticespec` |
| `documents` | Composition | List<[NoticeDocumentDto](NoticeDocumentDto.md)> |  |  |  | one-to-many |  | Документы | `noticeextdocs` |
| `protocols` | Composition | List<[ProtocolDto](ProtocolDto.md)> |  |  |  | one-to-many |  | Протоколы | `protocols по номеру` |
| `contract` | Association | [ContractDto](ContractDto.md) |  |  |  | many-to-one |  | Контракт (может отсутствовать) | `reestr по trim(tendernumber)` |
| `warnings` | String (list) | `List<String>` |  |  |  |  |  | Предупреждения сборки карточки | `вычисляется` |

## Связи

- `customer` → [OrganizationDto](OrganizationDto.md) (many-to-one)
- `lots` → List<[LotDto](LotDto.md)> (one-to-many, composition)
- `finances` → List<[NoticeFinanceDto](NoticeFinanceDto.md)> (one-to-many, composition)
- `items` → List<[SpecPositionDto](SpecPositionDto.md)> (one-to-many, composition)
- `documents` → List<[NoticeDocumentDto](NoticeDocumentDto.md)> (one-to-many, composition)
- `protocols` → List<[ProtocolDto](ProtocolDto.md)> (one-to-many, composition)
- `contract` → [ContractDto](ContractDto.md) (many-to-one)

## Примечания

- `dogovortype` у извещения — код ЕИС (`spdogovortypes.rel44type`), у контракта — id справочника.
- `warnings` — список строк; если Studio не даст выбрать `List<String>`, добавить вручную в коде (в tech_solution_jmix.md указано, что это допустимо для DTO) **[проверить в Studio 3]**.
