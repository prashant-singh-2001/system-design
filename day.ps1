<#
.SYNOPSIS
    Runs one day of the 90-day system design curriculum.
.EXAMPLE
    .\day.ps1 7            # print Day 7's brief, then run Day 7's tests
    .\day.ps1 7 -Brief     # brief only
    .\day.ps1 7 -Test      # tests only
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateRange(1, 90)]
    [int]$Day,

    [switch]$Brief,
    [switch]$Test
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot

# The `java` on PATH is a stale JRE 8. Always go through JAVA_HOME.
if (-not $env:JAVA_HOME -or -not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    Write-Error "JAVA_HOME is not set to a valid JDK. This project needs JDK 21 or newer."
}

$modules = @(
    'phase-01-foundations', 'phase-02-solid', 'phase-03-patterns',
    'phase-04-lld', 'phase-05-databases', 'phase-06-caching',
    'phase-07-messaging', 'phase-08-reliability', 'phase-09-hld-capstone'
)

$phaseNum  = [math]::Ceiling($Day / 10)
$module    = $modules[$phaseNum - 1]
$dayLabel  = 'Day{0:D2}' -f $Day
$briefPath = Join-Path $root "$module\days\day-$('{0:D2}' -f $Day).md"

# Neither switch given means "do both".
$showBrief = $Brief -or -not ($Brief -or $Test)
$runTests  = $Test  -or -not ($Brief -or $Test)

if ($showBrief) {
    if (Test-Path $briefPath) {
        Write-Host ''
        Write-Host ('=' * 78) -ForegroundColor DarkGray
        Write-Host "  DAY $Day  --  $module" -ForegroundColor Cyan
        Write-Host ('=' * 78) -ForegroundColor DarkGray
        Get-Content $briefPath | Write-Host
        Write-Host ('=' * 78) -ForegroundColor DarkGray
        Write-Host ''
    }
    else {
        Write-Host "No brief yet for Day $Day." -ForegroundColor Yellow
        Write-Host "Ask Claude: 'generate phase $phaseNum' to write the briefs for these days." -ForegroundColor Yellow
        Write-Host "The full 90-day map is in CURRICULUM.md." -ForegroundColor Yellow
        if (-not $runTests) { return }
    }
}

if ($runTests) {
    Write-Host "Running $dayLabel tests in $module ..." -ForegroundColor Cyan
    & mvn -q -pl $module "-Dtest=$dayLabel*" '-DfailIfNoTests=false' '-Dsurefire.failIfNoSpecifiedTests=false' test
    if ($LASTEXITCODE -eq 0) {
        Write-Host ''
        Write-Host "  GREEN - Day $Day done. Write your NOTES.md, tick PROGRESS.md, commit." -ForegroundColor Green
    }
    else {
        Write-Host ''
        Write-Host "  RED - that is the starting line, not a problem. Open:" -ForegroundColor Yellow
        Write-Host "  $briefPath" -ForegroundColor Yellow
    }

    # Propagate the result so `day.cmd` and any wrapping script see a real exit code.
    exit $LASTEXITCODE
}
