<#
.SYNOPSIS
  Запускает multi-reestr через java -jar из папки установки (по умолчанию — папка скрипта).
  Настройки берутся из config\application.properties рядом с jar и из переменных окружения.
.EXAMPLE
  .\start-app.ps1
  .\start-app.ps1 -JavaHome "C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot" -Xmx 1g
#>
param(
    [string]$AppDir = $PSScriptRoot,
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$Xmx = "1g"
)
$ErrorActionPreference = "Stop"
$java = if ($JavaHome) { Join-Path $JavaHome "bin\java.exe" } else { "java" }
$jar = Join-Path $AppDir "multi-reestr.jar"
if (-not (Test-Path $jar)) { throw "Не найден $jar" }
if (-not $env:MTR_SECRET_KEY) {
    Write-Warning "MTR_SECRET_KEY не задан: сохранить пароль legacy на экране настроек не получится."
}
Set-Location $AppDir
New-Item -ItemType Directory -Force -Path (Join-Path $AppDir "logs") | Out-Null
& $java "-Xmx$Xmx" "-Dfile.encoding=UTF-8" "-Duser.timezone=Europe/Moscow" -jar $jar
