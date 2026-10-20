SELECT n.id, n.purchasenumber, n.docpublishdate, n.predmet,
       n.isdeleted, n.iscanceled, n.modifversion, n.srcguid
FROM notices n
WHERE (CAST(:date AS date) IS NULL OR n.docpublishdate::date = CAST(:date AS date))
  AND (CAST(:subject AS text) IS NULL OR n.predmet ILIKE '%' || CAST(:subject AS text) || '%' ESCAPE '\')
ORDER BY n.id
LIMIT 200
