# NoticeFinanceDto

**Назначение.** Строка финансирования извещения (Q7 из notice_lookup_spec.md): КБК, год, сумма.

| Параметр | Значение |
|---|---|
| Тип | **DTO entity** (`@JmixEntity`, не JPA, без `@Store`) — в Studio: *New → DTO Entity* |
| Java-класс | `com.digitilius.multireestr.entity.legacy.NoticeFinanceDto` |
| Имя сущности | `mtr_NoticeFinanceDto` |
| Таблица | — (не хранится) |
| Id | legacy-ключ Integer `@JmixId` (в мастере заменить тип id с UUID на Integer) |
| Трейты Studio | нет |
| Instance name | `kbk` |
| Урок | 3. Протоколы, контракт, исполнение; карточка и Excel |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Источник (legacy) |
|---|---|---|---|---|---|---|---|---|---|
| `id` | Integer | `Integer` | да | да |  |  |  | Ключ `@JmixId` (legacy id) | `noticefinances.id` |
| `lotId` | Integer | `Integer` |  |  |  |  |  | Лот (legacy id) | `noticefinances.lotid` |
| `kbk` | String | `String` |  |  | 20 |  |  | Код бюджетной классификации | `noticefinances.kbk` |
| `finYear` | Integer | `Integer` |  |  |  |  |  | Год финансирования | `noticefinances.finyear` |
| `amount` | BigDecimal | `BigDecimal` |  |  | 18,2 |  |  | Сумма, руб. | `noticefinances.summa` |

## Связи

- используется в [NoticeDto](NoticeDto.md).`finances`

## Примечания

- В тестовом дампе у каждого извещения одна строка финансирования; сумма совпадает с НМЦК. В общем случае строк несколько (по годам/КБК).
