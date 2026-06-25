# Redistribute unpushed commit dates across empty GitHub contribution days.
$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

$base = git merge-base HEAD origin/feature/full-pos-system 2>$null
if (-not $base) { $base = "13eb005" }

$commits = @(git rev-list --reverse "$base..HEAD")
if ($commits.Count -eq 0) {
    Write-Host "No commits to redate."
    exit 0
}

$emptyDays = @(
    "2026-06-25", "2026-06-27", "2026-06-29",
    "2026-04-08", "2026-04-09", "2026-04-10", "2026-04-11", "2026-04-13",
    "2026-04-14", "2026-04-15", "2026-04-16", "2026-04-17", "2026-04-18",
    "2026-04-20", "2026-04-22", "2026-04-23", "2026-04-24",
    "2026-03-23", "2026-03-24", "2026-03-25", "2026-03-26", "2026-03-27",
    "2026-03-28", "2026-03-30", "2026-03-31", "2026-04-01",
    "2026-01-12", "2026-01-13", "2026-01-16", "2026-01-19"
)
$times = @("09:30:00", "11:00:00", "13:15:00", "15:45:00")

$branch = git branch --show-current
$tempBranch = "temp-redate-$(Get-Random)"
git branch $tempBranch
git checkout -B redate-work $base

$dayIdx = 0
$timeIdx = 0
$n = 0

foreach ($commit in $commits) {
    git cherry-pick $commit 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Cherry-pick failed for $commit"
        git cherry-pick --abort
        git checkout $branch
        git branch -D redate-work
        exit 1
    }

    $day = $emptyDays[$dayIdx % $emptyDays.Count]
    $time = $times[$timeIdx % $times.Count]
    $dateStr = "${day}T${time}+0530"

    $timeIdx++
    if ($timeIdx % $times.Count -eq 0) { $dayIdx++ }

    $env:GIT_COMMITTER_DATE = $dateStr
    git commit --amend --no-edit --date=$dateStr
    Remove-Item Env:GIT_COMMITTER_DATE -ErrorAction SilentlyContinue
    $n++
}

git checkout -B $branch
git branch -D redate-work
Write-Host "Redated $n commits across empty contribution days."
