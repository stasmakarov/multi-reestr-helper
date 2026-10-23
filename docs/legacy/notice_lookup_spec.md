# Спецификация: поиск извещения 44-ФЗ и связанного контракта в БД Reestr

Документ описывает логику, которой формировались `notices_report.md` / `notices.xlsx`. Её рабочая реализация — `notice_lookup.sql`: только чтение, SQL совместим с сервером PostgreSQL 9.6, но клиент psql нужен версии 10+ (`\if`, `\gset`). Клиент psql 9.6 не понимает `\if`; в приложении параметры передаются bind-переменными, а `\gset` заменяется чтением id из результата. Запросы проверены на живой копии `reestrdb` (`/tmp/pg96data`): для всех 10 номеров из отчёта id извещения, НМЦК, число позиций, контракт, цена, поставщик и сумма оплат совпали с `notices.xlsx`.

Исходные скрипты: `work/build.py` (генерация отчёта), `work/grep.sql` (поиск номера по всем столбцам), `work/q.py` (обёртка psql `--csv`, `default_transaction_read_only=on`).

---

## 1. Вход и нормализация

| Вход | Обязателен | Пример |
|---|---|---|
| `pn` — номер извещения | да, либо `pdate`/`pname` | `0173100017026000015`, `№0173100017026000015`, ` 0173100017025000005` |
| `pdate` — дата публикации (YYYY-MM-DD) | для поиска без номера | `2026-01-21` |
| `pname` — фрагмент объекта закупки | для поиска без номера | `криминалист` |

Правила:
1. **Нормализация:** `pn_norm = regexp_replace(pn, '[^0-9]', '', 'g')`. Удаляются пробелы (в том числе ведущие), «№», дефисы, пробелы-разделители.
2. **Валидность:** номер извещения ЕИС — **ровно 19 цифр** (`^[0-9]{19}$`). Структура: `01731000170` (11 цифр, код заказчика в ЕИС = `noticecustomerrequirements.customerregnum`) + `25`/`26` (год) + 6-значный порядковый номер. Невалидный номер → переход к поиску по дате и названию.
3. **Сравнение в БД всегда через `trim(...)`** с обеих сторон. В данных встречаются значения с ведущими пробелами: `reestr.contractnumber = ' 0173100017025000005'`, а у `notices.srcguid` (char(38)) — хвостовые пробелы.
4. **Где искать номер.** Основной столбец — `notices.purchasenumber`. Запасные: `notices.docnumber` (формат `№0173…`, сравнивать по цифрам) и `notices.internalnumber`. Он **не всегда** равен номеру: бывает `РЗ-2026.30.000044`.
5. **Поиск без номера:** `docpublishdate >= pdate AND docpublishdate < pdate + 1` и `predmet ILIKE '%pname%'`. Нужен хотя бы один критерий. Пример: 21.01.2026 + «криминалист» → единственное совпадение `0173100017026000003` (id 15). Если совпадений несколько, их надо отдать пользователю на выбор и не угадывать. Строки с `purchasenumber IS NULL` (черновики и локальные шаблоны MultiReestr, не опубликованные извещения) в поиск без номера **не попадают**: условие `purchasenumber IS NOT NULL` в `notice-candidates-by-date-subject.sql`.

Для диагностики можно искать номер по **всем** текстовым столбцам всех таблиц (`work/grep.sql`: генерируется `SELECT … WHERE col::text ~ '01731000170(25|26)'` по `pg_attribute` для типов varchar/text/bpchar). Результат на этой БД: номера есть только в `notices`, `protocols`, `reestr` (`tendernumber`, `contractnumber`, `contractprojectnumber`, `documentbase`), `reestrextdocs`, `reestrprocedureextdocs`, `logs.desctext`. В `notreestr`, `plangraf`, `notices223`, `planzakupok` и `pricerequests` их нет.

## 2. Алгоритм

```
pn → нормализация → [Q1] кандидаты из notices → [Q2] выбор оригинала → nid
nid → [Q3] извещение+заказчик → [Q4] сроки → [Q5] лоты → [Q6] требования заказчика
    → [Q7] финансирование → [Q8] преференции → [Q9] документы → [Q10] спецификация → [Q11] характеристики КТРУ
номер (npn) → [Q12] протоколы
номер (npn) → [Q13] контракт → rid → [Q14] позиции контракта → [Q15] участники
                              → [Q16] приёмка и оплаты (reestrprocedures → reestrprocexecutions) → [Q17] итоги → [Q18] reestroplat
```

### 2.1. Связи (FK в БД / только в коде)

| От | К | Ключ | FK в БД? |
|---|---|---|---|
| notices.ownerid | owners.id | заказчик | **нет** |
| notices.dogovortype | spdogovortypes.rel44type | способ закупки (код ЕИС) | **нет** |
| reestr.dogovortype | spdogovortypes.id | способ закупки | **нет** |
| noticelots.noticeid | notices.id | | да, CASCADE |
| noticecustomerrequirements.lotid | noticelots.id | | да |
| noticecustomerrequirements.noticeid | notices.id | денормализованная копия | **нет** |
| noticespec.lotid | noticelots.id | | да |
| noticespec.noticeid | notices.id | денормализованная копия; в запросах фильтр по ней | **нет** |
| noticefinances.noticecustomerrequirementsid | noticecustomerrequirements.id | | да |
| noticeprocedureinfo.noticeid | notices.id | | да |
| noticepurchaseprefs.lotid | noticelots.id | | да |
| noticepurchaseprefs.prefcode | sppurchasepreferences.oosid | справочник, `oosid` **не уникален** | **нет** |
| noticektruharacts.noticespecid | noticespec.id | | да |
| noticektruharactvalues.noticektruharactid | noticektruharacts.id | | да |
| protocols.purchasenumber | notices.purchasenumber | **текстовый** номер | **нет** |
| protocols.reestrid | reestr.id **или** notreestr.id | полиморфная ссылка | **нет** |
| reestr.tendernumber | notices.purchasenumber | **текстовый** номер, сравнивать через trim | **нет** |
| reestr.winnerid | owners.id | поставщик | **нет** |
| reestr.ownerid | owners.id | заказчик | **нет** |
| reestrspec.reestrid | reestr.id | | **нет** |
| members.reestrid | reestr.id **или** notreestr.id | полиморфная ссылка | **нет** |
| reestrprocedures.reestrid | reestr.id | | да |
| reestrprocexecutions.reestrproceduresid | reestrprocedures.id | | да |
| reestroplat.reestrid | reestr.id | | **нет** |

Между извещением и контрактом **нет числового ключа**: связь идёт только по строке номера (`reestr.tendernumber`, а также `contractnumber`/`contractprojectnumber`, которые в этих данных совпадают с номером извещения).

### 2.2. Фильтры и выбор оригинала при дублях

Дубли по `purchasenumber` есть у двух номеров:
- `0173100017026000015`: id 3 (оригинал из ЕИС) и id 42 «Копия 16 …» (`srcguid='{084C…}'`, `modifversion=0`, `internalnumber='РЗ-2026.30.000044'`, дата 07.10.2026).
- `0173100017025000006`: id 33 (`isdeleted=1`) и id 41.

Порядок сортировки кандидатов (берётся первая строка, Q2):
1. `coalesce(isdeleted,0) <> 0` — удалённые в конец (флаг int, бывает NULL);
2. признак локальной копии: `srcguid LIKE '{%'` (GUID в формате Delphi `{…}` создаёт сама программа; импорт из ЕИС — без фигурных скобок, с хвостовыми пробелами char(38)) **или** `predmet ~* '^\s*Копия'`;
3. `coalesce(iscanceled,0) <> 0` — отменённые в конец;
4. `modifversion DESC NULLS LAST`;
5. `docpublishdate DESC NULLS LAST`;
6. `id ASC`.

Остальные кандидаты возвращаются как предупреждение (`duplicates`).

Контракт (Q13): `trim(tendernumber) = npn AND coalesce(isdeleted,0)=0`, затем по `contractdate DESC, id DESC`. `prikaz` = 1 → 44-ФЗ, 0 → 223-ФЗ. У всех найденных `prikaz = 1`; для извещений 44-ФЗ можно дополнительно фильтровать `prikaz = 1`.

## 3. Блоки вывода (запросы — в `notice_lookup.sql`)

### Q3. Извещение и заказчик
```sql
SELECT n.id, trim(n.purchasenumber), n.docpublishdate, n.predmet, n.placingway_code, n.prikaz,
       n.firstmaxcena, n.etp_name, n.dochref, o.name, o.fullname, o.ownerinn, o.ownerkpp
FROM notices n LEFT JOIN owners o ON o.id = n.ownerid WHERE n.id = :nid;
```
| Поле вывода | Источник |
|---|---|
| number | `trim(purchasenumber)` |
| published_at | `docpublishdate` (timestamp без зоны, московское время) |
| subject | `predmet` |
| method.code | `placingway_code` (код ЕИС, `EAP20`) |
| method.name | `spdogovortypes.name`, где **`spdogovortypes.rel44type = notices.dogovortype`** (у извещения хранится внешний код ЕИС: 30 → id 19 «Электронный аукцион»). У контракта, наоборот, `reestr.dogovortype = spdogovortypes.id` (19). Один и тот же столбец `dogovortype` в двух таблицах имеет разный смысл. Запасной вариант — словарь по `placingway_code` (`EAP20` = «Электронный аукцион»). |
| law | `prikaz` 1/0 |
| nmck | `firstmaxcena` |
| platform | `etp_name` (формат различается: «РТС-тендер» / «ООО «РТС-тендер»») |
| customer | `owners.name/fullname/ownerinn/ownerkpp` |

### Q4. Сроки — `noticeprocedureinfo`
`collectingstartdate` → начало подачи заявок, `collectingenddate` → окончание, `biddingdate` → дата аукциона, `summarizingdate` → подведение итогов. `noticeprocedureinfo2` для этих извещений пуста.

### Q5–Q7. Лоты, требования заказчика, финансирование
- `noticelots`: `lotnumber`, `maxprice`, `currencycode`.
- `noticecustomerrequirements`: `ikz`; `pglotreestrnumber` (позиция плана-графика); `aguarantee_amount`/`aguarantee_part` (обеспечение заявки, руб./%); `cguarantee_part` (обеспечение контракта, %; `cguarantee_amount` часто 0); `gguarantee_*` (гарантийные обязательства); срок исполнения = `relativelen` дней с даты заключения, если он не 0, иначе `nonrelativeenddate`. Банковские реквизиты с нулями (`00000000000000000000`, БИК `000000000`) — заглушки, их надо игнорировать.
- `noticefinances`: `kbk`, `finyear`, `summa`.

### Q8. Преференции/ограничения
`noticepurchaseprefs.prefcode` (integer) = `sppurchasepreferences.oosid` (integer). В справочнике `oosid` повторяется (на один код до сотни строк), поэтому нужна группировка по `(preftype, prefcode)` и `min(shortname)`. Коды без расшифровки выводятся как «код N» (пример: `56116975`). Результат для 26-000015: `код 56116975; ET44; TR442`.

### Q9. Документы — `noticeextdocs.name`, `url` (ссылки на filestore ЕИС).

### Q10–Q11. Спецификация и КТРУ
`noticespec` (фильтр по `noticeid`, порядок по `id`): `articlename`, `okpdcode`, `kol_vo` (numeric(29,11) — хвостовые нули обрезать), `unit`, `okeicode`, `cena`, `summa`.

**Код КТРУ (производный):** если `coalesce(ktrusuffixcode,0) <> 0`, то `coalesce(nullif(trim(okpd4ktru),''), okpdcode) || '-' || lpad(ktrusuffixcode::text, 8, '0')`, иначе позиция не по КТРУ (NULL). Результат совпадает с `noticektruharacts.ktrucode`. Учесть, что `okpd4ktru` может отличаться от `okpdcode` (пример: 25-000005, позиция 1: ОКПД2 `26.20.21.120`, КТРУ `26.20.22.110-00000005`) — так в данных, не исправлять.

Характеристики: `noticektruharacts` (`haractname`) + `noticektruharactvalues`. Значение берётся по порядку: `qualitydesc` (текст) → `concretevalue` (число, обрезать нули) → диапазон `minmathnotation rangeminvalue maxmathnotation rangemaxvalue` (пример: `>= 220 < 260`). Несколько значений склеиваются через ` | `.

### Q12. Протоколы
`protocols` по `trim(purchasenumber) = npn`, порядок по `protocolnumber`. Тип: `protocol_types.name` по `protocoltype`. У протоколов `…-2` `protocoltype = 0`, а записи с id 0 в `protocol_types` нет → «тип 0 (нет в справочнике)». У `…-3` тип 12 «Протокол подведения итогов…» (subtype `EF2020FinalPart`). `protocoldate` может быть NULL (26-000015, `…-3`).

### Q13–Q14. Контракт, поставщик, позиции
`reestr`: `trim(contractnumber)`, `contractdate`, `contractdateend`, `contractcena`, `reestrnumber` (реестровый номер контракта), `ikz`. Снижение = `(1 - contractcena / notices.firstmaxcena) * 100`, округлить до 0,1.
Поставщик: `owners` по `reestr.winnerid` → `name`, `ownerinn`, `ownerkpp`. Поля `reestr.winnername/winnerinn` **пусты**, источник — только `owners`.
Позиции контракта: `reestrspec` по `reestrid` (наименование содержит «Товарный знак: …»; цена может иметь 11 знаков после запятой, например 5.22875816993).

### Q15. Участники
`members` по `reestrid = rid`. По всем 10 закупкам **пусто**: участники не загружены, известен только победитель. Учесть полиморфизм: `members.reestrid` может ссылаться на `notreestr`, и признака источника нет.

### Q16–Q18. Исполнение и оплаты
`reestrprocedures` (по `reestrid`) → `reestrprocexecutions`:
- `executiontype = 1` → **оплата**: `pd_documentname` («Платежное поручение»), `pd_documentnum`, `pd_documentdate`, `paid`;
- иначе (0) → **приёмка**: `de_documentnum`, `de_documentdate`, `de_fulfilmentsum`. Бывают записи приёмки на 0,00 (документы ОМТО у 25-000009, 26-000001) — выводить, но в сумму не включать.
Итог: `sum(paid) FILTER (WHERE executiontype=1)`; процент оплаты = `paid_total / contractcena`. `reestroplat` (старая схема оплат) в этой БД пуста, но запрос оставлен для совместимости.

## 4. Подводные камни

1. **Дата в запросе ≠ дата в БД.** Для 25-000005 пользователь указал 21.02.2025, а в БД `docpublishdate = 24.02.2025 09:16`. Сверять по номеру; дату использовать только как подсказку и показывать расхождение.
2. **Пробелы:** ведущие в `reestr.contractnumber`, хвостовые в char(38) (`srcguid`, `noticelots.srcguid`). Сравнивать через `trim`.
3. **GUID двух форматов** в одном char(38): `32A89D1F-…  ` (из ЕИС) и `{084C9949-…}` (создан программой). Это признак локальной копии.
4. **Дубли извещений** (копии «Копия N …», удалённые записи) — правило выбора в п. 2.2.
5. **Полиморфные `reestrid`** в `protocols`, `members`, `requests` (reestr или notreestr). Для протоколов надёжнее ключ `purchasenumber`.
6. **Нет FK** на основных связях (извещение ↔ контракт только по тексту номера; `reestrspec.reestrid`, `winnerid`, `ownerid`). Сироты возможны (в этой БД не найдены).
7. **Флаги int/smallint с NULL** (`isdeleted`, `iscanceled`): `coalesce(x,0)`. `iscanceled` у оригиналов часто NULL.
8. **`dogovortype` с разным смыслом:** в `notices` это код ЕИС (→ `spdogovortypes.rel44type`), в `reestr` — id справочника. Наивный `JOIN … ON id = notices.dogovortype` ничего не находит (id 30 нет). **Справочники неполные:** `protocoltype` 0 отсутствует в `protocol_types`; `prefcode` 56116975 отсутствует в `sppurchasepreferences`; `oosid` в справочнике не уникален.
9. **Участники не загружены** — блок может быть пустым при наличии контракта.
10. **Нулевые суммы приёмки** и несколько платежей на один контракт (25-000009: 2 приёмки на сумму, 2 оплаты).
11. **Временная зона:** timestamp без зоны, по данным — московское время.
12. **Технически (PG 9.6 на Win32):** системные представления (`pg_tables`, `pg_roles`) на Linux-копии не читаются, поэтому в коде использовать только пользовательские таблицы и `pg_class`/`pg_attribute`. Если номер не найден, `\gset` в Q2 завершает скрипт ошибкой «no rows returned» — в приложении это надо трактовать как «не найдено».

## 5. Структура результата (пример — 0173100017026000015, реальные значения)

```json
{
  "query": {"input": "0173100017026000015", "normalized": "0173100017026000015", "mode": "number"},
  "found": true,
  "notice": {
    "id": 3,
    "number": "0173100017026000015",
    "published_at": "2026-04-07T16:57:55+03:00",
    "subject": "Канцелярские принадлежности папки «Дело» без скоросшивателя, картонные",
    "law": "44-ФЗ",
    "method": {"code": "EAP20", "name": "Электронный аукцион"},
    "platform": "РТС-тендер",
    "nmck": 43360.20,
    "currency": "RUB",
    "customer": {"id": 3, "name": "МСК",
                 "full_name": "ВОЕННОЕ СЛЕДСТВЕННОЕ УПРАВЛЕНИЕ СЛЕДСТВЕННОГО КОМИТЕТА РОССИЙСКОЙ ФЕДЕРАЦИИ ПО ГОРОДУ МОСКВЕ",
                 "inn": "7714841940", "kpp": "771401001", "eis_code": "01731000170"},
    "ikz": "261771484194077140100100040040000244",
    "plan_position": "202601731000170001000016",
    "deadlines": {"applications_from": "2026-04-07T16:57:55+03:00", "applications_to": "2026-04-15T10:00:00+03:00",
                  "bidding": "2026-04-15", "summarizing": "2026-04-16"},
    "guarantees": {"application": {"amount": 433.60, "percent": 1.00}, "contract": {"percent": 5.00},
                   "warranty": {"amount": 4336.02, "percent": 10.00}},
    "execution_term_days": 30,
    "financing": [{"kbk": "41703018820090049244", "year": 2026, "amount": 43360.20}],
    "preferences": ["код 56116975", "ET44", "TR442"],
    "documents": ["1. Обоснование НМЦК папки картонные.docx", "2. Проект контракта папки картонные.docx",
                  "3. ТЗ папки картонные.docx", "4 Требования к содержанию, составу заявки на участие в закупке.docx"],
    "lots": [{"id": 3, "number": 1, "max_price": 43360.20, "currency": "RUB"}],
    "items": [{
      "pos": 1, "name": "Папка картонная", "okpd2": "17.23.13.193", "ktru": "17.23.13.193-00000002",
      "qty": 3060, "unit": "Штука", "okei": 796, "price": 14.17, "amount": 43360.20,
      "ktru_characteristics": [
        {"name": "Плотность картона, г/м2", "value": ">= 220 < 260"},
        {"name": "Тип", "value": "Папка-обложка без скоросшивателя"},
        {"name": "Формат", "value": "A4"},
        {"name": "Цвет", "value": "белый"},
        {"name": "Ширина корешка", "value": "25"}]
    }]
  },
  "duplicates": [{"id": 42, "reason": "локальная копия ('Копия 16 …', srcguid '{…}', modifversion 0)", "published_at": "2026-10-07"}],
  "protocols": [
    {"id": 73, "number": "0173100017026000015-2", "date": "2026-04-15", "type_code": 0, "type_name": null},
    {"id": 74, "number": "0173100017026000015-3", "date": null, "type_code": 12,
     "type_name": "Протокол подведения итогов определения поставщика (подрядчика, исполнителя)"}],
  "participants": [],
  "contract": {
    "id": 30, "number": "0173100017026000015", "date": "2026-04-27", "end_date": "2026-05-27",
    "price": 16000.00, "reduction_pct": 63.1, "registry_number": "1771484194026000013",
    "supplier": {"id": 24, "name": "Поставщик-физлицо 2", "inn": "000000000258"},
    "items": [{"name": "Папка картонная. Товарный знак: отсутствует", "okpd2": "17.23.13.193",
               "qty": 3060, "unit": "Штука", "price": 5.22875816993, "amount": 16000.00}],
    "acceptances": [{"doc_number": "ЯЕ26089", "date": "2026-05-08", "amount": 16000.00}],
    "payments": [{"doc": "Платежное поручение", "number": "8202", "date": "2026-05-13", "amount": 16000.00}],
    "paid_total": 16000.00, "paid_pct": 100
  },
  "warnings": ["есть дубль-копия извещения id=42", "участники не загружены"]
}
```

## 6. Запуск и проверка

```bash
psql -h /tmp/pg96sock -p 5496 -U postgres -d reestrdb -X -A -F'|' -v pn='0173100017026000015' -f notice_lookup.sql
psql ... -v pn='' -v pdate='2026-01-21' -v pname='криминалист' -f notice_lookup.sql
```
Проверено: нормализация `' 0173100017025000005'`, `'№0173100017025000005'`, `'0173 1000 1702 5000 005'` → id 32; дубль `0173100017025000006` → id 41 (не удалённый); `0173100017026000015` → id 3 (не копия); поиск без номера → id 15 (`0173100017026000003`); невалидный номер `123` → «не найдено».
