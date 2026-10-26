#!/usr/bin/env bash
# Сборка production-jar на Mac (или Linux) и папки для переноса на Windows.
# Использование: dev/deploy/build-jar.sh
# Результат: build/deploy/multi-reestr-helper/ — jar, скрипты PowerShell и SQL для reestr_ro.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
OUT="$ROOT/build/deploy/multi-reestr-helper"
cd "$ROOT"

# productionMode=true: фронтенд Vaadin собирается в jar, на Windows не нужны Node.js и dev-сервер.
./gradlew clean bootJar -Pvaadin.productionMode=true

JAR="$(ls build/libs/*.jar | grep -v -- '-plain.jar' | head -n 1)"
rm -rf "$OUT"
mkdir -p "$OUT/sql" "$OUT/config"
cp "$JAR" "$OUT/multi-reestr-helper.jar"
cp dev/deploy/*.ps1 "$OUT/"
cp dev/deploy/application-windows.properties.example "$OUT/config/"
cp dev/legacy/grant-readonly.sql "$OUT/sql/"

if jar tf "$OUT/multi-reestr-helper.jar" | grep -q vaadin-dev; then
  echo "Ошибка: production-jar содержит vaadin-dev" >&2
  exit 1
fi

echo "Готово: $OUT"
echo "Скопируйте папку на Windows-машину в C:\\multi-reestr-helper (см. docs/deploy-windows.md)."
