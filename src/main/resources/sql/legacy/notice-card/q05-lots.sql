SELECT id, lotnumber, maxprice, currencycode
FROM noticelots
WHERE noticeid = :nid
ORDER BY lotnumber
