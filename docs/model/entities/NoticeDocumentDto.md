# NoticeDocumentDto

**Назначение.** Документ извещения (ссылка на filestore ЕИС). Нужен для замечания 2: файлов в legacy-БД нет.

| Параметр | Значение |
|---|---|
| Тип | **DTO entity** (`@JmixEntity`, не JPA, без `@Store`) — в Studio: *New → DTO Entity* |
| Java-класс | `com.digitilius.multireestr.entity.legacy.NoticeDocumentDto` |
| Имя сущности | `mtr_NoticeDocumentDto` |
| Таблица | — (не хранится) |
| Id | legacy-ключ Integer `@JmixId` (в мастере заменить тип id с UUID на Integer) |
| Трейты Studio | нет |
| Instance name | `name` |
| Урок | 2. DTO-модель извещения и поиск |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Источник (legacy) |
|---|---|---|---|---|---|---|---|---|---|
| `id` | Integer | `Integer` | да | да |  |  |  | Ключ `@JmixId` (legacy id) | `noticeextdocs.id` |
| `name` | String | `String` | да |  | 100 |  |  | Имя файла | `noticeextdocs.name` |
| `url` | String | `String` |  |  | 1024 |  |  | Ссылка ЕИС | `noticeextdocs.url` |

## Связи

- используется в [NoticeDto](NoticeDto.md).`documents`

## Примечания

- `allbinarycontext` пуст, `binarycontextid` = NULL — содержимое только по URL.
