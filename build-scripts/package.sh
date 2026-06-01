#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "$ROOT_DIR"

SKIP_TESTS=0
CLEAN=0
OUTPUT_DIR="dist"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-tests) SKIP_TESTS=1; shift ;;
    --clean) CLEAN=1; shift ;;
    --output-dir)
      OUTPUT_DIR="${2:-}"
      if [[ -z "$OUTPUT_DIR" ]]; then
        echo "--output-dir requires a value" >&2
        exit 2
      fi
      shift 2
      ;;
    -h|--help)
      echo "Usage: build-scripts/package.sh [--clean] [--skip-tests] [--output-dir DIR]"
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      exit 2
      ;;
  esac
done

MVN_ARGS=()
if [[ "$CLEAN" == "1" ]]; then
  MVN_ARGS+=(clean)
fi
MVN_ARGS+=(package)
if [[ "$SKIP_TESTS" == "1" ]]; then
  MVN_ARGS+=(-DskipTests)
fi

echo "Packaging NetBeans plugin..."
echo "Command: mvn ${MVN_ARGS[*]}"
mvn "${MVN_ARGS[@]}"

mkdir -p "$OUTPUT_DIR"
shopt -s nullglob globstar
NBM_FILES=(target/**/*.nbm target/*.nbm)
if [[ ${#NBM_FILES[@]} -eq 0 ]]; then
  echo "No .nbm file found under target." >&2
  exit 1
fi

COPIED_NAMES=" "
for file in "${NBM_FILES[@]}"; do
  name="$(basename "$file")"
  if [[ "$COPIED_NAMES" == *" $name "* ]]; then
    continue
  fi
  cp -f "$file" "$OUTPUT_DIR/"
  COPIED_NAMES="${COPIED_NAMES}${name} "
  echo "Packaged: ${OUTPUT_DIR}/${name}"
done

echo
echo "Package completed."
