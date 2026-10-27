#!/usr/bin/env bash
# Pushes this project to GitHub from a bash terminal (Git Bash, WSL, macOS, Linux).
#
# Usage (from the project folder):
#   bash scripts/push-to-github.sh                                        # asks for the URL (or creates the repo with the GitHub CLI)
#   bash scripts/push-to-github.sh https://github.com/<user>/<repo>.git
#   bash scripts/push-to-github.sh https://github.com/<user>/<repo>.git "my commit message"
set -euo pipefail

REPO_URL="${1:-}"
MESSAGE="${2:-Experiment 9: CI/CD pipeline - build, test, dockerize and canary release}"
BRANCH="main"
REPO_NAME="exp9-ci-cd-pipeline"

cd "$(dirname "$0")/.."

command -v git >/dev/null || { echo "Git is not installed: https://git-scm.com/downloads"; exit 1; }

[[ -d .git ]] || { echo ">> git init"; git init -b "$BRANCH" >/dev/null; }

echo ">> git add / commit"
git add -A
if ! git diff --cached --quiet; then
  git commit -m "$MESSAGE"
else
  echo "   nothing new to commit"
fi
git branch -M "$BRANCH"

if ! git remote | grep -qx origin; then
  if [[ -z "$REPO_URL" ]] && command -v gh >/dev/null && gh auth status >/dev/null 2>&1; then
    echo ">> Creating GitHub repo '$REPO_NAME' with the GitHub CLI and pushing"
    gh repo create "$REPO_NAME" --public --source . --remote origin --push
    echo "Done. Open the Actions tab of your repo to watch the pipeline run."
    exit 0
  fi
  [[ -n "$REPO_URL" ]] || read -r -p "Paste your GitHub repository URL (https://github.com/<user>/<repo>.git): " REPO_URL
  git remote add origin "$REPO_URL"
fi

echo ">> git push"
git push -u origin "$BRANCH"
echo "Done. Open the Actions tab of your repo to watch the pipeline run."
