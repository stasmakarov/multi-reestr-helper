#!/usr/bin/env bash
# Пересборка контейнера app из текущего дерева исходников (IDE / git checkout).
# Том PostgreSQL 16 не трогает. Нужен .env в корне репозитория.
# Использование: dev/deploy/rebuild-docker.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

if [[ ! -f .env ]]; then
  echo "Нет файла .env в $ROOT" >&2
  echo "Скопируйте .env.example → .env и заполните MTR_SECRET_KEY и POSTGRES_PASSWORD." >&2
  echo "См. docs/deploy-docker.md" >&2
  exit 1
fi

docker info >/dev/null 2>&1 || { echo "Docker не запущен" >&2; exit 1; }

build_args=()
if command -v git >/dev/null 2>&1 && [[ -d .git ]]; then
  build_args+=(
    --build-arg "GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD)"
    --build-arg "GIT_COMMIT=$(git rev-parse --short HEAD)"
    --build-arg "GIT_COMMIT_TIME=$(git log -1 --format=%cI)"
  )
fi

# Только app пересобирается из Dockerfile + src; postgres и его том остаются.
docker compose build "${build_args[@]}" app
docker compose up -d --no-deps --force-recreate app

echo "Готово: http://localhost:8080 (образ из $ROOT, БД на :5435 не сбрасывалась)."
