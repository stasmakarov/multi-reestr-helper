SELECT e.id,
       e.executiontype,
       p.ordinalnumber,
       p.stageenddate,
       p.penaltyinfo,
       e.pd_documentname,
       e.de_documentnum,
       e.pd_documentnum,
       e.de_documentdate,
       e.pd_documentdate,
       e.de_deliveryacceptdate,
       e.de_fulfilmentsum,
       e.paid,
       e.improperexecutiontext
FROM reestrprocexecutions e
JOIN reestrprocedures p ON p.id = e.reestrproceduresid
WHERE p.reestrid = :rid
ORDER BY coalesce(e.pd_documentdate, e.de_documentdate), e.id
