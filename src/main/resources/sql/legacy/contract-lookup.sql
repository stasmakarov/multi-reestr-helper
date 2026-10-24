SELECT r.id,
       trim(r.contractnumber) AS contract_number,
       trim(r.tendernumber) AS purchase_number,
       r.reestrnumber,
       r.ikz,
       r.contractdate,
       r.contractdateend,
       r.contractcena,
       NULL::numeric AS reduction_pct,
       o.id AS customer_id,
       o.name AS customer_name,
       o.fullname AS customer_full_name,
       o.ownerinn AS customer_inn,
       o.ownerkpp AS customer_kpp,
       w.id AS supplier_id,
       w.name AS supplier_name,
       w.fullname AS supplier_full_name,
       w.ownerinn AS supplier_inn,
       w.ownerkpp AS supplier_kpp
FROM reestr r
LEFT JOIN owners o ON o.id = r.ownerid
LEFT JOIN owners w ON w.id = r.winnerid
WHERE coalesce(r.isdeleted, 0) = 0
  AND (
        (CAST(:id AS integer) IS NOT NULL AND r.id = CAST(:id AS integer))
        OR (:contract_number IS NOT NULL AND trim(r.contractnumber) ILIKE :contract_number ESCAPE '\')
        OR (:purchase_number IS NOT NULL AND trim(r.tendernumber) ILIKE :purchase_number ESCAPE '\')
      )
ORDER BY r.contractdate DESC NULLS LAST, r.id DESC
LIMIT 50
