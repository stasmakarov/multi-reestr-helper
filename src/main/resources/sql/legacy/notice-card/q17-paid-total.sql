SELECT coalesce(sum(e.paid) FILTER (WHERE e.executiontype = 1), 0) AS paid_total
FROM reestrprocexecutions e
JOIN reestrprocedures p ON p.id = e.reestrproceduresid
WHERE p.reestrid = :rid
