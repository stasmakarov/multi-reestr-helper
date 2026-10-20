-- notice_lookup.sql — поиск извещения 44-ФЗ и связанного контракта в БД Reestr (reestrdb, PostgreSQL 9.6).
-- Только чтение. SQL совместим с сервером PostgreSQL 9.6; клиент psql >= 10 (используются \if и \gset).
-- Запуск:
--   psql -d reestrdb -v pn='0173100017026000015' -f notice_lookup.sql
--   psql -d reestrdb -v pn='' -v pdate='2026-01-21' -v pname='криминалист' -f notice_lookup.sql   -- поиск без номера
-- Переменные: pn (номер, можно с пробелами/«№»), pdate (YYYY-MM-DD, опц.), pname (фрагмент объекта закупки, опц.).
-- Между блоками id передаются через \gset (:nid — извещение, :rid — контракт).

\set ON_ERROR_STOP on
SET default_transaction_read_only = on;
BEGIN READ ONLY;
\if :{?pdate} \else \set pdate '' \endif
\if :{?pname} \else \set pname '' \endif

-- Q0. Нормализация номера: оставить только цифры; валидно ровно 19 цифр.
SELECT regexp_replace(coalesce(:'pn',''), '[^0-9]', '', 'g') AS pn_norm,
       regexp_replace(coalesce(:'pn',''), '[^0-9]', '', 'g') ~ '^[0-9]{19}$' AS pn_valid
\gset

-- Q1. Кандидаты-извещения (все строки, для диагностики дублей).
--     По номеру: purchasenumber (основной), запасной вариант — docnumber ('№…') / internalnumber.
--     Без номера: дата публикации (день) + фрагмент predmet.
SELECT n.id, n.purchasenumber, n.docpublishdate, n.predmet, n.isdeleted, n.iscanceled, n.modifversion, n.srcguid,
       (coalesce(n.isdeleted,0) = 0)                       AS not_deleted,
       (n.srcguid NOT LIKE '{%' AND n.predmet !~* '^\s*Копия') AS is_from_eis
FROM notices n
WHERE CASE WHEN :'pn_valid' THEN
          trim(n.purchasenumber) = :'pn_norm'
       OR regexp_replace(coalesce(n.docnumber,''),'[^0-9]','','g') = :'pn_norm'
       OR trim(n.internalnumber) = :'pn_norm'
      ELSE
          (:'pdate' = '' OR n.docpublishdate::date = nullif(:'pdate','')::date)
      AND (:'pname' = '' OR n.predmet ILIKE '%' || :'pname' || '%')
      AND (:'pdate' <> '' OR :'pname' <> '')
      END
ORDER BY n.id;

-- Q2. Выбор оригинала: не удалён → не копия (srcguid без '{', predmet не «Копия…») → не отменён
--     → больший modifversion → более поздняя публикация → меньший id.
SELECT n.id AS nid, trim(n.purchasenumber) AS npn
FROM notices n
WHERE CASE WHEN :'pn_valid' THEN
          trim(n.purchasenumber) = :'pn_norm'
       OR regexp_replace(coalesce(n.docnumber,''),'[^0-9]','','g') = :'pn_norm'
       OR trim(n.internalnumber) = :'pn_norm'
      ELSE
          (:'pdate' = '' OR n.docpublishdate::date = nullif(:'pdate','')::date)
      AND (:'pname' = '' OR n.predmet ILIKE '%' || :'pname' || '%')
      AND (:'pdate' <> '' OR :'pname' <> '')
      END
ORDER BY (coalesce(n.isdeleted,0) <> 0),
         (n.srcguid LIKE '{%' OR n.predmet ~* '^\s*Копия'),
         (coalesce(n.iscanceled,0) <> 0),
         n.modifversion DESC NULLS LAST,
         n.docpublishdate DESC NULLS LAST,
         n.id
LIMIT 1
\gset

-- Q3. Извещение + заказчик (owners через notices.ownerid — FK нет) + способ закупки.
SELECT n.id, trim(n.purchasenumber) AS purchase_number, n.docpublishdate, n.predmet,
       n.placingway_code,
       CASE n.placingway_code WHEN 'EAP20' THEN 'Электронный аукцион'
                              WHEN 'EZK20' THEN 'Запрос котировок в электронной форме'
                              WHEN 'EOK20' THEN 'Открытый конкурс в электронной форме'
                              ELSE n.placingway_code END AS placing_way_name,
       dt.id AS method_sp_id, dt.name AS method_sp_name,   -- notices.dogovortype = spdogovortypes.rel44type (код ЕИС), не id!
       CASE n.prikaz WHEN 1 THEN '44-ФЗ' WHEN 0 THEN '223-ФЗ' ELSE n.prikaz::text END AS law,
       n.firstmaxcena AS nmck, n.etp_code, n.etp_name, n.dochref, n.printformurl,
       o.id AS customer_id, o.name AS customer_short, o.fullname AS customer_full, o.ownerinn AS customer_inn, o.ownerkpp AS customer_kpp
FROM notices n
LEFT JOIN owners o ON o.id = n.ownerid
LEFT JOIN LATERAL (SELECT id, name FROM spdogovortypes d WHERE d.rel44type = n.dogovortype ORDER BY d.isvisible DESC, d.id LIMIT 1) dt ON true
WHERE n.id = :nid;

-- Q4. Сроки процедуры.
SELECT collectingstartdate, collectingenddate, biddingdate, summarizingdate
FROM noticeprocedureinfo WHERE noticeid = :nid;

-- Q5. Лоты.
SELECT id, lotnumber, maxprice, currencycode FROM noticelots WHERE noticeid = :nid ORDER BY lotnumber;

-- Q6. Требования заказчика: ИКЗ, позиция плана-графика, обеспечение, срок исполнения.
SELECT lotid, customerregnum, maxprice, ikz, pglotreestrnumber, plannumber,
       aguarantee_amount, aguarantee_part, cguarantee_part, gguarantee_amount, gguarantee_part,
       relativelen, nonrelativestartdate, nonrelativeenddate, warrantyterm
FROM noticecustomerrequirements WHERE noticeid = :nid ORDER BY id;

-- Q7. Финансирование.
SELECT lotid, kbk, finyear, summa FROM noticefinances WHERE noticeid = :nid ORDER BY id;

-- Q8. Преференции/ограничения (без DISTINCT справочник даёт дубли: sppurchasepreferences.oosid не уникален).
SELECT p.preftype, p.prefcode, coalesce(min(s.shortname), 'код ' || p.prefcode) AS pref_name
FROM noticepurchaseprefs p
LEFT JOIN sppurchasepreferences s ON s.oosid = p.prefcode
WHERE p.noticeid = :nid
GROUP BY p.preftype, p.prefcode ORDER BY min(p.id);

-- Q9. Документы извещения.
SELECT name, url FROM noticeextdocs WHERE noticeid = :nid ORDER BY id;

-- Q10. Спецификация. КТРУ = okpd4ktru || '-' || lpad(ktrusuffixcode, 8, '0'); суффикс 0/NULL → позиция не по КТРУ.
SELECT s.id, row_number() OVER (ORDER BY s.id) AS pos_no, s.lotid, s.articlename, s.okpdcode,
       CASE WHEN coalesce(s.ktrusuffixcode,0) <> 0
            THEN coalesce(nullif(trim(s.okpd4ktru),''), s.okpdcode) || '-' || lpad(s.ktrusuffixcode::text, 8, '0') END AS ktru_code,
       s.kol_vo, s.okeicode, s.unit, s.cena, s.summa
FROM noticespec s WHERE s.noticeid = :nid ORDER BY s.id;

-- Q11. Характеристики КТРУ по позициям (значение: текст | точное число | диапазон с знаками).
SELECT h.noticespecid, h.ktrucode, h.haractname,
       string_agg(coalesce(v.qualitydesc,
                           trim(trailing '.' from trim(trailing '0' from v.concretevalue::text)),
                           concat_ws(' ', v.minmathnotation, trim(trailing '.' from trim(trailing '0' from v.rangeminvalue::text)),
                                          v.maxmathnotation, trim(trailing '.' from trim(trailing '0' from v.rangemaxvalue::text)))),
                  ' | ' ORDER BY v.id) AS vals
FROM noticektruharacts h
LEFT JOIN noticektruharactvalues v ON v.noticektruharactid = h.id
WHERE h.noticeid = :nid
GROUP BY h.id, h.noticespecid, h.ktrucode, h.haractname ORDER BY h.id;

-- Q12. Протоколы (по тексту purchasenumber; тип 0 отсутствует в protocol_types).
SELECT p.id, p.reestrid, p.protocolnumber, p.protocoldate, p.protocoltype,
       coalesce(t.name, 'тип ' || p.protocoltype || ' (нет в protocol_types)') AS protocol_type_name, t.subtype
FROM protocols p
LEFT JOIN protocol_types t ON t.id = p.protocoltype
WHERE trim(p.purchasenumber) = :'npn'
ORDER BY p.protocolnumber;

-- Q13. Контракт (reestr.tendernumber = номер извещения; FK нет; prikaz=1 — 44-ФЗ) + поставщик (owners через winnerid).
--      Выбор: не удалён, затем самый поздний.
SELECT r.id AS rid FROM reestr r
WHERE trim(r.tendernumber) = :'npn' AND coalesce(r.isdeleted,0) = 0
ORDER BY r.contractdate DESC NULLS LAST, r.id DESC LIMIT 1
\gset
\if :{?rid} \else \set rid 0 \endif

SELECT r.id, trim(r.contractnumber) AS contract_number, r.contractdate, r.contractdateend, r.contractcena,
       round((1 - r.contractcena / nullif(n.firstmaxcena,0)) * 100, 1) AS reduction_pct,
       r.reestrnumber, r.ikz, r.prikaz,
       r.dogovortype, dt.name AS method_name,              -- reestr.dogovortype = spdogovortypes.id
       w.id AS supplier_id, w.name AS supplier_name, w.ownerinn AS supplier_inn, w.ownerkpp AS supplier_kpp
FROM reestr r
LEFT JOIN owners  w ON w.id = r.winnerid
LEFT JOIN notices n ON n.id = :nid
LEFT JOIN spdogovortypes dt ON dt.id = r.dogovortype
WHERE r.id = :rid;

-- Q14. Позиции контракта.
SELECT id, articlename, okpdcode, kol_vo, unit, cena, summa FROM reestrspec WHERE reestrid = :rid ORDER BY id;

-- Q15. Участники (members.reestrid указывает на reestr ИЛИ notreestr — здесь считаем reestr).
SELECT membername, memberinn, iswinner, membercena FROM members WHERE reestrid = :rid ORDER BY id;

-- Q16. Исполнение: приёмка (executiontype <> 1) и оплаты (executiontype = 1).
SELECT e.id, e.executiontype,
       CASE WHEN e.executiontype = 1 THEN 'payment' ELSE 'acceptance' END AS kind,
       e.de_documentnum, e.de_documentdate, e.de_fulfilmentsum,
       e.pd_documentname, e.pd_documentnum, e.pd_documentdate, e.paid
FROM reestrprocexecutions e
JOIN reestrprocedures p ON p.id = e.reestrproceduresid
WHERE p.reestrid = :rid
ORDER BY coalesce(e.pd_documentdate, e.de_documentdate), e.id;

-- Q17. Итог оплат.
SELECT coalesce(sum(e.paid) FILTER (WHERE e.executiontype = 1), 0) AS paid_total,
       count(*) FILTER (WHERE e.executiontype <> 1) AS acceptance_docs
FROM reestrprocexecutions e JOIN reestrprocedures p ON p.id = e.reestrproceduresid
WHERE p.reestrid = :rid;

-- Q18. Старая таблица оплат (в этой БД пустая, но может быть заполнена у других клиентов).
SELECT schetnumber, schetdate, summa, docname FROM reestroplat WHERE reestrid = :rid ORDER BY schetdate;

COMMIT;
