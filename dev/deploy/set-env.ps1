<#
.SYNOPSIS
  Сохраняет секреты приложения в переменные окружения текущего пользователя Windows
  (MAIN_DATASOURCE_PASSWORD, LEGACY_DATASOURCE_PASSWORD, MTR_SECRET_KEY).
  Новые значения видны в новых окнах PowerShell.
.EXAMPLE
  .\set-env.ps1               # спросит пароли; ключ шифрования сгенерирует, если его нет
#>
$ErrorActionPreference = "Stop"
function Read-Plain([string]$prompt) {
    $s = Read-Host $prompt -AsSecureString
    [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($s))
}
$main = Read-Plain "Пароль роли multireestr (PostgreSQL 16, :5435)"
[Environment]::SetEnvironmentVariable("MAIN_DATASOURCE_PASSWORD", $main, "User")

$legacy = Read-Plain "Пароль reestr_ro (MultiReestr 9.6) для первого старта, Enter — пропустить"
if ($legacy) { [Environment]::SetEnvironmentVariable("LEGACY_DATASOURCE_PASSWORD", $legacy, "User") }

$key = [Environment]::GetEnvironmentVariable("MTR_SECRET_KEY", "User")
if (-not $key) {
    $bytes = New-Object byte[] 32
    [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $key = ($bytes | ForEach-Object { $_.ToString("x2") }) -join ""
    [Environment]::SetEnvironmentVariable("MTR_SECRET_KEY", $key, "User")
    Write-Host "Сгенерирован MTR_SECRET_KEY. Сохраните его копию в надёжном месте: без него сохранённый пароль legacy не расшифровать."
} else {
    Write-Host "MTR_SECRET_KEY уже задан — оставлен без изменений."
}
Write-Host "Готово. Откройте новое окно PowerShell перед запуском start-app.ps1."
