#!/usr/bin/env bash
# Генерирует src/test/resources/legacy/reestr-golden-fixture.sql из legacy-БД MultiReestr.
# Использование:
#   dev/legacy/make-test-fixture.sh [out.sql]
# По умолчанию читает контейнер reestr-pg96 (dev/legacy/legacy-db.sh up), роль reestr_ro.
# Другой источник: PSQL="psql -h host -p port -U user -d reestrdb" dev/legacy/make-test-fixture.sh
# Только чтение: выполняются исключительно SELECT. Выгружаются только таблицы из dev/legacy/grant-readonly.sql.
#
# Персональные данные обезличиваются при выгрузке (повторный запуск даёт тот же результат):
#   физлица — это 12-значные ИНН в owners.ownerinn, reestr.winnerinn;
#   N = номер такого ИНН по возрастанию (1..k) среди выгруженных строк;
#   ИНН            -> фиктивный корректный 12-значный ИНН: lpad(N, 10, '0') + две контрольные цифры;
#   ФИО (owners.name/fullname, reestr.winnername/winnerfio) -> «Поставщик-физлицо N»;
#   reestr.winnerfio непустой у любого поставщика -> «Поставщик-физлицо N» (N по winnerinn или ИНН владельца winnerid, иначе 0);
#   адреса и телефон поставщика-физлица (reestr.winneradress1..3, winnerphone) -> NULL.
set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
OUT="${1:-$DIR/../../src/test/resources/legacy/reestr-golden-fixture.sql}"
PSQL="${PSQL:-docker exec -i -e PGPASSWORD=reestr_ro reestr-pg96 psql -h localhost -U reestr_ro -d reestrdb}"
IDS="${IDS:-3,4,10,17,18,21,33,37,38,40,41,42}"
NUMS="SELECT trim(purchasenumber) FROM notices WHERE id IN ($IDS)"
RIDS="SELECT id FROM reestr WHERE trim(tendernumber) IN ($NUMS)"

q() { $PSQL -X -q -At -v ON_ERROR_STOP=1 -c "$1"; }

OWNERS_WHERE="id IN (SELECT ownerid FROM notices WHERE id IN ($IDS)) OR id IN (SELECT winnerid FROM reestr WHERE id IN ($RIDS))"

# Физлица: 12-значные ИНН среди выгружаемых строк, пронумерованные по возрастанию.
PII="SELECT inn, row_number() OVER (ORDER BY inn) AS n FROM (
       SELECT trim(ownerinn) AS inn FROM owners WHERE $OWNERS_WHERE
       UNION SELECT trim(winnerinn) FROM reestr WHERE id IN ($RIDS)) x
     WHERE inn ~ '^[0-9]{12}\$'"
pii_n() { echo "(SELECT p.n FROM ($PII) p WHERE p.inn = trim($1))"; }
# Фиктивный ИНН с правильными контрольными цифрами для номера N.
fake_inn() {
  echo "(SELECT b || c11 || ((SELECT sum(substr(b || c11, i, 1)::int * (ARRAY[3,7,2,4,10,3,5,9,4,6,8])[i])
            FROM generate_series(1, 11) i) % 11 % 10)
     FROM (SELECT b, ((SELECT sum(substr(b, i, 1)::int * (ARRAY[7,2,4,10,3,5,9,4,6,8])[i])
            FROM generate_series(1, 10) i) % 11 % 10)::text AS c11
           FROM (SELECT lpad(($1)::text, 10, '0') AS b) b0) b1)"
}
anon_inn()  { echo "CASE WHEN $(pii_n "$1") IS NULL THEN $1 ELSE $(fake_inn "$(pii_n "$1")") END"; }
anon_name() { echo "CASE WHEN $(pii_n "$2") IS NULL THEN $1 ELSE 'Поставщик-физлицо ' || $(pii_n "$2") END"; }
anon_drop() { echo "CASE WHEN $(pii_n "$2") IS NULL THEN $1 END"; }

# Выражение значения колонки в INSERT (с обезличиванием).
colexpr() {
  local t=$1 c=$2
  case "$t.$c" in
    owners.ownerinn)              anon_inn ownerinn ;;
    owners.name|owners.fullname)  anon_name "$c" ownerinn ;;
    reestr.winnerinn)             anon_inn winnerinn ;;
    reestr.winnername)            anon_name winnername winnerinn ;;
    reestr.winneradress1|reestr.winneradress2|reestr.winneradress3|reestr.winnerphone)
                                  anon_drop "$c" winnerinn ;;
    reestr.winnerfio)
      echo "CASE WHEN nullif(trim(winnerfio), '') IS NULL THEN winnerfio ELSE 'Поставщик-физлицо ' || coalesce(
              $(pii_n winnerinn), (SELECT $(pii_n o.ownerinn) FROM owners o WHERE o.id = reestr.winnerid), 0) END" ;;
    *) echo "$c" ;;
  esac
}

# table | columns filter (пусто = все) | WHERE
TABLES=(
  "notices||id IN ($IDS)"
  "owners|id,name,fullname,ownerinn,ownerkpp|$OWNERS_WHERE"
  "spdogovortypes||true"
  "noticeprocedureinfo||noticeid IN ($IDS)"
  "noticelots||noticeid IN ($IDS)"
  "noticecustomerrequirements||noticeid IN ($IDS)"
  "noticepurchaseprefs||noticeid IN ($IDS)"
  "sppurchasepreferences||oosid IN (SELECT prefcode FROM noticepurchaseprefs WHERE noticeid IN ($IDS))"
  "noticeextdocs||noticeid IN ($IDS)"
  "noticespec||noticeid IN ($IDS)"
  "noticektruharacts||noticeid IN ($IDS)"
  "noticektruharactvalues||noticektruharactid IN (SELECT id FROM noticektruharacts WHERE noticeid IN ($IDS))"
  "noticefinances||noticeid IN ($IDS)"
  "protocol_types||true"
  "protocols||trim(purchasenumber) IN ($NUMS)"
  "reestr||id IN ($RIDS)"
  "reestrspec||reestrid IN ($RIDS)"
  "reestrprocedures||reestrid IN ($RIDS)"
  "reestrprocexecutions||reestrproceduresid IN (SELECT id FROM reestrprocedures WHERE reestrid IN ($RIDS))"
)

{
  echo "-- Тестовый набор данных legacy-БД MultiReestr (PostgreSQL 9.6-совместимо)."
  echo "-- Сгенерировано из reestrdb.dump скриптом dev/legacy/make-test-fixture.sh, $(date '+%Y-%m-%d %H:%M %Z')."
  echo "-- Извещения notices.id IN ($IDS) + связанные строки (noticeid / noticektruharactid),"
  echo "-- заказчики (owners: только id,name,fullname,ownerinn,ownerkpp), весь spdogovortypes,"
  echo "-- используемые sppurchasepreferences; для урока 3: noticefinances, protocols (по номеру) + весь protocol_types,"
  echo "-- reestr (по tendernumber, включая удалённые), reestrspec, reestrprocedures, reestrprocexecutions;"
  echo "-- owners также по reestr.winnerid. userinfo и учётные данные не выгружаются."
  echo "-- Персональные данные обезличены: ФИО физлиц -> «Поставщик-физлицо N», 12-значные ИНН -> фиктивные."
  echo "-- Перегенерировать: dev/legacy/legacy-db.sh up && dev/legacy/make-test-fixture.sh"
  echo
  for spec in "${TABLES[@]}"; do
    IFS='|' read -r t cols where <<<"$spec"
    colfilter="true"
    [ -n "$cols" ] && colfilter="a.attname = ANY (string_to_array('$cols', ','))"
    attrs="FROM pg_attribute a JOIN pg_class c ON c.oid = a.attrelid JOIN pg_namespace n ON n.oid = c.relnamespace
           WHERE n.nspname = 'public' AND c.relname = '$t' AND a.attnum > 0 AND NOT a.attisdropped AND $colfilter"
    echo "DROP TABLE IF EXISTS $t;"
    echo "CREATE TABLE $t ("
    q "SELECT string_agg('    ' || quote_ident(a.attname) || ' ' || format_type(a.atttypid, a.atttypmod)
              || CASE WHEN a.attnotnull THEN ' NOT NULL' ELSE '' END, E',\n' ORDER BY a.attnum) $attrs"
    echo ");"
    collist=$(q "SELECT string_agg(quote_ident(a.attname), ', ' ORDER BY a.attnum) $attrs")
    valexpr=""
    for c in $(q "SELECT quote_ident(a.attname) $attrs ORDER BY a.attnum"); do
      [ -n "$valexpr" ] && valexpr="$valexpr || ', ' || "
      valexpr="${valexpr}quote_nullable($(colexpr "$t" "$c"))"
    done
    q "SELECT s FROM (SELECT 'INSERT INTO $t ($collist) VALUES (' || $valexpr || ');' AS s FROM $t WHERE $where) x ORDER BY s COLLATE \"C\""
    echo
  done
} > "$OUT.tmp"
mv "$OUT.tmp" "$OUT"
echo "Записано: $OUT ($(grep -c '^INSERT' "$OUT") INSERT)"
