#!/usr/bin/env bash
set -euo pipefail

RUN_SOLVER_BIN="${RUN_SOLVER_BIN:-run-benchmarks}"
RUN_LEARNER_BIN="${RUN_LEARNER_BIN:-run-learner}"

AE_RESULTS_ROOT="${AE_RESULTS_ROOT:-plotting_paper_with_ellipsoids/artifact_results}"
AE_SOLVER_INPUT_ROOT="${AE_SOLVER_INPUT_ROOT:-plotting_paper_with_ellipsoids/results_uniform_solving_new/parametric_convex}"

AE_QUICK_LEARNER_MODEL="${AE_QUICK_LEARNER_MODEL:-BETTING_GAME_PARALLEL}"
AE_QUICK_LEARNER_RUNS="${AE_QUICK_LEARNER_RUNS:-PARAMETER_TYING,PARAMETRIC_CONVEX}"
AE_QUICK_LEARNER_ITERATIONS="${AE_QUICK_LEARNER_ITERATIONS:-4000}"
AE_QUICK_LEARNER_MAX_EPISODE_LENGTH="${AE_QUICK_LEARNER_MAX_EPISODE_LENGTH:-6}"
AE_QUICK_LEARNER_MULTIPLIER="${AE_QUICK_LEARNER_MULTIPLIER:-2}"
AE_QUICK_LEARNER_PARAMS="${AE_QUICK_LEARNER_PARAMS:-n=6,p_1=0.55,p_2=0.53}"

AE_QUICK_SOLVER_MODEL="${AE_QUICK_SOLVER_MODEL:-BETTING_GAME_PARALLEL}"
AE_QUICK_SOLVER_RUNS="${AE_QUICK_SOLVER_RUNS:-PARAMETER_TYING,PARAMETRIC_CONVEX}"
AE_QUICK_SOLVER_TIMEOUT_SECONDS="${AE_QUICK_SOLVER_TIMEOUT_SECONDS:-900}"

AE_PAPER_MODELS=(
  AIRCRAFT_MIXTURE_POSITION
  BETTING_GAME_CONVEX_ADAPTIVE
  BETTING_GAME_PARALLEL
  ENGAGEMENT_ADAPTIVE_5
  GLIDER
  SAV2_ADAPTIVE_5
)
AE_PAPER_LEARNER_RUNS="${AE_PAPER_LEARNER_RUNS:-PARAMETER_TYING,PARAMETRIC_CONVEX,LP_TO_INTERVAL_EXACT,LP_TO_INTERVAL_FAST}"
AE_PAPER_SOLVER_RUNS="${AE_PAPER_SOLVER_RUNS:-PARAMETER_TYING,PARAMETRIC_CONVEX,LP_TO_INTERVAL_EXACT,LP_TO_INTERVAL_FAST,ELLIPSOID,ELLIPSOID_TO_INTERVAL_EXACT,ELLIPSOID_TO_INTERVAL_FAST}"
AE_PAPER_SOLVER_TIMEOUT_SECONDS="${AE_PAPER_SOLVER_TIMEOUT_SECONDS:-7200}"

usage() {
  cat <<'USAGE'
Usage: run-ae <command>

Commands:
  quick                 Run a short learner+solver smoke test.
  learner-paper-subset  Run learner on the paper subset models.
  solver-paper-subset   Run solver reproduction on the paper subset models.
  solver-full           Run solver reproduction on all benchmark instances.
  all-paper             Run learner-paper-subset, then solver-paper-subset.
  help                  Show this help.

Environment:
  AE_RESULTS_ROOT              Base output folder (default: plotting_paper_with_ellipsoids/artifact_results)
  AE_SOLVER_INPUT_ROOT         Solver benchmark input root
  AE_QUICK_*                   Quick preset overrides
  AE_PAPER_LEARNER_RUNS        Learner run set for paper subset
  AE_PAPER_SOLVER_RUNS         Solver run set for paper subset
  AE_PAPER_SOLVER_TIMEOUT_SECONDS
USAGE
}

run_quick() {
  local learner_root="${AE_RESULTS_ROOT}/learning_quick/parametric_convex"
  local solver_root="${AE_RESULTS_ROOT}/solver_quick/parametric_convex"
  mkdir -p "$learner_root" "$solver_root"

  "$RUN_LEARNER_BIN" \
    --model="${AE_QUICK_LEARNER_MODEL}" \
    --runs="${AE_QUICK_LEARNER_RUNS}" \
    --iterations="${AE_QUICK_LEARNER_ITERATIONS}" \
    --max-episode-length="${AE_QUICK_LEARNER_MAX_EPISODE_LENGTH}" \
    --multiplier="${AE_QUICK_LEARNER_MULTIPLIER}" \
    --param="${AE_QUICK_LEARNER_PARAMS}" \
    --output-root="${learner_root}"

  "$RUN_SOLVER_BIN" \
    --benchmark-input-root="${AE_SOLVER_INPUT_ROOT}" \
    --benchmark-output-root="${solver_root}" \
    --model="${AE_QUICK_SOLVER_MODEL}" \
    --runs="${AE_QUICK_SOLVER_RUNS}" \
    --benchmark-timeout-seconds="${AE_QUICK_SOLVER_TIMEOUT_SECONDS}"

  echo "Quick-check outputs:"
  echo "  Learner: ${learner_root}"
  echo "  Solver:  ${solver_root}"
}

run_learner_paper_subset() {
  local out_root="${AE_RESULTS_ROOT}/learning_paper_subset/parametric_convex"
  mkdir -p "$out_root"

  "$RUN_LEARNER_BIN" --model=AIRCRAFT_MIXTURE_POSITION --runs="${AE_PAPER_LEARNER_RUNS}" --param=maxX=20,maxY=10,theta1=0.4,theta2=0.2,theta3=0.15,theta4=0.12 --iterations=100000 --max-episode-length=20 --multiplier=2 --output-root="$out_root"
  "$RUN_LEARNER_BIN" --model=BETTING_GAME_CONVEX_ADAPTIVE --runs="${AE_PAPER_LEARNER_RUNS}" --param=n=25,p=0.55 --iterations=200000 --max-episode-length=25 --multiplier=2 --output-root="$out_root"
  "$RUN_LEARNER_BIN" --model=BETTING_GAME_PARALLEL --runs="${AE_PAPER_LEARNER_RUNS}" --param=n=6,p_1=0.55,p_2=0.53 --iterations=100000 --max-episode-length=6 --multiplier=2 --output-root="$out_root"
  "$RUN_LEARNER_BIN" --model=ENGAGEMENT_ADAPTIVE_5 --runs="${AE_PAPER_LEARNER_RUNS}" --param=L=50,theta1=0.3,theta2=0.2,theta3=0.1,theta4=0.25 --iterations=300000 --max-episode-length=50 --multiplier=2 --output-root="$out_root"
  "$RUN_LEARNER_BIN" --model=GLIDER --runs="${AE_PAPER_LEARNER_RUNS}" --param=w=11,h=9,theta_h=0.3,theta_v=0.7 --iterations=200000 --max-episode-length=100 --multiplier=2 --output-root="$out_root"
  "$RUN_LEARNER_BIN" --model=SAV2_ADAPTIVE_5 --runs="${AE_PAPER_LEARNER_RUNS}" --param=Xsize=10,Ysize=10,theta1=0.4,theta2=0.2,theta3=0.15,theta4=0.14 --iterations=100000 --max-episode-length=50 --multiplier=2 --output-root="$out_root"

  echo "Learner paper-subset outputs: ${out_root}"
}

run_solver_paper_subset() {
  local out_root="${AE_RESULTS_ROOT}/solver_paper_subset/parametric_convex"
  mkdir -p "$out_root"

  local model
  for model in "${AE_PAPER_MODELS[@]}"; do
    "$RUN_SOLVER_BIN" \
      --benchmark-input-root="${AE_SOLVER_INPUT_ROOT}" \
      --benchmark-output-root="${out_root}" \
      --model="${model}" \
      --runs="${AE_PAPER_SOLVER_RUNS}" \
      --benchmark-timeout-seconds="${AE_PAPER_SOLVER_TIMEOUT_SECONDS}"
  done

  echo "Solver paper-subset outputs: ${out_root}"
}

run_solver_full() {
  local out_root="${AE_RESULTS_ROOT}/solver_full/parametric_convex"
  mkdir -p "$out_root"

  "$RUN_SOLVER_BIN" \
    --benchmark-input-root="${AE_SOLVER_INPUT_ROOT}" \
    --benchmark-output-root="${out_root}" \
    "$@"

  echo "Solver full outputs: ${out_root}"
}

command="${1:-quick}"
if [[ $# -gt 0 ]]; then
  shift
fi

case "$command" in
  quick)
    run_quick "$@"
    ;;
  learner-paper-subset)
    run_learner_paper_subset "$@"
    ;;
  solver-paper-subset)
    run_solver_paper_subset "$@"
    ;;
  solver-full)
    run_solver_full "$@"
    ;;
  all-paper)
    run_learner_paper_subset
    run_solver_paper_subset
    ;;
  help|--help|-h)
    usage
    ;;
  *)
    echo "Unknown command: $command" >&2
    usage
    exit 1
    ;;
esac
