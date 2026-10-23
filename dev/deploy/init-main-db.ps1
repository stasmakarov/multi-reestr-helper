<#
.SYNOPSIS
  Создаёт роль и БД multireestr для Jmix в PostgreSQL 16 (порт 5435).
.EXAMPLE
  .\init-main-db.ps1
  .\init-main-db.ps1 -PgBin "C:\Program Files\PostgreSQL\16\bin" -Port 5435
#>
param(
    [string]$PgBin = "C:\Program Files\PostgreSQL\16\bin",
    [string]$PgHost = "localhost",
    [int]$Port = 5435,
    [string]$Database = "multireestr",
    [string]$Owner = "multireestr"
)
$ErrorActionPreference = "Stop"
$psql = Join-Path $PgBin "psql.exe"
if (-not (Test-Path $psql)) { throw "Не найден $psql. Укажите -PgBin." }

$pwd1 = Read-Host "Пароль новой роли $Owner" -AsSecureString
$plain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($pwd1))
$escaped = $plain.Replace("'", "''")

Write-Host "Подключаюсь к ${PgHost}:$Port под postgres (psql спросит пароль суперпользователя)..."
$exists = & $psql -h $PgHost -p $Port -U postgres -d postgres -Atc "select 1 from pg_roles where rolname='$Owner'"
if ($exists -ne "1") {
    & $psql -h $PgHost -p $Port -U postgres -d postgres -v ON_ERROR_STOP=1 -c "CREATE ROLE $Owner LOGIN PASSWORD '$escaped'"
} else {
    & $psql -h $PgHost -p $Port -U postgres -d postgres -v ON_ERROR_STOP=1 -c "ALTER ROLE $Owner PASSWORD '$escaped'"
}
$dbExists = & $psql -h $PgHost -p $Port -U postgres -d postgres -Atc "select 1 from pg_database where datname='$Database'"
if ($dbExists -ne "1") {
    & $psql -h $PgHost -p $Port -U postgres -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE $Database OWNER $Owner ENCODING 'UTF8' TEMPLATE template0"
}
Write-Host "Готово: jdbc:postgresql://${PgHost}:$Port/$Database ($Owner). Таблицы создаст Liquibase при первом старте."
