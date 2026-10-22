# Эталонные извещения для голден-теста выбора оригинала

Эталон для `NoticeLookupGoldenTest` (данные теста — `src/test/resources/legacy/notice-golden.csv`).
Тест передаёт каждый номер в `NoticeLookupService.findByNumber` и проверяет, что `chosen=true`
стоит у ожидаемого id, а число кандидатов совпадает.

Источник: тестовый дамп `dev/legacy/reestrdb.dump` (40 извещений, 38 номеров). Ожидаемый
оригинал вычислен SQL-запросом по правилу Q2 из [notice_lookup_spec.md](notice_lookup_spec.md):
не удалён → не локальная копия → не отменён → больший `modifversion` → более поздняя публикация → меньший id.

| Номер | Записи (id) | Кандидатов | Ожидаемый оригинал | Что проверяет |
|---|---|---|---|---|
| `0173100017025000006` | 33 (удалена), 41 | 2 | **41** | удалённая проигрывает |
| `0173100017026000015` | 3, 42 (локальная копия) | 2 | **3** | локальная копия проигрывает |
| `0173100017025000015` | 21 (редакция 3) | 1 | **21** | несколько редакций |
| `0173100017026000008` | 10 (редакция 2) | 1 | **10** | несколько редакций |
| `0173100017024000008` | 40 | 1 | **40** | 2024 год |
| `0173100017024000010` | 38 | 1 | **38** | 2024 год |
| `0173100017025000001` | 37 | 1 | **37** | одиночное |
| `0173100017025000020` | 18 | 1 | **18** | одиночное |
| `0173100017026000001` | 17 | 1 | **17** | одиночное |
| `0173100017026000014` | 4 | 1 | **4** | одиночное |

## Ограничения дампа

- Дублей по номеру всего два, отменённых извещений нет.
- Правила «отменено проигрывает», «равные редакции → более поздняя публикация» и «→ меньший id»
  проверяются юнит-тестами `NoticeOriginalSelectorTest` на синтетических данных.
- При загрузке новых данных в MultiReestr стоит добавить в эталон отменённое извещение и
  извещение с несколькими записями-редакциями.

## Как пересчитать эталон

```sql
with c as (
  select id, trim(purchasenumber) pn,
         coalesce(isdeleted,0)<>0 del,
         (srcguid like '{%' or predmet ~* '^\s*Копия') cp,
         coalesce(iscanceled,0)<>0 can,
         modifversion mv, docpublishdate pd
  from notices)
select pn, count(*) candidates,
       (select id from c c2 where c2.pn = c.pn
        order by del, cp, can, mv desc nulls last, pd desc nulls last, id limit 1) original
from c group by pn order by count(*) desc, pn;
```

```bash
docker exec -i reestr-pg96 psql -U reestr_ro -d reestrdb < query.sql
```

## Урок 3: протоколы, контракт, исполнение

Эталон для тестов карточки урока 3 (финансирование Q7, протоколы Q12, контракт Q13–Q14, исполнение Q16–Q17).
Посчитан на полном дампе `reestrdb` и совпадает с результатом на тестовой фикстуре
`src/test/resources/legacy/reestr-golden-fixture.sql` (сгенерирована `dev/legacy/make-test-fixture.sh`).
Для каждого номера извещение взято из таблицы выше (ожидаемый оригинал).

Правила:

- финансирование — строки `noticefinances` по `noticeid` оригинала, сумма `summa`;
- протоколы — по тексту номера: `trim(protocols.purchasenumber) = номер`;
- контракт (Q13) — `trim(reestr.tendernumber) = номер`, не удалён (`coalesce(isdeleted,0) = 0`),
  самый поздний `contractdate` (NULL в конце), затем больший `id`;
- снижение, % — `round((1 - contractcena / firstmaxcena) * 100, 1)` (НМЦК из извещения-оригинала);
- поставщик — `owners` по `reestr.winnerid`;
- документы приёмки — `executiontype <> 1`, включая документы с нулевой суммой; оплачено — `sum(paid)` по `executiontype = 1`.

| Номер | Извещение | Финанс. строк | Финанс. сумма | Протоколов | Контракт id | Номер контракта | Цена | Снижение, % | ИНН поставщика | Позиций | Приёмок | Оплачено |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `0173100017024000008` | 40 | 1 | 1744800.00 | 0 | 3 | `0173100017024000008` | 1744800.00 | 0.0 | `7708826943` | 2 | 12 | 1744800.00 |
| `0173100017024000010` | 38 | 1 | 799980.00 | 0 | — | — | — | — | — | 0 | 0 | 0 |
| `0173100017025000001` | 37 | 1 | 560000.00 | 2 | 8 | `0173100017025000001` | 445200.00 | 20.5 | `000000000184` | 1 | 1 | 445200.00 |
| `0173100017025000006` | 41 | 1 | 420000.00 | 0 | — | — | — | — | — | 0 | 0 | 0 |
| `0173100017025000015` | 21 | 1 | 252700.00 | 1 | 15 | `0173100017025000015` | 252700.00 | 0.0 | `9701105424` | 8 | 1 | 252700.00 |
| `0173100017025000020` | 18 | 1 | 800000.00 | 2 | 18 | `0173100017025000020` | 800000.00 | 0.0 | `000000000321` | 1 | 1 | 800000.00 |
| `0173100017026000001` | 17 | 1 | 560000.00 | 2 | 19 | `0173100017026000001` | 347200.00 | 38.0 | `5526006170` | 1 | 2 | 347200.00 |
| `0173100017026000008` | 10 | 1 | 175000.00 | 2 | 26 | `0173100017026000008` | 93050.00 | 46.8 | `7721129652` | 2 | 1 | 93050.00 |
| `0173100017026000014` | 4 | 1 | 33000.00 | 2 | 29 | `0173100017026000014` | 33000.00 | 0.0 | `3662046958` | 2 | 1 | 33000.00 |
| `0173100017026000015` | 3 | 1 | 43360.20 | 2 | 30 | `0173100017026000015` | 16000.00 | 63.1 | `000000000258` | 1 | 1 | 16000.00 |

Наблюдения:

- у `0173100017024000010` и `0173100017025000006` контракта нет — карточка должна показывать пустой блок «Контракт» без ошибки;
- среди контрактов по эталонным номерам удалённых нет (`isdeleted <> 0` — 0 строк), поэтому правило «не удалён» на этих данных не срабатывает — его проверяет юнит-тест;
- у `0173100017024000008` 12 документов приёмки при одной оплате на всю сумму — хороший случай для проверки разделения приёмок и оплат;
- `reestroplat` по этим контрактам пуст, `members` — 1 строка.

Запрос, которым посчитан эталон:

```sql
WITH g(pn, nid) AS (VALUES
  ('0173100017025000006',41),('0173100017026000015',3),('0173100017025000015',21),('0173100017026000008',10),
  ('0173100017024000008',40),('0173100017024000010',38),('0173100017025000001',37),('0173100017025000020',18),
  ('0173100017026000001',17),('0173100017026000014',4)),
c AS (
  SELECT g.pn, g.nid,
         (SELECT r.id FROM reestr r WHERE trim(r.tendernumber) = g.pn AND coalesce(r.isdeleted,0) = 0
          ORDER BY r.contractdate DESC NULLS LAST, r.id DESC LIMIT 1) AS rid
  FROM g)
SELECT c.pn, c.nid,
       (SELECT count(*) FROM noticefinances f WHERE f.noticeid = c.nid) AS fin_rows,
       (SELECT coalesce(sum(f.summa),0) FROM noticefinances f WHERE f.noticeid = c.nid) AS fin_sum,
       (SELECT count(*) FROM protocols p WHERE trim(p.purchasenumber) = c.pn) AS protocols,
       (SELECT count(*) FROM reestr r WHERE trim(r.tendernumber) = c.pn) AS reestr_all,
       c.rid, trim(r.contractnumber) AS contract_number, r.contractcena,
       round((1 - r.contractcena / nullif(n.firstmaxcena,0)) * 100, 1) AS reduction_pct,
       w.ownerinn AS supplier_inn,
       (SELECT count(*) FROM reestrspec s WHERE s.reestrid = c.rid) AS items,
       (SELECT count(*) FILTER (WHERE e.executiontype <> 1) FROM reestrprocexecutions e
          JOIN reestrprocedures p ON p.id = e.reestrproceduresid WHERE p.reestrid = c.rid) AS acceptance_docs,
       (SELECT coalesce(sum(e.paid) FILTER (WHERE e.executiontype = 1), 0) FROM reestrprocexecutions e
          JOIN reestrprocedures p ON p.id = e.reestrproceduresid WHERE p.reestrid = c.rid) AS paid_total
FROM c
LEFT JOIN reestr r ON r.id = c.rid
LEFT JOIN notices n ON n.id = c.nid
LEFT JOIN owners w ON w.id = r.winnerid
ORDER BY c.pn
```
