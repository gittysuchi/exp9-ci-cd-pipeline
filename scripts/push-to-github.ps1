<#
  Pushes this project to GitHub from the VS Code terminal (Windows PowerShell).

  Usage (from the project folder):
      .\scripts\push-to-github.ps1                                   # asks for the repo URL (or creates the repo with the GitHub CLI)
      .\scripts\push-to-github.ps1 -RepoUrl https://github.com/<user>/<repo>.git
      .\scripts\push-to-github.ps1 -Message "my commit message"
#>
param(
    [string]$RepoUrl = "",
    [string]$Message = "Experiment 9: CI/CD pipeline - build, test, dockerize and canary release",
    [string]$Branch  = "main",
    [string]$RepoName = "exp9-ci-cd-pipeline"
)

$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    throw "Git is not installed. Install it from https://git-scm.com/downloads and reopen VS Code."
}

# 1. repository
if (-not (Test-Path ".git")) {
    Write-Host ">> git init" -ForegroundColor Cyan
    git init -b $Branch | Out-Null
}

# 2. commit
Write-Host ">> git add / commit" -ForegroundColor Cyan
git add -A
git diff --cached --quiet
if ($LASTEXITCODE -ne 0) {
    git commit -m $Message
} else {
    Write-Host "   nothing new to commit"
}
git branch -M $Branch

# 3. remote
$remotes = @(git remote)
if ($remotes -notcontains "origin") {
    $hasGh = [bool](Get-Command gh -ErrorAction SilentlyContinue)
    if (-not $RepoUrl -and $hasGh) {
        gh auth status *> $null
        if ($LASTEXITCODE -eq 0) {
            Write-Host ">> Creating GitHub repo '$RepoName' with the GitHub CLI and pushing" -ForegroundColor Cyan
            gh repo create $RepoName --public --source . --remote origin --push
            Write-Host "Done. Open the Actions tab of your repo to watch the pipeline run." -ForegroundColor Green
            exit 0
        }
    }
    if (-not $RepoUrl) {
        $RepoUrl = Read-Host "Paste your GitHub repository URL (https://github.com/<user>/<repo>.git)"
    }
    git remote add origin $RepoUrl
}

# 4. push
Write-Host ">> git push" -ForegroundColor Cyan
git push -u origin $Branch
Write-Host "Done. Open the Actions tab of your repo to watch the pipeline run." -ForegroundColor Green
