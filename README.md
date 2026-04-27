# PRISM Artifact for *Robust Parameter Learning for Uncertain MDPs*

This repository contains the artifact used to evaluate the paper:

- **Robust Parameter Learning for Uncertain MDPs**
- **Yannik Schnitzer, Alessandro Abate, David Parker**

The reviewer-oriented paper PDF is placed in:

- `docker/Learning_Parameters_of_Uncertain_pMDPs (43).pdf`

For complete Docker setup and troubleshooting, use:

- [`docker/README.md`](docker/README.md)
- Start at Section 2 for Gurobi license/setup and Section 3 for image loading.

## Reviewer Reproduction Map

All commands below are executed inside the artifact container via `run-ae` (see [`docker/README.md`](docker/README.md) for exact `docker run` wrappers and mounts).

Path mapping for reviewer runs:

- Commands/logs use container paths like `plotting_paper_with_ellipsoids/artifact_results/...`.
- With the documented wrapper, these map to host `results/...` (same suffix after `artifact_results/`).

| Command | Reproduces | Outputs |
|---|---|---|
| `run-ae quick` | Learner + solver sanity check on one small instance | `artifact_results/learning_quick`, `artifact_results/solver_quick` |
| `run-ae quick --appendix` | Quick sanity check + ellipsoid solver runs for that instance | same as above |
| `run-ae learner-paper-subset` | Learning experiments on the fixed 6-model paper subset | `artifact_results/learning_paper_subset` |
| `run-ae solver-paper-subset` | Solver experiments for the 6-model subset (main-paper runs only) | `artifact_results/solver_paper_subset` |
| `run-ae solver-paper-subset --appendix` | Adds appendix ellipsoid solver runs | `artifact_results/solver_paper_subset` |
| `run-ae all-paper` | `learner-paper-subset` then `solver-paper-subset` | both folders above |
| `run-ae all-paper --appendix` | `all-paper` plus appendix ellipsoid runs | both folders above |
| `run-ae solver-full` | Solver on full benchmark input set (main-paper runs only) | `artifact_results/solver_full` |
| `run-ae solver-full --appendix` | Full solver sweep including appendix ellipsoid runs | `artifact_results/solver_full` |

## Why the Default is a Subset (and Not Full)

The default reviewer route focuses on `solver-paper-subset` with main-paper runs only. Appendix ellipsoid runs are opt-in via `--appendix`.

- Main subset workload: `6 instances x 4 run configurations = 24 runs`
- Appendix subset workload: `6 instances x 7 run configurations = 42 runs`
- Main full workload: `19 instances x 4 run configurations = 76 runs`
- Appendix full workload: `19 instances x 7 run configurations = 133 runs`
- Default per-run timeout: `7200s` (2h)

Upper-bound timeout budget:

- Main subset: `48h`
- Appendix subset: `84h`
- Main full: `152h`
- Appendix full: `266h`

Most timeout-prone configurations:

- `ELLIPSOID`
- `ELLIPSOID_TO_INTERVAL_EXACT`
- `ELLIPSOID_TO_INTERVAL_FAST`

## Full Postprocessing (Included)

Postprocessing is exposed through `run-ae postprocess ...` and includes all three reviewer-facing outputs. With the wrapper in [`docker/README.md`](docker/README.md), this now works out of the box (no `/workspace/...` script path overrides).

```bash
run-ae postprocess all
run-ae postprocess tables
run-ae postprocess stats
run-ae postprocess learning-plots
```

`run-ae postprocess learning-plots` requires learner outputs (from `run-ae learner-paper-subset` or `run-ae all-paper`).

Generated artifacts:

- `postprocess/tables/benchmark_results_table_landscape_split_runtime.tex`
- `postprocess/stats/benchmark_stats_table.tex`
- `postprocess/learning_plots/<MODEL>/<PARAM_DIR>/*.pdf`

Script entrypoints:

- `artifact_eval/table_row_printer.py`
- `artifact_eval/plot_results.py`
- `artifact_eval/learning_plot_results.py`

## Upstream PRISM Information

This artifact builds on PRISM (Probabilistic Symbolic Model Checker).

- Manual: https://www.prismmodelchecker.org/manual/
- Installation docs: https://www.prismmodelchecker.org/manual/InstallingPRISM/Instructions
- Local manual copy: `manual/index.html`
