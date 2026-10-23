SELECT r.id,
       trim(r.contractnumber) AS contract_number,
       trim(r.tendernumber) AS purchase_number,
       r.reestrnumber,
       r.ikz,
       r.contractdate,
       r.contractdateend,
       r.contractcena
FROM reestr r
WHERE coalesce(r.isdeleted, 0) = 0
  AND (
        r.id = :id
        OR lower(trim(coalesce(r.contractnumber, ''))) LIKE :like
        OR trim(coalesce(r.tendernumber, '')) LIKE :like
      )
ORDER BY r.contractdate DESC NULLS LAST, r.id DESC
LIMIT 20
