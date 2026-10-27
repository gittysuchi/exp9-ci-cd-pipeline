#!/usr/bin/env bash
# Promotes the canary: 100 % of the traffic now goes to the new release.
#
# Usage: deploy/full-rollout.sh <new-image> [version]
set -euo pipefail

NEW_IMAGE="${1:?new image required}"
VERSION="${2:-release}"

cd "$(dirname "$0")"

# In the full-rollout topology both containers run the new release; nginx only routes to "canary".
export STABLE_IMAGE="$NEW_IMAGE" CANARY_IMAGE="$NEW_IMAGE"
export STABLE_VERSION="$VERSION" CANARY_VERSION="$VERSION" CANARY_FAIL="false"
export NGINX_CONF="full.conf"

echo "==> Tearing down the canary topology"
docker compose -f docker-compose.canary.yml down --remove-orphans

echo "==> Full rollout of ${NEW_IMAGE}"
docker compose -f docker-compose.canary.yml up -d --wait --wait-timeout 180

echo "==> Smoke test on the fully rolled-out release"
curl -fsS http://localhost:8080/api/info
echo
curl -fsS http://localhost:8080/api/products > /dev/null
echo "==> Release ${VERSION} is live for 100 % of users"
