SELECT n.id, trim(n.purchasenumber) AS purchase_number, n.docpublishdate, n.predmet,
       n.modifversion, n.dogovortype, n.placingway_code,
       CASE n.placingway_code WHEN 'EAP20' THEN 'Электронный аукцион'
                              WHEN 'EZK20' THEN 'Запрос котировок в электронной форме'
                              WHEN 'EOK20' THEN 'Открытый конкурс в электронной форме'
                              ELSE n.placingway_code END AS placing_way_name,
       dt.id AS method_sp_id, dt.name AS method_sp_name,
       CASE n.prikaz WHEN 1 THEN '44-ФЗ' WHEN 0 THEN '223-ФЗ' ELSE n.prikaz::text END AS law,
       n.firstmaxcena AS nmck, n.etp_code, n.etp_name, n.dochref, n.printformurl,
       o.id AS customer_id, o.name AS customer_short, o.fullname AS customer_full,
       o.ownerinn AS customer_inn, o.ownerkpp AS customer_kpp
FROM notices n
LEFT JOIN owners o ON o.id = n.ownerid
LEFT JOIN LATERAL (
    SELECT id, name
    FROM spdogovortypes d
    WHERE d.rel44type = n.dogovortype
    ORDER BY d.isvisible DESC, d.id
    LIMIT 1
) dt ON true
WHERE n.id = :nid
