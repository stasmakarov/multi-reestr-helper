SELECT s.id, row_number() OVER (ORDER BY s.id) AS pos_no, s.lotid, s.articlename, s.okpdcode,
       s.okpd4ktru,
       CASE WHEN coalesce(s.ktrusuffixcode, 0) <> 0
            THEN coalesce(nullif(trim(s.okpd4ktru), ''), s.okpdcode) || '-' || lpad(s.ktrusuffixcode::text, 8, '0')
       END AS ktru_code,
       s.kol_vo, s.okeicode, s.unit, s.cena, s.summa,
       s.isprohibitionforeignpo, s.isrestrictforeignpo, s.ispreferenserfpo,
       s.isimposibilityprohibition, s.reasonimposibilityprohibition
FROM noticespec s
WHERE s.noticeid = :nid
ORDER BY s.id
