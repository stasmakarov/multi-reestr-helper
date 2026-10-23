<#
.SYNOPSIS
  Создаёт (или обновляет) роль reestr_ro на живой БД MultiReestr (PostgreSQL 9.6)
  и выдаёт SELECT только на таблицы из sql\grant-readonly.sql. Повторный запуск безопасен.
.EXAMPLE
  .\create-legacy-ro.ps1
  .\create-legacy-ro.ps1 -Port 5432 -Database reestrdb -GrantSql .\sql\grant-readonly.sql
#>
param(
    [string]$PgBin = "C:\Program Files\PostgreSQL\16\bin",
    [string]$PgHost = "localhost",
    [int]$Port = 5432,
    [string]$Database = "reestrdb",
    [string]$Role = "reestr_ro",
    [string]$Admin = "postgres",
    [string]$GrantSql = (Join-Path $PSScriptRoot "sql\grant-readonly.sql")
)
$ErrorActionPreference = "Stop"
$psql = Join-Path $PgBin "psql.exe"
if (-not (Test-Path $psql)) { throw "Не найден $psql. Укажите -PgBin (подойдёт psql от PostgreSQL 16)." }
if (-not (Test-Path $GrantSql)) { throw "Не найден $GrantSql (копия dev/legacy/grant-readonly.sql)." }
$env:PGCLIENTENCODING = "UTF8"

$server = & $psql -h $PgHost -p $Port -U $Admin -d $Database -Atc "show server_version"
Write-Host "Сервер ${PgHost}:$Port/$Database, версия $server"

$sec = Read-Host "Пароль роли $Role" -AsSecureString
$plain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($sec))
$escaped = $plain.Replace("'", "''")

$exists = & $psql -h $PgHost -p $Port -U $Admin -d $Database -Atc "select 1 from pg_roles where rolname='$Role'"
if ($exists -ne "1") {
    & $psql -h $PgHost -p $Port -U $Admin -d $Database -v ON_ERROR_STOP=1 -c "CREATE ROLE $Role LOGIN PASSWORD '$escaped'"
} else {
    & $psql -h $PgHost -p $Port -U $Admin -d $Database -v ON_ERROR_STOP=1 -c "ALTER ROLE $Role LOGIN PASSWORD '$escaped'"
}
& $psql -h $PgHost -p $Port -U $Admin -d $Database -v ON_ERROR_STOP=1 -c "GRANT CONNECT ON DATABASE $Database TO $Role"
& $psql -h $PgHost -p $Port -U $Admin -d $Database -X -q -v ON_ERROR_STOP=1 -f $GrantSql
if ($LASTEXITCODE -ne 0) { throw "grant-readonly.sql завершился с ошибкой" }

$env:PGPASSWORD = $plain
$n = & $psql -h $PgHost -p $Port -U $Role -d $Database -Atc "select count(*) from notices"
Remove-Item Env:PGPASSWORD
Write-Host "Готово: $Role видит notices ($n строк). Запись запрещена: проверьте кнопкой «Проверить подключение» в приложении."
