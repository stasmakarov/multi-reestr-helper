SELECT collectingstartdate, collectingenddate, biddingdate::date AS biddingdate,
       summarizingdate::date AS summarizingdate
FROM noticeprocedureinfo
WHERE noticeid = :nid
