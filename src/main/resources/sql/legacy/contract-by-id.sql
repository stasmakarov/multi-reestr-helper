SELECT r.id,
       trim(r.contractnumber) AS contract_number,
       trim(r.tendernumber) AS purchase_number,
       r.reestrnumber,
       r.ikz,
       r.contractdate,
       r.contractdateend,
       r.contractcena
FROM reestr r
WHERE r.id = :id
  AND coalesce(r.isdeleted, 0) = 0
