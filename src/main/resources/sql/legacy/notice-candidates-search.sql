WITH keys AS (
    SELECT trim(n.purchasenumber) AS pn,
           row_number() OVER (ORDER BY max(n.docpublishdate) DESC, trim(n.purchasenumber)) AS rn
    FROM notices n
    WHERE n.purchasenumber IS NOT NULL
      AND (CAST(:numberExact AS text) IS NULL
           OR trim(n.purchasenumber) = CAST(:numberExact AS text))
      AND (CAST(:numberLike AS text) IS NULL
           OR trim(n.purchasenumber) ILIKE CAST(:numberLike AS text) ESCAPE '\')
      AND (CAST(:fromDate AS date) IS NULL
           OR n.docpublishdate >= CAST(:fromDate AS date))
      AND (CAST(:toDate AS date) IS NULL
           OR n.docpublishdate < CAST(:toDate AS date) + 1)
      AND (CAST(:subject AS text) IS NULL
           OR n.predmet ILIKE '%' || CAST(:subject AS text) || '%' ESCAPE '\')
    GROUP BY trim(n.purchasenumber)
)
SELECT n.id, n.purchasenumber, n.docpublishdate, n.predmet,
       n.isdeleted, n.iscanceled, n.modifversion, n.srcguid,
       k.rn
FROM notices n
JOIN keys k ON trim(n.purchasenumber) = k.pn
WHERE k.rn > :offset
  AND k.rn <= :offset + :limit
  AND k.rn <= :cap
ORDER BY k.rn, n.id
