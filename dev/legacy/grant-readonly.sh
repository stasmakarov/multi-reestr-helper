#!/usr/bin/env bash
# Выдаёт роли reestr_ro SELECT только на таблицы из dev/legacy/grant-readonly.sql.
# Использование:
#   dev/legacy/grant-readonly.sh                                   # контейнер reestr-pg96, база reestrdb
#   PSQL="psql -h host -p port -U postgres -d reestrdb" dev/legacy/grant-readonly.sh
# Роль reestr_ro и GRANT CONNECT должны уже существовать (см. legacy-db.sh, docs/sql/legacy_readonly_user.sql).
set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
PSQL="${PSQL:-docker exec -i reestr-pg96 psql -U postgres -d reestrdb}"

$PSQL -X -q -v ON_ERROR_STOP=1 < "$DIR/grant-readonly.sql"
echo "Права reestr_ro обновлены по $DIR/grant-readonly.sql"
