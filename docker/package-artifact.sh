#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="${1:-$ROOT_DIR/artifact_dist}"
IMAGE_TAG="${IMAGE_TAG:-prism-convex:ae}"
IMAGE_ARCHIVE_NAME="${IMAGE_ARCHIVE_NAME:-prism-convex-ae-image.tar.gz}"
SUBMISSION_ARCHIVE_NAME="${SUBMISSION_ARCHIVE_NAME:-qest-formats-2026-ae-artifact.tar.gz}"
PAPER_PDF="${PAPER_PDF:-}"

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "ERROR: required command not found: $1" >&2
    exit 1
  }
}

require_cmd docker
require_cmd tar
require_cmd shasum

mkdir -p "$OUT_DIR"

echo "==> Building Docker image: $IMAGE_TAG"
docker build -t "$IMAGE_TAG" -f "$ROOT_DIR/Dockerfile" "$ROOT_DIR"

IMAGE_ARCHIVE_PATH="$OUT_DIR/$IMAGE_ARCHIVE_NAME"
echo "==> Exporting Docker image: $IMAGE_ARCHIVE_PATH"
docker save "$IMAGE_TAG" | gzip -n > "$IMAGE_ARCHIVE_PATH"

STAGING_DIR="$(mktemp -d "$OUT_DIR/.artifact-staging.XXXXXX")"
cleanup() {
  rm -rf "$STAGING_DIR"
}
trap cleanup EXIT

cp "$IMAGE_ARCHIVE_PATH" "$STAGING_DIR/$IMAGE_ARCHIVE_NAME"
cp "$ROOT_DIR/docker/README.md" "$STAGING_DIR/README.md"
cp "$ROOT_DIR/COPYING.txt" "$STAGING_DIR/LICENSE"

if [[ -n "$PAPER_PDF" ]]; then
  if [[ ! -f "$PAPER_PDF" ]]; then
    echo "ERROR: PAPER_PDF is set but file does not exist: $PAPER_PDF" >&2
    exit 1
  fi
  cp "$PAPER_PDF" "$STAGING_DIR/paper.pdf"
fi

if command -v git >/dev/null 2>&1 && git -C "$ROOT_DIR" rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  {
    echo "image_tag=$IMAGE_TAG"
    echo "build_time_utc=$(date -u +%Y-%m-%dT%H:%M:%SZ)"
    echo "git_commit=$(git -C "$ROOT_DIR" rev-parse HEAD)"
    if [[ -n "$(git -C "$ROOT_DIR" status --porcelain)" ]]; then
      echo "git_tree_state=dirty"
    else
      echo "git_tree_state=clean"
    fi
  } > "$STAGING_DIR/BUILD_INFO.txt"
fi

SUBMISSION_ARCHIVE_PATH="$OUT_DIR/$SUBMISSION_ARCHIVE_NAME"
echo "==> Creating submission archive: $SUBMISSION_ARCHIVE_PATH"
tar -C "$STAGING_DIR" -czf "$SUBMISSION_ARCHIVE_PATH" .

SHA_PATH="$SUBMISSION_ARCHIVE_PATH.sha256"
shasum -a 256 "$SUBMISSION_ARCHIVE_PATH" > "$SHA_PATH"

echo
echo "Done."
echo "  Image archive:       $IMAGE_ARCHIVE_PATH"
echo "  Submission archive:  $SUBMISSION_ARCHIVE_PATH"
echo "  SHA-256 file:        $SHA_PATH"
if [[ -z "$PAPER_PDF" ]]; then
  echo "  Note: paper PDF not included. Set PAPER_PDF=/path/to/paper.pdf to include it."
fi
