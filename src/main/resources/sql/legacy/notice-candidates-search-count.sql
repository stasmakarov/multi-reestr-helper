SELECT count(*)
FROM (
    SELECT 1
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
) numbered
