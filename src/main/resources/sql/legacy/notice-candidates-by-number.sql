SELECT n.id, n.purchasenumber, n.docpublishdate, n.predmet,
       n.isdeleted, n.iscanceled, n.modifversion, n.srcguid
FROM notices n
WHERE trim(n.purchasenumber) = :pn
   OR regexp_replace(coalesce(n.docnumber,''),'[^0-9]','','g') = :pn
   OR trim(n.internalnumber) = :pn
ORDER BY n.id
