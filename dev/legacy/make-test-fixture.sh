#!/usr/bin/env bash
# Генерирует src/test/resources/legacy/reestr-golden-fixture.sql из legacy-БД MultiReestr.
# Использование:
#   dev/legacy/make-test-fixture.sh [out.sql]
# По умолчанию читает контейнер reestr-pg96 (dev/legacy/legacy-db.sh up), роль reestr_ro.
# Другой источник: PSQL="psql -h host -p port -U user -d reestrdb" dev/legacy/make-test-fixture.sh
# Только чтение: выполняются исключительно SELECT.
set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
OUT="${1:-$DIR/../../src/test/resources/legacy/reestr-golden-fixture.sql}"
PSQL="${PSQL:-docker exec -i -e PGPASSWORD=reestr_ro reestr-pg96 psql -h localhost -U reestr_ro -d reestrdb}"
IDS="${IDS:-3,4,10,17,18,21,33,37,38,40,41,42}"

q() { $PSQL -X -q -At -v ON_ERROR_STOP=1 -c "$1"; }

# table | columns filter (пусто = все) | WHERE
TABLES=(
  "notices||id IN ($IDS)"
  "owners|id,name,fullname,ownerinn,ownerkpp|id IN (SELECT ownerid FROM notices WHERE id IN ($IDS))"
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
)

{
  echo "-- Тестовая фикстура legacy-БД MultiReestr (PostgreSQL 9.6-совместимо)."
  echo "-- Сгенерировано из reestrdb.dump скриптом dev/legacy/make-test-fixture.sh, $(date '+%Y-%m-%d %H:%M %Z')."
  echo "-- Извещения notices.id IN ($IDS) + связанные строки (noticeid / noticektruharactid),"
  echo "-- заказчики (owners: только id,name,fullname,ownerinn,ownerkpp), весь spdogovortypes,"
  echo "-- используемые sppurchasepreferences. userinfo и учётные данные не выгружаются."
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
    valexpr=$(q "SELECT string_agg('quote_nullable(' || quote_ident(a.attname) || ')', ' || '', '' || ' ORDER BY a.attnum) $attrs")
    q "SELECT 'INSERT INTO $t ($collist) VALUES (' || $valexpr || ');' FROM $t WHERE $where ORDER BY 1"
    echo
  done
} > "$OUT.tmp"
mv "$OUT.tmp" "$OUT"
echo "Записано: $OUT ($(grep -c '^INSERT' "$OUT") INSERT)"
