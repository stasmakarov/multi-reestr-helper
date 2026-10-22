SELECT id, lotid, customerregnum, maxprice, ikz, pglotreestrnumber, plannumber,
       aguarantee_amount, aguarantee_part, cguarantee_part, gguarantee_amount, gguarantee_part,
       relativelen, nonrelativestartdate, nonrelativeenddate, warrantyterm
FROM noticecustomerrequirements
WHERE noticeid = :nid
ORDER BY id
