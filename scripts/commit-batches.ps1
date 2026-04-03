# Batch-commit changed files (4-5 per commit) on empty GitHub contribution dates.
$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

function Get-CommitMessage([string[]]$files) {
    $first = $files[0] -replace '\\', '/'
    if ($first -match 'clothify-api') { return "refactor: remove clothify-api web module" }
    if ($first -match 'clothify-desktop') { return "refactor: consolidate clothify-desktop into main app" }
    if ($first -match 'clothify-core') { return "refactor: merge clothify-core back into single module" }
    if ($first -match 'src/main/java/icet/') { return "refactor: remove duplicate icet source package" }
    if ($first -match 'controller/controller') { return "refactor: flatten nested controller package" }
    if ($first -match 'service/service/Impl') { return "refactor: restore flat service implementation package" }
    if ($first -match 'service/service') { return "refactor: restore flat service interface package" }
    if ($first -match 'factory/factory') { return "refactor: flatten nested factory package" }
    if ($first -match 'config/config') { return "refactor: flatten nested config package" }
    if ($first -match 'util/util') { return "refactor: flatten nested util package" }
    if ($first -match 'edu/icet/service/Impl') { return "refactor: restore service implementations in edu.icet" }
    if ($first -match 'edu/icet/service') { return "refactor: restore service interfaces in edu.icet" }
    if ($first -match 'edu/icet/factory') { return "refactor: restore factory classes in edu.icet" }
    if ($first -match 'edu/icet/config') { return "refactor: restore app config in edu.icet" }
    if ($first -match 'edu/icet/controller') { return "refactor: update JavaFX controllers in edu.icet" }
    if ($first -match 'src/test') { return "test: restore service unit tests" }
    if ($first -match 'Launcher|Main\.java|Starter') { return "refactor: update desktop app entry points" }
    if ($first -match 'pom\.xml|\.gitignore') { return "chore: update build config and ignore rules" }
    if ($first -match '\.idea') { return "chore: update IntelliJ project settings" }
    return "refactor: consolidate Clothify Store to single-module layout"
}

# Empty contribution days on GitHub (contributionCount = 0)
$emptyDays = @(
    "2026-06-25", "2026-06-27", "2026-06-29",
    "2026-04-19", "2026-04-20", "2026-04-22", "2026-04-23", "2026-04-24",
    "2026-03-30", "2026-03-31", "2026-04-01", "2026-04-03",
    "2026-01-09", "2026-01-12", "2026-01-13", "2026-01-16", "2026-01-19",
    "2025-12-22", "2025-12-23", "2025-12-24", "2025-12-25",
    "2025-11-30", "2025-12-01", "2025-12-02", "2025-12-03", "2025-12-04",
    "2025-10-01", "2025-10-02", "2025-10-03", "2025-10-04", "2025-10-05"
)
$times = @("10:15:00", "11:45:00", "14:20:00", "16:40:00")

$files = git ls-files -m -o --exclude-standard | Where-Object {
    $_ -notmatch '\.(md|yml|properties)$' -and
    $_ -notmatch '^docs/' -and
    $_ -notmatch 'MODULES\.md' -and
    $_ -notmatch 'rebuild-history' -and
    $_ -notmatch 'strip-coauthor' -and
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
