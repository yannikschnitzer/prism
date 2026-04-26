#!/usr/bin/env bash
set -euo pipefail

RUN_SOLVER_BIN="${RUN_SOLVER_BIN:-run-benchmarks}"
RUN_LEARNER_BIN="${RUN_LEARNER_BIN:-run-learner}"

AE_RESULTS_ROOT="${AE_RESULTS_ROOT:-plotting_paper_with_ellipsoids/artifact_results}"
AE_SOLVER_INPUT_ROOT="${AE_SOLVER_INPUT_ROOT:-plotting_paper_with_ellipsoids/results_uniform_solving_new/parametric_convex}"

AE_QUICK_LEARNER_MODEL="${AE_QUICK_LEARNER_MODEL:-AIRCRAFT_MIXTURE_POSITION}"
AE_QUICK_LEARNER_RUNS="${AE_QUICK_LEARNER_RUNS:-PARAMETER_TYING,PARAMETRIC_CONVEX}"
AE_QUICK_LEARNER_ITERATIONS="${AE_QUICK_LEARNER_ITERATIONS:-4000}"
AE_QUICK_LEARNER_MAX_EPISODE_LENGTH="${AE_QUICK_LEARNER_MAX_EPISODE_LENGTH:-20}"
AE_QUICK_LEARNER_MULTIPLIER="${AE_QUICK_LEARNER_MULTIPLIER:-2}"
AE_QUICK_LEARNER_PARAMS="${AE_QUICK_LEARNER_PARAMS:-maxX=20,maxY=10,theta1=0.4,theta2=0.2,theta3=0.15,theta4=0.12}"

AE_QUICK_SOLVER_MODEL="${AE_QUICK_SOLVER_MODEL:-AIRCRAFT_MIXTURE_POSITION}"
AE_QUICK_SOLVER_PARAMS="${AE_QUICK_SOLVER_PARAMS:-maxX=50,maxY=10,theta1=0.4,theta2=0.2,theta3=0.15,theta4=0.12}"
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
AE_PAPER_LEARNER_ITERATIONS="${AE_PAPER_LEARNER_ITERATIONS:-100000}"
AE_PAPER_SOLVER_RUNS="${AE_PAPER_SOLVER_RUNS:-PARAMETER_TYING,PARAMETRIC_CONVEX,LP_TO_INTERVAL_EXACT,LP_TO_INTERVAL_FAST,ELLIPSOID,ELLIPSOID_TO_INTERVAL_EXACT,ELLIPSOID_TO_INTERVAL_FAST}"
AE_PAPER_SOLVER_TIMEOUT_SECONDS="${AE_PAPER_SOLVER_TIMEOUT_SECONDS:-7200}"

# Fixed subset sizes for paper-subset commands (learner + solver).
AE_SUBSET_AIRCRAFT_PARAMS="${AE_SUBSET_AIRCRAFT_PARAMS:-maxX=50,maxY=10,theta1=0.4,theta2=0.2,theta3=0.15,theta4=0.12}"
AE_SUBSET_BETTING_CONVEX_PARAMS="${AE_SUBSET_BETTING_CONVEX_PARAMS:-n=50,p=0.55}"
AE_SUBSET_BETTING_PARALLEL_PARAMS="${AE_SUBSET_BETTING_PARALLEL_PARAMS:-n=5,p_1=0.55,p_2=0.53}"
AE_SUBSET_ENGAGEMENT_PARAMS="${AE_SUBSET_ENGAGEMENT_PARAMS:-L=100,theta1=0.3,theta2=0.2,theta3=0.1,theta4=0.25}"
AE_SUBSET_GLIDER_PARAMS="${AE_SUBSET_GLIDER_PARAMS:-w=21,h=17,theta_h=0.3,theta_v=0.7}"
AE_SUBSET_SAV2_PARAMS="${AE_SUBSET_SAV2_PARAMS:-Xsize=50,Ysize=50,theta1=0.4,theta2=0.2,theta3=0.15,theta4=0.14}"

usage() {
  cat <<'USAGE'
Usage: run-ae <command>

Commands:
  quick                 Run a short learner+solver smoke test.
  learner-paper-subset  Run learner on the paper subset models.
  solver-paper-subset   Run solver reproduction on the paper subset models.
  solver-full           Run solver reproduction on all benchmark instances.
  postprocess           Run table/statistics/learning-plot post-processing hooks (delegates to run-postprocess).
  all-paper             Run learner-paper-subset, then solver-paper-subset.
  help                  Show this help.

Environment:
  AE_RESULTS_ROOT              Base output folder (default: plotting_paper_with_ellipsoids/artifact_results)
  AE_SOLVER_INPUT_ROOT         Solver benchmark input root
  AE_QUICK_*                   Quick preset overrides
  AE_PAPER_LEARNER_RUNS        Learner run set for paper subset
  AE_PAPER_LEARNER_ITERATIONS  Learner iterations for paper subset (default: 100000)
  AE_PAPER_SOLVER_RUNS         Solver run set for paper subset
  AE_PAPER_SOLVER_TIMEOUT_SECONDS

Unofficial local-testing flag:
  run-ae solver-paper-subset --no-ellipsoid
  run-ae all-paper --no-ellipsoid
USAGE
}

strip_ellipsoid_runs() {
  local runs_csv="$1"
  local tokens=()
  local kept=()
  local token
  local trimmed

  IFS=',' read -r -a tokens <<< "$runs_csv"
  for token in "${tokens[@]}"; do
    trimmed="$(echo "$token" | xargs)"
    if [[ -z "$trimmed" ]]; then
      continue
    fi
    case "$trimmed" in
      ELLIPSOID|ELLIPSOID_TO_INTERVAL_EXACT|ELLIPSOID_TO_INTERVAL_FAST)
        continue
        ;;
      *)
        kept+=("$trimmed")
        ;;
    esac
  done

  if [[ ${#kept[@]} -eq 0 ]]; then
    echo "ERROR: no solver runs left after --no-ellipsoid filter." >&2
    exit 1
  fi

  local joined
  joined="$(IFS=','; echo "${kept[*]}")"
  echo "$joined"
}

stage_solver_input_pairs() {
  local subset_root="$1"
  shift

  rm -rf "$subset_root"
  mkdir -p "$subset_root"

  local pair model param_dir src_dir dst_dir
  for pair in "$@"; do
    model="${pair%%|*}"
    param_dir="${pair#*|}"
    src_dir="${AE_SOLVER_INPUT_ROOT}/${model}/${param_dir}"
    dst_dir="${subset_root}/${model}/${param_dir}"

    if [[ ! -d "$src_dir" ]]; then
      echo "ERROR: Missing benchmark input directory: $src_dir" >&2
      exit 1
    fi

    mkdir -p "$(dirname "$dst_dir")"
    cp -a "$src_dir" "$dst_dir"
  done
}

run_quick() {
  local learner_root="${AE_RESULTS_ROOT}/learning_quick/parametric_convex"
  local solver_root="${AE_RESULTS_ROOT}/solver_quick/parametric_convex"
  local quick_solver_input_root="${AE_RESULTS_ROOT}/_solver_quick_input/parametric_convex"
  mkdir -p "$learner_root" "$solver_root"

  stage_solver_input_pairs "$quick_solver_input_root" "${AE_QUICK_SOLVER_MODEL}|${AE_QUICK_SOLVER_PARAMS}"

  "$RUN_LEARNER_BIN" \
    --model="${AE_QUICK_LEARNER_MODEL}" \
    --runs="${AE_QUICK_LEARNER_RUNS}" \
    --iterations="${AE_QUICK_LEARNER_ITERATIONS}" \
    --max-episode-length="${AE_QUICK_LEARNER_MAX_EPISODE_LENGTH}" \
    --multiplier="${AE_QUICK_LEARNER_MULTIPLIER}" \
    --param="${AE_QUICK_LEARNER_PARAMS}" \
    --output-root="${learner_root}"

  "$RUN_SOLVER_BIN" \
    --benchmark-input-root="${quick_solver_input_root}" \
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

  "$RUN_LEARNER_BIN" --model=AIRCRAFT_MIXTURE_POSITION --runs="${AE_PAPER_LEARNER_RUNS}" --param="${AE_SUBSET_AIRCRAFT_PARAMS}" --iterations="${AE_PAPER_LEARNER_ITERATIONS}" --max-episode-length=20 --multiplier=2 --output-root="$out_root"
  "$RUN_LEARNER_BIN" --model=BETTING_GAME_CONVEX_ADAPTIVE --runs="${AE_PAPER_LEARNER_RUNS}" --param="${AE_SUBSET_BETTING_CONVEX_PARAMS}" --iterations="${AE_PAPER_LEARNER_ITERATIONS}" --max-episode-length=25 --multiplier=2 --output-root="$out_root"
  "$RUN_LEARNER_BIN" --model=BETTING_GAME_PARALLEL --runs="${AE_PAPER_LEARNER_RUNS}" --param="${AE_SUBSET_BETTING_PARALLEL_PARAMS}" --iterations="${AE_PAPER_LEARNER_ITERATIONS}" --max-episode-length=6 --multiplier=2 --output-root="$out_root"
  "$RUN_LEARNER_BIN" --model=ENGAGEMENT_ADAPTIVE_5 --runs="${AE_PAPER_LEARNER_RUNS}" --param="${AE_SUBSET_ENGAGEMENT_PARAMS}" --iterations="${AE_PAPER_LEARNER_ITERATIONS}" --max-episode-length=50 --multiplier=2 --output-root="$out_root"
  "$RUN_LEARNER_BIN" --model=GLIDER --runs="${AE_PAPER_LEARNER_RUNS}" --param="${AE_SUBSET_GLIDER_PARAMS}" --iterations="${AE_PAPER_LEARNER_ITERATIONS}" --max-episode-length=100 --multiplier=2 --output-root="$out_root"
  "$RUN_LEARNER_BIN" --model=SAV2_ADAPTIVE_5 --runs="${AE_PAPER_LEARNER_RUNS}" --param="${AE_SUBSET_SAV2_PARAMS}" --iterations="${AE_PAPER_LEARNER_ITERATIONS}" --max-episode-length=50 --multiplier=2 --output-root="$out_root"

  echo "Learner paper-subset outputs: ${out_root}"
}

stage_solver_subset_inputs() {
  local subset_root="$1"
  local pairs=(
    "AIRCRAFT_MIXTURE_POSITION|${AE_SUBSET_AIRCRAFT_PARAMS}"
    "BETTING_GAME_CONVEX_ADAPTIVE|${AE_SUBSET_BETTING_CONVEX_PARAMS}"
    "BETTING_GAME_PARALLEL|${AE_SUBSET_BETTING_PARALLEL_PARAMS}"
    "ENGAGEMENT_ADAPTIVE_5|${AE_SUBSET_ENGAGEMENT_PARAMS}"
    "GLIDER|${AE_SUBSET_GLIDER_PARAMS}"
    "SAV2_ADAPTIVE_5|${AE_SUBSET_SAV2_PARAMS}"
  )
  stage_solver_input_pairs "$subset_root" "${pairs[@]}"
}

run_solver_paper_subset() {
  local out_root="${AE_RESULTS_ROOT}/solver_paper_subset/parametric_convex"
  local subset_input_root="${AE_RESULTS_ROOT}/_solver_subset_inputs/parametric_convex"
  local solver_runs="${AE_PAPER_SOLVER_RUNS}"
  local arg

  for arg in "$@"; do
    case "$arg" in
      --no-ellipsoid|--unofficial-no-ellipsoid)
        solver_runs="$(strip_ellipsoid_runs "$solver_runs")"
        ;;
      *)
        echo "Unknown option for solver-paper-subset: $arg" >&2
        echo "Supported option: --no-ellipsoid" >&2
        exit 1
        ;;
    esac
  done

  mkdir -p "$out_root"
  stage_solver_subset_inputs "$subset_input_root"

  "$RUN_SOLVER_BIN" \
    --benchmark-input-root="${subset_input_root}" \
    --benchmark-output-root="${out_root}" \
    --runs="${solver_runs}" \
    --benchmark-timeout-seconds="${AE_PAPER_SOLVER_TIMEOUT_SECONDS}"

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

run_postprocess() {
  run-postprocess "$@"
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
  postprocess)
    run_postprocess "$@"
    ;;
  all-paper)
    run_learner_paper_subset
    run_solver_paper_subset "$@"
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
