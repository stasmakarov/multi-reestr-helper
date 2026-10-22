SELECT s.id,
       s.articlename,
       s.okpdcode,
       CASE WHEN coalesce(s.ktrusuffixcode, 0) <> 0
            THEN coalesce(nullif(trim(s.okpd4ktru), ''), s.okpdcode) || '-' || lpad(s.ktrusuffixcode::text, 8, '0')
       END AS ktru_code,
       s.kol_vo,
       s.unit,
       s.cena,
       s.summa
FROM reestrspec s
WHERE s.reestrid = :rid
ORDER BY s.id
