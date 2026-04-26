#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'USAGE'
Usage: run-postprocess <tables|plots|all> [extra args passed to python scripts]

Environment:
  AE_TABLE_SCRIPT             Python script for table extraction
                              (default: artifact_eval/table_row_printer.py)
  AE_PLOT_SCRIPT              Python script for plot generation
                              (default: artifact_eval/plot_results.py)
  AE_POSTPROCESS_INPUT_ROOT   Input directory with experiment outputs
                              (default: plotting_paper_with_ellipsoids/artifact_results/solver_paper_subset/parametric_convex)
  AE_POSTPROCESS_OUTPUT_ROOT  Base output directory for postprocessing artifacts
                              (default: plotting_paper_with_ellipsoids/artifact_results/postprocess)
USAGE
}

mode="${1:-help}"
if [[ $# -gt 0 ]]; then
  shift
fi

TABLE_SCRIPT="${AE_TABLE_SCRIPT:-artifact_eval/table_row_printer.py}"
PLOT_SCRIPT="${AE_PLOT_SCRIPT:-artifact_eval/plot_results.py}"
INPUT_ROOT="${AE_POSTPROCESS_INPUT_ROOT:-plotting_paper_with_ellipsoids/artifact_results/solver_paper_subset/parametric_convex}"
OUTPUT_ROOT="${AE_POSTPROCESS_OUTPUT_ROOT:-plotting_paper_with_ellipsoids/artifact_results/postprocess}"

mkdir -p "$OUTPUT_ROOT/tables" "$OUTPUT_ROOT/plots"

run_tables() {
  if [[ ! -f "$TABLE_SCRIPT" ]]; then
    echo "ERROR: table script not found: $TABLE_SCRIPT" >&2
    echo "Expected interface: python3 $TABLE_SCRIPT --input-root <dir> --output-root <dir>" >&2
    exit 1
  fi
  python3 "$TABLE_SCRIPT" --input-root "$INPUT_ROOT" --output-root "$OUTPUT_ROOT/tables" "$@"
}

run_plots() {
  if [[ ! -f "$PLOT_SCRIPT" ]]; then
    echo "ERROR: plot script not found: $PLOT_SCRIPT" >&2
    echo "Expected interface: python3 $PLOT_SCRIPT --input-root <dir> --output-root <dir>" >&2
    exit 1
  fi
  python3 "$PLOT_SCRIPT" --input-root "$INPUT_ROOT" --output-root "$OUTPUT_ROOT/plots" "$@"
}

case "$mode" in
  tables)
    run_tables "$@"
    ;;
  plots)
    run_plots "$@"
    ;;
  all)
    run_tables "$@"
    run_plots "$@"
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
