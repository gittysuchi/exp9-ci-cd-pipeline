#!/usr/bin/env bash
# Starts stable + canary behind the nginx gateway (10 % canary traffic).
#
# Usage:  deploy/canary-deploy.sh <stable-image> <canary-image> [canary-version] [simulate-failure:true|false]
set -euo pipefail

STABLE_IMAGE="${1:?stable image required}"
CANARY_IMAGE="${2:?canary image required}"
CANARY_VERSION="${3:-canary}"
CANARY_FAIL="${4:-false}"

cd "$(dirname "$0")"

export STABLE_IMAGE CANARY_IMAGE CANARY_VERSION CANARY_FAIL
export STABLE_VERSION="stable"
export NGINX_CONF="canary.conf"

echo "==> Deploying CANARY release (10 % of traffic)"
echo "    stable : ${STABLE_IMAGE}"
echo "    canary : ${CANARY_IMAGE}  (simulate failure: ${CANARY_FAIL})"

docker compose -f docker-compose.canary.yml up -d --wait --wait-timeout 180
docker compose -f docker-compose.canary.yml ps
echo "==> Canary deployment is live on http://localhost:8080"
