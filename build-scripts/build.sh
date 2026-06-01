#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "$ROOT_DIR"

SKIP_TESTS=0
CLEAN=0

for arg in "$@"; do
  case "$arg" in
    --skip-tests) SKIP_TESTS=1 ;;
    --clean) CLEAN=1 ;;
    -h|--help)
      echo "Usage: build-scripts/build.sh [--clean] [--skip-tests]"
      exit 0
      ;;
    *)
      echo "Unknown option: $arg" >&2
      exit 2
      ;;
  esac
done

MVN_ARGS=()
if [[ "$CLEAN" == "1" ]]; then
  MVN_ARGS+=(clean)
fi
MVN_ARGS+=(verify)
if [[ "$SKIP_TESTS" == "1" ]]; then
  MVN_ARGS+=(-DskipTests)
fi

echo "Building NetBeans plugin..."
echo "Command: mvn ${MVN_ARGS[*]}"
mvn "${MVN_ARGS[@]}"
echo
echo "Build completed."
