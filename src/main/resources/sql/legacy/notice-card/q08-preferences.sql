SELECT p.preftype, p.prefcode, coalesce(min(s.shortname), 'код ' || p.prefcode) AS pref_name
FROM noticepurchaseprefs p
LEFT JOIN sppurchasepreferences s ON s.oosid = p.prefcode
WHERE p.noticeid = :nid
GROUP BY p.preftype, p.prefcode
ORDER BY min(p.id)
