#!/usr/bin/env bash
set -euo pipefail

die() {
  echo "ERROR: $*" >&2
  exit 1
}

PRISM_HOME="${PRISM_DIR:-/workspace/prism_convex/prism}"
cd "$PRISM_HOME"

[[ -d "$PWD/classes" ]] || die "Missing classes directory at $PWD/classes. Did the image build step complete?"
[[ -d "$PWD/lib" ]] || die "Missing lib directory at $PWD/lib."

if [[ -n "${GUROBI_LIB_DIR:-}" ]]; then
  [[ -d "$GUROBI_LIB_DIR" ]] || die "GUROBI_LIB_DIR does not exist: $GUROBI_LIB_DIR"
  ls "$GUROBI_LIB_DIR"/libgurobi*.so >/dev/null 2>&1 || die "No libgurobi*.so found in GUROBI_LIB_DIR=$GUROBI_LIB_DIR"
  ls "$GUROBI_LIB_DIR"/libGurobiJni*.so >/dev/null 2>&1 || die "No libGurobiJni*.so found in GUROBI_LIB_DIR=$GUROBI_LIB_DIR"
fi

if [[ -n "${GRB_LICENSE_FILE:-}" && ! -f "${GRB_LICENSE_FILE}" ]]; then
  die "GRB_LICENSE_FILE points to a missing file: ${GRB_LICENSE_FILE}"
fi

JAVA_LIB_PATH="$PWD/lib"
if [[ -n "${GUROBI_LIB_DIR:-}" ]]; then
  JAVA_LIB_PATH="${JAVA_LIB_PATH}:${GUROBI_LIB_DIR}"
fi

LD_PATH="$PWD/lib"
if [[ -n "${GUROBI_LIB_DIR:-}" ]]; then
  LD_PATH="${LD_PATH}:${GUROBI_LIB_DIR}"
fi
if [[ -n "${LD_LIBRARY_PATH:-}" ]]; then
  LD_PATH="${LD_PATH}:${LD_LIBRARY_PATH}"
fi
export LD_LIBRARY_PATH="$LD_PATH"

TOOL_OPTS=(
  "--enable-native-access=ALL-UNNAMED"
  "-Xms${JAVA_INITIAL_HEAP:-12g}"
  "-Xmx${JAVA_MAX_HEAP:-28g}"
  "-Djava.library.path=${JAVA_LIB_PATH}"
)

if [[ -n "${JAVA_TOOL_OPTIONS:-}" ]]; then
  export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS} ${TOOL_OPTS[*]}"
else
  export JAVA_TOOL_OPTIONS="${TOOL_OPTS[*]}"
fi

cmd=(
  java
  -cp "classes:lib/*"
  learning.ParametricConvex.ParametricConvexSolver
  --reproduce-benchmarks
  --benchmark-input-root="${BENCHMARK_INPUT_ROOT:-plotting_paper_with_ellipsoids/results_uniform_solving_new/parametric_convex}"
)

if [[ -n "${BENCHMARK_TIMEOUT_SECONDS:-}" ]]; then
  cmd+=(--benchmark-timeout-seconds="${BENCHMARK_TIMEOUT_SECONDS}")
fi
if [[ -n "${BENCHMARK_OUTPUT_ROOT:-}" ]]; then
  cmd+=(--benchmark-output-root="${BENCHMARK_OUTPUT_ROOT}")
fi
if [[ -n "${BENCHMARK_MODEL:-}" ]]; then
  cmd+=(--model="${BENCHMARK_MODEL}")
fi
if [[ -n "${BENCHMARK_RUNS:-}" ]]; then
  cmd+=(--runs="${BENCHMARK_RUNS}")
fi

cmd+=("$@")
exec "${cmd[@]}"
