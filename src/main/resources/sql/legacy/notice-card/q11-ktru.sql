SELECT h.id, h.noticespecid, h.ktrucode, h.haractcode, h.haractname,
       h.isfromoos, h.isrequired, h.haracttype, h.haractkind,
       string_agg(coalesce(v.qualitydesc,
                           trim(trailing '.' from trim(trailing '0' from v.concretevalue::text)),
                           concat_ws(' ', v.minmathnotation,
                                          trim(trailing '.' from trim(trailing '0' from v.rangeminvalue::text)),
                                          v.maxmathnotation,
                                          trim(trailing '.' from trim(trailing '0' from v.rangemaxvalue::text)))),
                  ' | ' ORDER BY v.id) AS vals
FROM noticektruharacts h
LEFT JOIN noticektruharactvalues v ON v.noticektruharactid = h.id
WHERE h.noticeid = :nid
GROUP BY h.id, h.noticespecid, h.ktrucode, h.haractcode, h.haractname,
         h.isfromoos, h.isrequired, h.haracttype, h.haractkind
ORDER BY h.id
