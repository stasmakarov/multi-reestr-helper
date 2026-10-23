SELECT p.id,
       p.finyear,
       p.oosversion,
       p.publishdate,
       p.approvedate
FROM planzakuppackets p
ORDER BY p.finyear DESC NULLS LAST, p.oosversion DESC NULLS LAST, p.id DESC
