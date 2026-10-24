SELECT id, lotid, kbk, finyear, summa
FROM noticefinances
WHERE noticeid = :nid
ORDER BY id
