#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="${1:-$ROOT_DIR/artifact_dist}"
IMAGE_TAG_BASE="${IMAGE_TAG_BASE:-prism-convex:ae}"
IMAGE_ARCHIVE_PREFIX="${IMAGE_ARCHIVE_PREFIX:-prism-convex-ae}"
ARCHES="${ARCHES:-amd64,arm64}"
SKIP_EXISTING="${SKIP_EXISTING:-1}"
SUBMISSION_ARCHIVE_NAME="${SUBMISSION_ARCHIVE_NAME:-qest-formats-2026-ae-artifact.tar.gz}"
DEFAULT_PAPER_PDF="$ROOT_DIR/docker/Learning_Parameters_of_Uncertain_pMDPs (43).pdf"
PAPER_PDF="${PAPER_PDF:-$DEFAULT_PAPER_PDF}"

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "ERROR: required command not found: $1" >&2
    exit 1
  }
}

require_cmd docker
require_cmd tar
require_cmd shasum

if ! docker buildx version >/dev/null 2>&1; then
  echo "ERROR: docker buildx is required for multi-arch packaging." >&2
  exit 1
fi

mkdir -p "$OUT_DIR"

IFS=',' read -r -a RAW_ARCHES <<< "$ARCHES"
SELECTED_ARCHES=()
for raw_arch in "${RAW_ARCHES[@]}"; do
  arch="$(echo "$raw_arch" | tr '[:upper:]' '[:lower:]' | xargs)"
  if [[ -z "$arch" ]]; then
    continue
  fi
  case "$arch" in
    amd64|arm64)
      SELECTED_ARCHES+=("$arch")
      ;;
    *)
      echo "ERROR: unsupported arch '$arch'. Supported values: amd64, arm64" >&2
      exit 1
      ;;
  esac
done

if [[ ${#SELECTED_ARCHES[@]} -eq 0 ]]; then
  echo "ERROR: no valid architectures selected via ARCHES='$ARCHES'." >&2
  exit 1
fi

STAGING_DIR="$(mktemp -d "$OUT_DIR/.artifact-staging.XXXXXX")"
cleanup() {
  rm -rf "$STAGING_DIR"
}
trap cleanup EXIT

IMAGE_ARCHIVE_PATHS=()
for arch in "${SELECTED_ARCHES[@]}"; do
  platform="linux/${arch}"
  tag="${IMAGE_TAG_BASE}-${arch}"
  archive_name="${IMAGE_ARCHIVE_PREFIX}-${arch}.tar.gz"
  archive_path="${OUT_DIR}/${archive_name}"

  if [[ "$SKIP_EXISTING" == "1" && -s "$archive_path" ]]; then
    echo "==> Reusing existing image archive: ${archive_path}"
  else
    echo "==> Building Docker image: ${tag} (${platform})"
    docker buildx build --platform "$platform" -t "$tag" -f "$ROOT_DIR/Dockerfile" "$ROOT_DIR" --load

    echo "==> Exporting Docker image: ${archive_path}"
    docker save "$tag" | gzip -n > "$archive_path"
  fi

  IMAGE_ARCHIVE_PATHS+=("$archive_path")
  cp "$archive_path" "$STAGING_DIR/$archive_name"
done

cp "$ROOT_DIR/docker/README.md" "$STAGING_DIR/README.md"
cp "$ROOT_DIR/COPYING.txt" "$STAGING_DIR/LICENSE"

if [[ -n "$PAPER_PDF" ]]; then
  if [[ ! -f "$PAPER_PDF" ]]; then
    if [[ "$PAPER_PDF" == "$DEFAULT_PAPER_PDF" ]]; then
      echo "WARNING: default paper PDF not found at: $PAPER_PDF (continuing without paper.pdf)"
      PAPER_PDF=""
    else
      echo "ERROR: PAPER_PDF is set but file does not exist: $PAPER_PDF" >&2
      exit 1
    fi
  fi
fi

if [[ -n "$PAPER_PDF" ]]; then
  cp "$PAPER_PDF" "$STAGING_DIR/paper.pdf"
fi

if command -v git >/dev/null 2>&1 && git -C "$ROOT_DIR" rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  {
    echo "image_tag_base=$IMAGE_TAG_BASE"
    echo "arches=$(IFS=, ; echo "${SELECTED_ARCHES[*]}")"
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
for archive_path in "${IMAGE_ARCHIVE_PATHS[@]}"; do
  echo "  Image archive:       $archive_path"
done
echo "  Submission archive:  $SUBMISSION_ARCHIVE_PATH"
echo "  SHA-256 file:        $SHA_PATH"
if [[ -z "$PAPER_PDF" ]]; then
  echo "  Note: paper PDF not included. Set PAPER_PDF=/path/to/paper.pdf to include it."
fi
