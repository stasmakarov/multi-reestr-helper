WITH keys AS (
    SELECT trim(n.purchasenumber) AS pn,
           row_number() OVER (ORDER BY max(n.docpublishdate) DESC, trim(n.purchasenumber)) AS rn
    FROM notices n
    WHERE n.purchasenumber IS NOT NULL
      AND (CAST(:date AS date) IS NULL
           OR (n.docpublishdate >= CAST(:date AS date)
               AND n.docpublishdate < CAST(:date AS date) + 1))
      AND (CAST(:subject AS text) IS NULL OR n.predmet ILIKE '%' || CAST(:subject AS text) || '%' ESCAPE '\')
    GROUP BY trim(n.purchasenumber)
)
SELECT n.id, n.purchasenumber, n.docpublishdate, n.predmet,
       n.isdeleted, n.iscanceled, n.modifversion, n.srcguid,
       k.rn
FROM notices n
JOIN keys k ON trim(n.purchasenumber) = k.pn
WHERE k.rn <= 201
ORDER BY n.id
