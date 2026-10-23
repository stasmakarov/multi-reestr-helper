WITH keys AS (
    SELECT trim(n.purchasenumber) AS pn
    FROM notices n
    WHERE n.purchasenumber IS NOT NULL
      AND (CAST(:date AS date) IS NULL OR n.docpublishdate::date = CAST(:date AS date))
      AND (CAST(:subject AS text) IS NULL OR n.predmet ILIKE '%' || CAST(:subject AS text) || '%' ESCAPE '\')
    GROUP BY trim(n.purchasenumber)
    ORDER BY max(n.docpublishdate) DESC, trim(n.purchasenumber)
    LIMIT 200
)
SELECT n.id, n.purchasenumber, n.docpublishdate, n.predmet,
       n.isdeleted, n.iscanceled, n.modifversion, n.srcguid
FROM notices n
JOIN keys k ON trim(n.purchasenumber) = k.pn
ORDER BY n.id
