#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

# 기본값 prod, 인자로 덮어쓰기 가능 (dev, prod)
PROFILE="${1:-prod}"

SPRING_PROFILES_ACTIVE="$PROFILE" \
  ./gradlew bootRun
