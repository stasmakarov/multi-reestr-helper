<#
.SYNOPSIS
  Альтернатива установщику: PostgreSQL 16 для Jmix в Docker Desktop на порту 5435
  (только БД; приложение по-прежнему java -jar). Полный стек app+postgres:
  см. docs/deploy-docker.md (docker compose в корне репозитория).
  Внимание: Docker Desktop стартует только после входа пользователя в Windows.
  Не поднимайте этот контейнер одновременно с compose-службой postgres (порт 5435).
.EXAMPLE
  .\main-db-docker.ps1
#>
param(
    [int]$Port = 5435,
    [string]$Name = "multireestr-helper-pg16",
    [string]$Volume = "multireestr-helper-pg16"
)
$ErrorActionPreference = "Stop"
docker info *> $null
if ($LASTEXITCODE -ne 0) { throw "Docker не запущен" }
$s = Read-Host "Пароль роли multireestr" -AsSecureString
$plain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($s))
$existing = docker ps -a --format "{{.Names}}" | Where-Object { $_ -eq $Name }
if ($existing) {
    docker start $Name | Out-Null
} else {
    docker run -d --name $Name --restart unless-stopped `
        -e POSTGRES_DB=multireestr -e POSTGRES_USER=multireestr -e "POSTGRES_PASSWORD=$plain" `
        -p "127.0.0.1:${Port}:5432" -v "${Volume}:/var/lib/postgresql/data" postgres:16 | Out-Null
}
Write-Host "PostgreSQL 16: jdbc:postgresql://localhost:$Port/multireestr (multireestr)"
