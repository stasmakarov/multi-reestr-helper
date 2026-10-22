SELECT p.id,
       p.protocolnumber,
       p.protocoldate,
       p.protocoltype,
       coalesce(t.name, 'тип ' || coalesce(p.protocoltype::text, '?') || ' (нет в справочнике)') AS protocol_type_name
FROM protocols p
LEFT JOIN protocol_types t ON t.id = p.protocoltype
WHERE trim(p.purchasenumber) = trim(:npn)
ORDER BY p.protocolnumber, p.id
