#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'USAGE'
Usage: run-postprocess <tables|stats|learning-plots|plots|all> [extra args passed to python scripts]

Environment:
  AE_TABLE_SCRIPT             Python script for table extraction
                              (default: artifact_eval/table_row_printer.py)
  AE_STATS_SCRIPT             Python script for benchmark statistics table
                              (default: artifact_eval/plot_results.py)
  AE_LEARNING_PLOT_SCRIPT     Python script for learning benchmark plots
                              (default: artifact_eval/learning_plot_results.py)
  AE_PLOT_SCRIPT              Backward-compatible alias for AE_LEARNING_PLOT_SCRIPT
  AE_POSTPROCESS_INPUT_ROOT   Input directory with experiment outputs
                              (default: plotting_paper_with_ellipsoids/artifact_results/solver_paper_subset/parametric_convex)
  AE_POSTPROCESS_LEARNING_INPUT_ROOT
                              Learning results root for learning-plots mode
                              (default: plotting_paper_with_ellipsoids/artifact_results/learning_paper_subset/parametric_convex)
  AE_POSTPROCESS_OUTPUT_ROOT  Base output directory for postprocessing artifacts
                              (default: plotting_paper_with_ellipsoids/artifact_results/postprocess)
USAGE
}

mode="${1:-help}"
if [[ $# -gt 0 ]]; then
  shift
fi

PRISM_HOME="${PRISM_DIR:-/workspace/prism_convex/prism}"
REPO_HOME=""
if REPO_HOME_CANDIDATE="$(cd "${PRISM_HOME}/.." 2>/dev/null && pwd)"; then
  REPO_HOME="${REPO_HOME_CANDIDATE}"
fi

resolve_default_script() {
  local script_rel="$1"
  local override="${2:-}"
  if [[ -n "$override" ]]; then
    echo "$override"
    return
  fi

  local candidates=(
    "${PRISM_HOME}/${script_rel}"
    "${REPO_HOME}/${script_rel}"
    "${PWD}/${script_rel}"
    "${script_rel}"
  )
  local candidate
  for candidate in "${candidates[@]}"; do
    if [[ -n "$candidate" && -f "$candidate" ]]; then
      echo "$candidate"
      return
    fi
  done

  if [[ -n "$REPO_HOME" ]]; then
    echo "${REPO_HOME}/${script_rel}"
  else
    echo "${script_rel}"
  fi
}

TABLE_SCRIPT="$(resolve_default_script "artifact_eval/table_row_printer.py" "${AE_TABLE_SCRIPT:-}")"
STATS_SCRIPT="$(resolve_default_script "artifact_eval/plot_results.py" "${AE_STATS_SCRIPT:-}")"
INPUT_ROOT="${AE_POSTPROCESS_INPUT_ROOT:-plotting_paper_with_ellipsoids/artifact_results/solver_paper_subset/parametric_convex}"
LEARNING_INPUT_ROOT="${AE_POSTPROCESS_LEARNING_INPUT_ROOT:-plotting_paper_with_ellipsoids/artifact_results/learning_paper_subset/parametric_convex}"
OUTPUT_ROOT="${AE_POSTPROCESS_OUTPUT_ROOT:-plotting_paper_with_ellipsoids/artifact_results/postprocess}"
LEARNING_SCRIPT_OVERRIDE="${AE_LEARNING_PLOT_SCRIPT:-${AE_PLOT_SCRIPT:-}}"
LEARNING_PLOT_SCRIPT="$(resolve_default_script "artifact_eval/learning_plot_results.py" "${LEARNING_SCRIPT_OVERRIDE}")"

mkdir -p "$OUTPUT_ROOT/tables" "$OUTPUT_ROOT/stats" "$OUTPUT_ROOT/learning_plots"

run_tables() {
  if [[ ! -f "$TABLE_SCRIPT" ]]; then
    echo "ERROR: table script not found: $TABLE_SCRIPT" >&2
    echo "Expected interface: python3 $TABLE_SCRIPT --input-root <dir> --output-root <dir>" >&2
    exit 1
  fi
  python3 "$TABLE_SCRIPT" --input-root "$INPUT_ROOT" --output-root "$OUTPUT_ROOT/tables" "$@"
}

run_stats() {
  if [[ ! -f "$STATS_SCRIPT" ]]; then
    echo "ERROR: stats script not found: $STATS_SCRIPT" >&2
    echo "Expected interface: python3 $STATS_SCRIPT --input-root <dir> --output-root <dir>" >&2
    exit 1
  fi
  python3 "$STATS_SCRIPT" --input-root "$INPUT_ROOT" --output-root "$OUTPUT_ROOT/stats" "$@"
}

run_learning_plots() {
  if [[ ! -f "$LEARNING_PLOT_SCRIPT" ]]; then
    echo "ERROR: learning plot script not found: $LEARNING_PLOT_SCRIPT" >&2
    echo "Expected interface: python3 $LEARNING_PLOT_SCRIPT --input-root <dir> --output-root <dir>" >&2
    exit 1
  fi
  python3 "$LEARNING_PLOT_SCRIPT" --input-root "$LEARNING_INPUT_ROOT" --output-root "$OUTPUT_ROOT/learning_plots" "$@"
}

case "$mode" in
  tables)
    run_tables "$@"
    ;;
  stats)
    run_stats "$@"
    ;;
  learning-plots)
    run_learning_plots "$@"
    ;;
  plots)
    # Backward-compatible alias for learning-plots.
    run_learning_plots "$@"
    ;;
  all)
    run_tables "$@"
    run_stats "$@"
    run_learning_plots "$@"
    ;;
  help|--help|-h)
    usage
    ;;
  *)
    echo "Unknown mode: $mode" >&2
    usage
    exit 1
    ;;
esac
