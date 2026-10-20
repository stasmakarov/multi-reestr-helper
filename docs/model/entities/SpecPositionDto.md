# SpecPositionDto

**Назначение.** Позиция спецификации извещения; вход для правил ПП 1875, КТРУ, описания (замечания 3, 4, 5, 7).

| Параметр | Значение |
|---|---|
| Тип | **DTO entity** (`@JmixEntity`, не JPA, без `@Store`) — в Studio: *New → DTO Entity* |
| Java-класс | `com.digitilius.multireestr.entity.legacy.SpecPositionDto` |
| Имя сущности | `mtr_SpecPositionDto` |
| Таблица | — (не хранится) |
| Id | legacy-ключ Integer `@JmixId` (в мастере заменить тип id с UUID на Integer) |
| Трейты Studio | нет |
| Instance name | `name` |
| Урок | 2. DTO-модель извещения и поиск |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Источник (legacy) |
|---|---|---|---|---|---|---|---|---|---|
| `id` | Integer | `Integer` | да | да |  |  |  | Ключ `@JmixId` (legacy id) | `noticespec.id` |
| `lotId` | Integer | `Integer` |  |  |  |  |  | Лот | `noticespec.lotid` |
| `pos` | Integer | `Integer` |  |  |  |  |  | Порядковый номер | `row_number() по id` |
| `name` | String | `String` | да |  | 2000 |  |  | Наименование | `noticespec.articlename` |
| `okpd2` | String | `String` |  |  | 20 |  |  | ОКПД2 | `noticespec.okpdcode` |
| `okpd4Ktru` | String | `String` |  |  | 12 |  |  | ОКПД2-часть кода КТРУ | `noticespec.okpd4ktru` |
| `ktru` | String | `String` |  |  | 25 |  |  | Код КТРУ или null | `okpd4ktru || '-' || lpad(ktrusuffixcode,8,'0') при suffix ≠ 0` |
| `qty` | BigDecimal | `BigDecimal` |  |  | 29,11 |  |  | Количество | `noticespec.kol_vo` |
| `unit` | String | `String` |  |  | 100 |  |  | Ед. изм. | `noticespec.unit` |
| `okei` | Integer | `Integer` |  |  |  |  |  | Код ОКЕИ | `noticespec.okeicode` |
| `price` | BigDecimal | `BigDecimal` |  |  | 29,11 |  |  | Цена | `noticespec.cena` |
| `amount` | BigDecimal | `BigDecimal` |  |  | 18,2 |  |  | Сумма | `noticespec.summa` |
| `foreignBan` | Boolean | `Boolean` |  |  |  |  |  | Установлен запрет (ст.14) | `noticespec.isprohibitionforeignpo = 1` |
| `foreignRestriction` | Boolean | `Boolean` |  |  |  |  |  | Установлено ограничение | `noticespec.isrestrictforeignpo = 1` |
| `rfPreference` | Boolean | `Boolean` |  |  |  |  |  | Преимущество РФ | `noticespec.ispreferenserfpo = 1` |
| `banImpossible` | Boolean | `Boolean` |  |  |  |  |  | Невозможность соблюдения запрета | `noticespec.isimposibilityprohibition = 1` |
| `banImpossibleReason` | String | `String` |  |  | unlimited |  |  | Обоснование невозможности | `noticespec.reasonimposibilityprohibition` |
| `characteristicsText` | String | `String` |  |  | unlimited |  |  | Характеристики одной строкой (для грида) | `вычисляется` |
| `characteristics` | Composition | List<[KtruCharacteristicDto](KtruCharacteristicDto.md)> |  |  |  | one-to-many |  | Характеристики | `noticektruharacts по noticespecid` |

## Связи

- `characteristics` → List<[KtruCharacteristicDto](KtruCharacteristicDto.md)> (one-to-many, composition)
- используется в [NoticeDto](NoticeDto.md).`items`

## Примечания

- Флаги в БД — smallint с NULL: маппинг `coalesce(x,0) = 1`.
- `okpd4ktru` может отличаться от `okpdcode` (25-000005) — не исправлять.
