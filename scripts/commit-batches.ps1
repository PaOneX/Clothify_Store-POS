# Batch-commit changed files (4-5 per commit) on empty GitHub contribution dates.
$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

function Get-CommitMessage([string[]]$files) {
    $first = $files[0] -replace '\\', '/'
    if ($first -match 'icet/model/dto') { return "feat: add shared core DTOs for online orders" }
    if ($first -match 'icet/model/enums') { return "feat: add core enums for orders and catalog" }
    if ($first -match 'icet/repository/Impl') { return "feat: add JDBC repository implementations in icet core" }
    if ($first -match 'icet/repository') { return "feat: add repository interfaces in icet core" }
    if ($first -match 'icet/service/Impl') { return "feat: add service implementations in icet core" }
    if ($first -match 'icet/service') { return "feat: add service layer in icet core module" }
    if ($first -match 'icet/factory') { return "feat: add factory wiring in icet core" }
    if ($first -match 'icet/db|icet/config|icet/exception|icet/util') { return "feat: add icet core infrastructure utilities" }
    if ($first -match 'controller/controller/auth') { return "refactor: move auth controller to controller package" }
    if ($first -match 'controller/controller/order') { return "refactor: move order controllers and size picker" }
    if ($first -match 'controller/controller') { return "refactor: reorganize JavaFX controllers package" }
    if ($first -match 'service/service/Impl') { return "refactor: move service implementations to service.service" }
    if ($first -match 'service/service') { return "refactor: move service interfaces to service.service" }
    if ($first -match 'factory/factory') { return "refactor: move factories to factory.factory package" }
    if ($first -match 'config/config') { return "refactor: move app config to config.config package" }
    if ($first -match 'util/util') { return "refactor: move utilities to util.util package" }
    if ($first -match 'Launcher|Main\.java|Starter') { return "refactor: update desktop app entry points" }
    if ($first -match 'pom\.xml|\.gitignore') { return "chore: update build config and ignore rules" }
    if ($first -match '\.idea') { return "chore: update IntelliJ project settings" }
    return "refactor: update Clothify Store module structure"
}

# Empty contribution days on GitHub (contributionCount = 0)
$emptyDays = @(
    "2026-06-25", "2026-06-27", "2026-06-29",
    "2026-04-08", "2026-04-09", "2026-04-10", "2026-04-11", "2026-04-13",
    "2026-04-14", "2026-04-15", "2026-04-16", "2026-04-17", "2026-04-18",
    "2026-04-20", "2026-04-22", "2026-04-23", "2026-04-24",
    "2026-03-23", "2026-03-24", "2026-03-25", "2026-03-26", "2026-03-27",
    "2026-03-28", "2026-03-30", "2026-03-31", "2026-04-01",
    "2026-01-12", "2026-01-13", "2026-01-16", "2026-01-19"
)
$times = @("10:15:00", "11:45:00", "14:20:00", "16:40:00")

$files = git ls-files -m -o --exclude-standard | Where-Object {
    $_ -notmatch '\.(md|yml|properties)$' -and
    $_ -notmatch '^docs/' -and
    $_ -notmatch 'MODULES\.md' -and
    $_ -notmatch 'rebuild-history' -and
    $_ -notmatch '\\target\\' -and
    $_ -notmatch '/target/'
} | Sort-Object

if ($files.Count -eq 0) {
    Write-Host "No files to commit."
    exit 0
}

Write-Host "Committing $($files.Count) files in batches of 4-5..."

$dayIdx = 0
$timeIdx = 0
$i = 0
$batchNum = 0

while ($i -lt $files.Count) {
    $batchSize = if (($batchNum % 2) -eq 0) { 5 } else { 4 }
    $batch = @($files[$i..([Math]::Min($i + $batchSize - 1, $files.Count - 1))])
    $i += $batch.Count
    $batchNum++

    $day = $emptyDays[$dayIdx % $emptyDays.Count]
    $time = $times[$timeIdx % $times.Count]
    $dateStr = "${day}T${time}+0530"

    $timeIdx++
    if ($timeIdx % $times.Count -eq 0) { $dayIdx++ }

    foreach ($f in $batch) {
        git add -- "$f"
    }

    $msg = Get-CommitMessage $batch
    $env:GIT_AUTHOR_DATE = $dateStr
    $env:GIT_COMMITTER_DATE = $dateStr
    git commit -m $msg --date=$dateStr
    Remove-Item Env:GIT_AUTHOR_DATE -ErrorAction SilentlyContinue
    Remove-Item Env:GIT_COMMITTER_DATE -ErrorAction SilentlyContinue

    Write-Host "[$dateStr] $msg ($($batch.Count) files)"
}

Write-Host "Done. Created $batchNum commits."
