#!/usr/bin/env bash
# Legacy-БД MultiReestr (PostgreSQL 9.6) для разработки.
# Использование: dev/legacy/legacy-db.sh [up|down|status|reset]
set -euo pipefail

NAME=reestr-pg96
VOLUME=reestr-pg96
PORT=5435
DB=reestrdb
RO_USER=reestr_ro
RO_PASS=reestr_ro
DIR="$(cd "$(dirname "$0")" && pwd)"
DUMP="$DIR/reestrdb.dump"

exists()  { docker ps -a --format '{{.Names}}' | grep -qx "$NAME"; }
running() { docker ps --format '{{.Names}}' | grep -qx "$NAME"; }

wait_ready() {
  for _ in $(seq 1 30); do
    docker exec "$NAME" pg_isready -U postgres -q && return 0
    sleep 2
  done
  echo "PostgreSQL не поднялся" >&2; exit 1
}

restore() {
  [ -f "$DUMP" ] || { echo "Нет дампа: $DUMP" >&2; exit 1; }
  echo "Восстанавливаю $DB из дампа..."
  docker exec "$NAME" createdb -U postgres "$DB"
  docker cp "$DUMP" "$NAME:/tmp/reestrdb.dump"
  docker exec "$NAME" pg_restore -U postgres -O -x -d "$DB" /tmp/reestrdb.dump
  docker exec "$NAME" rm /tmp/reestrdb.dump
  docker exec -i "$NAME" psql -U postgres -d "$DB" -v ON_ERROR_STOP=1 -q <<SQL
CREATE ROLE $RO_USER LOGIN PASSWORD '$RO_PASS';
GRANT CONNECT ON DATABASE $DB TO $RO_USER;
GRANT USAGE ON SCHEMA public TO $RO_USER;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO $RO_USER;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO $RO_USER;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO $RO_USER;
SQL
}

up() {
  docker info >/dev/null 2>&1 || { echo "Docker не запущен" >&2; exit 1; }
  if running; then
    echo "Уже запущен"
  elif exists; then
    docker update --restart unless-stopped "$NAME" >/dev/null
    docker start "$NAME" >/dev/null; wait_ready
  else
    docker run -d --name "$NAME" --restart unless-stopped \
      -e POSTGRES_PASSWORD=postgres -p "$PORT:5432" \
      -v "$VOLUME:/var/lib/postgresql/data" postgres:9.6 >/dev/null
    wait_ready; sleep 2
  fi
  if ! docker exec "$NAME" psql -U postgres -Atc "select 1 from pg_database where datname='$DB'" | grep -q 1; then
    restore
  fi
  status
}

down()   { exists && docker stop "$NAME" >/dev/null; echo "Остановлен"; }

reset() {
  exists && docker rm -f "$NAME" >/dev/null
  docker volume rm "$VOLUME" >/dev/null 2>&1 || true
  up
}

status() {
  if running; then
    n=$(docker exec "$NAME" psql -U "$RO_USER" -d "$DB" -Atc "select count(*) from notices" 2>/dev/null || echo "?")
    echo "Запущен: jdbc:postgresql://localhost:$PORT/$DB ($RO_USER), извещений: $n"
  else
    echo "Не запущен"
  fi
}

case "${1:-up}" in
  up) up ;; down) down ;; status) status ;; reset) reset ;;
  *) echo "Использование: $0 [up|down|status|reset]" >&2; exit 1 ;;
esac
