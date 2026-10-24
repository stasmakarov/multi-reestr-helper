# План уроков: multi-reestr (Jmix 3.0.3)

Курс состоит из 10 уроков. Каждый урок заканчивается коммитом и тегом `lesson-NN` (`lesson-01` … `lesson-10`). На каждом теге проект собирается и проходит тесты, поэтому курс можно продолжить с любого тега. Пользовательские сценарии (не урок) — в [USAGE.md](USAGE.md).

Модель данных (сущности и перечисления) описана заранее в [model/OVERVIEW.md](model/OVERVIEW.md) и создаётся **в начале курса** в визарде Jmix Studio. Нумерация уроков совпадает с колонкой «Урок» в [model/README.md](model/README.md) и в карточках сущностей и перечислений. Экраны и навигация описаны в [UI.md](UI.md).

## Уроки

1. **Проект и подключение legacy-БД (read-only).**
   - Материал урока: [lessons/lesson-01-legacy.md](lessons/lesson-01-legacy.md).
   - Каркас: проект Jmix 3.0.3 на Java 21, пользователь admin, создание модели данных в Studio. Основная БД: в разработке HSQLDB (или `postgres:16`), в продуктиве — отдельный PostgreSQL 16 на порту `5435`. Jmix 3 не работает с PostgreSQL 9.6, поэтому на сервере MultiReestr основную БД не размещаем.
   - Второй DataSource `legacy`: БД MultiReestr подключается без Liquibase, через `NamedParameterJdbcTemplate` (`legacyJdbc`). Приложение стартует и без неё (с предупреждением). Защита от записи на трёх уровнях: драйвер (`readOnlyMode=always`), Hikari и пользователь БД.
   - Тестовый стенд: Testcontainers `postgres:9.6`, схема из бэкапа, данные по 10 эталонным номерам (12 записей извещений, включая дубли), тест на запрет записи.
   - Подключение из интерфейса: сущность [LegacyConnectionSettings](model/entities/LegacyConnectionSettings.md) (одна запись, пароль зашифрован ключом из `MTR_SECRET_KEY`), экран «Подключение к MultiReestr» с формой параметров, «Проверить подключение» и «Сохранить и применить». Пул `legacy` пересобирается без перезапуска: бин `legacy` — всегда существующая обёртка над сменяемым Hikari-пулом. `legacy.datasource.*` и переменные окружения — запасной вариант для первого старта.
   - Развёртывание на Windows 10/11 по [deploy-windows.md](deploy-windows.md): production-jar с Mac, JDK 21, отдельный PostgreSQL 16 на `:5435` для main, `reestr_ro` на живой БД 9.6, `java -jar`; скрипты PowerShell в `dev/deploy/`.

2. **DTO-модель извещения и поиск.**
   - Материал урока: [lessons/lesson-02-notice-search.md](lessons/lesson-02-notice-search.md).
   - Value object `NoticeNumber`: нормализация номера (пробелы, «№»), юнит-тесты.
   - `NoticeLookupService` находит запись и выбирает оригинал среди дублей и копий (голден-тесты на 10 номерах).
   - DTO `OrganizationDto`, `NoticeCandidateDto`, `NoticeDto`, `LotDto`, `SpecPositionDto` (с КТРУ), `NoticeDocumentDto`.
   - Экран поиска на Flow UI: по номеру, а без номера — по дате и названию.

3. **DTO протоколов, контракта и исполнения; карточка и Excel.**
   - DTO `ProtocolDto`, `ContractDto`, `ContractItemDto`, `ExecutionDto` (приёмки, оплаты). Связи идут по тексту номера.
   - Карточка извещения с вкладками по блокам, переходы через `ViewNavigators`.
   - Выгрузка в Excel: свой экспорт на Apache POI, два листа, как в `notices.xlsx`.

4. **Производственный календарь.**
   - Собственный календарь, без аддона Business Calendar (он требует Enterprise).
   - `CalendarDay` хранит только исключения; по умолчанию Сб и Вс — выходные.
   - Интерфейс `WorkingCalendar` повторяет API аддона.
   - Кнопка «Загрузить год» на списке календаря: за указанный год качает XML с xmlcalendar.ru или берёт его из файла (для закрытого контура) и заменяет исключения года в одной транзакции. Перед записью показывает сводку; повторная загрузка безопасна.
   - Годы тестового набора данных загружаются по умолчанию из CSV в репо (Liquibase), без обращения в интернет; CSV генерирует скрипт `dev/calendar/`.
   - Тесты на переносах.

5. **Движок сроков (ст. 191–193 ГК).**
   - Материал урока: [lessons/lesson-05-deadlines.md](lessons/lesson-05-deadlines.md).
   - `DeadlineCalculator` (пакет `deadline`) поверх `WorkingCalendar`: `deadline(event, n, unit)`, `deadline(anchor, spec)`, `nextBusinessDayIfNonWorking`, `compare`. Формула — уже существующий embeddable `DeadlineSpec` (якорь, смещение, календарные или рабочие дни или месяцы, `startNextDay`, перенос, сравнение). Новых сущностей и экранов нет.
   - Ст. 191: при `startNextDay=true` N единиц после дня события (`plusDays` / `calendar.plus` / `plusMonths`). Ст. 192: месяцы — то же число или последний день месяца. Ст. 193: пустой `shiftRule` → `NEXT_WORKDAY` для календарных дней и месяцев, `NONE` для рабочих.
   - Пример «пятница/суббота» оформлен как тест; отдельно — рабочая суббота-перенос, майские, Новый год (первый рабочий день по seed `CalendarDay`), конец месяца, `PREVIOUS_WORKDAY`.
   - Замечание 1 — константы из модели (ЛБО в legacy нет). Замечания 6 и 9 — настоящие даты из тестового набора (данных) через `NoticeCardService`.

6. **Справочники и правила; роли.**
   - Материал урока: [lessons/lesson-06-rules.md](lessons/lesson-06-rules.md).
   - Шаблон (`RuleTemplateType` + Java-бин) → правило → версия с датами действия → параметры; серьёзность, ссылка на норму. Evaluator пишет `CheckResultDto` в память, без `CheckRun` / `Violation` (урок 7).
   - Справочные списки и matcher (префикс, исключение, период); импорт/экспорт Excel (замена после подтверждения). Кнопки загрузки из legacy `okpd2_1875_zap` нет.
   - Сейчас **3 из 10** правил аудита: `R03_PP1875` (ПП 1875 прил. 1), `R05_KTRU_CHARS` (ПП 145, лишние характеристики КТРУ), `R07_NO_DESCRIPTION` (ст. 33 44-ФЗ). Остальные семь — следующие уроки.
   - Редактор правил: без удаления (флаг `enabled`), «Новая версия», активация без пересечения периодов, архив; блок `DeadlineSpec` только для шаблонов срока.
   - `@ResourceRole` специалиста и администратора правил (стек с `ui-minimal` и `notice-viewer`; `SpecificPolicy` активации и импорта). Демо-пользователи `specialist` / `rule-admin` — Liquibase 052 с `context="demo"`; на Windows `main.liquibase.contexts=!demo`. Обязательные данные — 050 (списки) и 051 (правила).

7. **Проверка по запросу и реестр нарушений.**
   - Материал урока: [lessons/lesson-07-checks.md](lessons/lesson-07-checks.md).
   - Кнопка «Проверить» на карточке: `NoticeRuleEvaluator` на загруженном `NoticeDto`, диалог `CheckResultDto`, журнал `CheckRun` `ON_DEMAND` (пишется в момент кнопки). «Сохранить в реестр» — upsert `Violation` по fingerprint.
   - Реестр: статусы, «принято к сведению», ложные срабатывания с комментарием, пояснения со ссылкой на норму. `FIXED` только если правило в этом запуске оценивалось (enabled, ACTIVE на дату, есть бин). Счётчик NEW в меню — урок 9.

8. **Ручные данные: условия контрактов, даты ЛБО/ПГ.**
   - Материал урока: [lessons/lesson-08-manual-data.md](lessons/lesson-08-manual-data.md).
   - `ContractTerm`: сроки поставки, приёмки и оплаты по формуле `DeadlineSpec` или конкретной датой.
   - `PlanScheduleEvent`: даты ЛБО и утверждения плана-графика, с подсказкой из `PlanScheduleDto`.

9. **Плановое сканирование (Quartz) и оповещение в приложении.**
   - Материал урока: [lessons/lesson-09-scheduled.md](lessons/lesson-09-scheduled.md).
   - Проверка по расписанию через Jmix Quartz, в первую очередь сроков поставки и приёмки (`ContractTerm` + `DeadlineCalculator` / `DeadlineAnchorResolver`) и правил по извещениям за окно публикации. Журнал `CheckRun` `SCHEDULED`, реестр `Violation` через общий `ViolationRegistryService` (тот же merge, что у кнопки «Проверить»).
   - Оповещение — только реестр нарушений и счётчик новых (NEW) нарушений в меню.
   - Почту (аддон Email Sending, `io.jmix.email`) не реализуем, только рассказываем.

10. **Dry-run правил на исторических данных; финал.**
    - Материал урока: [lessons/lesson-10-dry-run.md](lessons/lesson-10-dry-run.md).
    - Пробный прогон DRAFT-версии на старых данных и сравнение с действующей версией (новые, без изменений, исчезнувшие). В реестр ничего не пишется.
    - Сквозной сценарий на эталонных данных, сборка и развёртывание на машине пользователя: обновление установки по [deploy-windows.md](deploy-windows.md) (первая установка — в уроке 1): рядом с MultiReestr (PG 9.6, `:5432`) работает отдельный PostgreSQL 16 на порту `:5435` (БД `multireestr`) для main; legacy подключается через `reestr_ro`.

## Не используем

- **Business Calendar** — требует Enterprise; вместо него свой `CalendarDay` + `WorkingCalendar`.
- **Notifications** — платный; оповещение в приложении — реестр нарушений и счётчик в меню.
- **Audit (entity log)** — достаточно трейтов аудита ядра (`createdBy`/`lastModifiedBy`), истории `RuleVersion` и `Violation.status`.
- **Email Sending** — только обзор как возможное расширение.
