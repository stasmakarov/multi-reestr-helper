# CalendarDay

**Назначение.** День-исключение производственного календаря РФ. Хранятся только дни, отличающиеся от правила по умолчанию (Пн–Пт рабочие, Сб–Вс выходные): праздники, переносы, сокращённые дни.

| Параметр | Значение |
|---|---|
| Тип | **JPA entity**, хранилище `main` — в Studio: *New → JPA Entity* |
| Java-класс | `com.digitilius.multireestr.entity.CalendarDay` |
| Имя сущности | `mtr_CalendarDay` |
| Таблица | MTR_CALENDAR_DAY |
| Id | `id` UUID (`@JmixGeneratedValue`), колонка ID |
| Трейты Studio | Audit of creation, Audit of modification |
| Instance name | `date` |
| Урок | 4. Производственный календарь |

## Атрибуты

| Атрибут | Тип Studio | Java-тип | Обяз. | Уник. | Длина/точн. | Связь | Enum | Описание | Колонка |
|---|---|---|---|---|---|---|---|---|---|
| *(трейты)* | | | | | | | | `createdBy/Date`, `lastModifiedBy/Date` | CREATED_BY, … |
| `date` | LocalDate | `LocalDate` | да | да |  |  |  | Дата | `DATE_` |
| `year` | Integer | `Integer` | да |  |  |  |  | Год (для фильтра/удаления года) | `YEAR_` |
| `dayType` | Enum | `DayType` | да |  |  |  | [DayType](../enums/DayType.md) | Тип дня | `DAY_TYPE` |
| `source` | Enum | `CalendarSource` | да |  |  |  | [CalendarSource](../enums/CalendarSource.md) | Источник | `SOURCE` |
| `mark` | String | `String` |  |  | 255 |  |  | Метка-пояснение для пользователя: «День народного единства», «Перенос с субботы 01.11» | `MARK` |

## Индексы и ограничения

- UNIQUE `IDX_MTR_CALENDAR_DAY_UNQ` (DATE_)
- `IDX_MTR_CALENDAR_DAY_YEAR` (YEAR_)

## Примечания

- `date`/`year` — зарезервированные слова; Studio сама даст колонкам суффикс `_` (DATE_, YEAR_).
- Рабочими считаются WORKING, SHORTENED, WORKING_TRANSFER.
- Даты нет в таблице → действует правило по умолчанию (Сб/Вс — выходные). Запись в таблице всегда приоритетнее правила по умолчанию (WORKING_TRANSFER делает субботу рабочей).
- `WORKING` хранить обычно не нужно; допустим для явной отмены переноса.
- Импорт: кнопка «Загрузить год» (xmlcalendar.ru или файл, `source = XMLCALENDAR`), замена исключений года в одной транзакции; сверка с legacy `holidays`.
- Начальные данные: годы тестового набора данных грузятся по умолчанию из CSV в репо через Liquibase, без интернета.

## Сервис WorkingCalendar (урок 4)

Код сроков обращается к календарю только через интерфейс — чтобы позже можно было подставить реализацию на аддоне Jmix Business Calendar. Сигнатуры по образцу API аддона (`BusinessCalendar`):

```java
public interface WorkingCalendar {
    boolean isBusinessDay(LocalDate date);
    DayInfo getDayInfo(LocalDate date);              // businessDay, dayType, mark, fromTable
    LocalDate plus(LocalDate date, int businessDays);  // арифметика в рабочих днях
    LocalDate minus(LocalDate date, int businessDays);
}

public record DayInfo(LocalDate date, boolean businessDay, DayType dayType,
                      @Nullable String mark, boolean fromTable) {}
```

Реализация `DbWorkingCalendar`: читает `CalendarDay` за год в кэш (`Map<LocalDate, CalendarDay>`), сбрасывает кэш после импорта/правки.

Правила ст.191/193 ГК РФ — **не** в `WorkingCalendar`, а в отдельном `DeadlineCalculator` поверх него:
- `deadline(LocalDate event, int n, DayCountUnit unit)` — течение со следующего дня после события;
- `nextBusinessDayIfNonWorking(LocalDate date)` — перенос окончания срока с нерабочего дня.

Из аддона сознательно **не** берём: рабочие часы и `Duration`, CRON/ежегодные праздники, календари на аннотациях, несколько календарей по `code`.
