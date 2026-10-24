SELECT id,
       finyear,
       oosversion,
       publishdate,
       approvedate
FROM planzakuppackets
ORDER BY finyear DESC NULLS LAST, oosversion DESC NULLS LAST, id
