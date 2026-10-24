<#
.SYNOPSIS
  Пересборка контейнера app из текущего дерева исходников (IDE / git checkout).
  Том PostgreSQL 16 не трогает. Нужен .env в корне репозитория.
.EXAMPLE
  .\rebuild-docker.ps1
  # или из корня репо:
  .\dev\deploy\rebuild-docker.ps1
#>
$ErrorActionPreference = "Stop"
$Root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
Set-Location $Root
if (-not (Test-Path (Join-Path $Root ".env"))) {
    throw "Нет файла .env в $Root. Скопируйте .env.example и заполните MTR_SECRET_KEY и POSTGRES_PASSWORD (docs/deploy-docker.md)."
}
docker info *> $null
if ($LASTEXITCODE -ne 0) { throw "Docker не запущен" }

docker compose up -d postgres
if ($LASTEXITCODE -ne 0) { throw "Не удалось запустить postgres" }
docker compose up -d --wait postgres *> $null
if ($LASTEXITCODE -ne 0) {
    $ready = $false
    for ($i = 0; $i -lt 36; $i++) {
        docker compose exec -T postgres pg_isready -q *> $null
        if ($LASTEXITCODE -eq 0) { $ready = $true; break }
        Start-Sleep -Seconds 2
    }
    if (-not $ready) { throw "PostgreSQL 16 не поднялся (служба postgres)." }
}

$buildArgs = @()
if (Get-Command git -ErrorAction SilentlyContinue) {
    if (Test-Path (Join-Path $Root ".git")) {
        $buildArgs += "--build-arg"
        $buildArgs += "GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD)"
        $buildArgs += "--build-arg"
        $buildArgs += "GIT_COMMIT=$(git rev-parse --short HEAD)"
        $buildArgs += "--build-arg"
        $buildArgs += "GIT_COMMIT_TIME=$(git log -1 --format=%cI)"
    }
}

docker compose build @buildArgs app
if ($LASTEXITCODE -ne 0) { throw "docker compose build не удался" }
docker compose up -d --no-deps --force-recreate app
if ($LASTEXITCODE -ne 0) { throw "docker compose up не удался" }
Write-Host "Готово: http://localhost:8080 (образ из $Root, БД на :5435 не сбрасывалась)."
