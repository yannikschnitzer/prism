# Postprocessing Scripts

This folder contains reviewer-facing postprocessing scripts for solver outputs.

## Scripts

- `artifact_eval/table_row_printer.py`
  - Generates:
    - `benchmark_results_table_landscape_split_runtime.tex`
  - Purpose:
    - aggregate robust/optimistic intervals, relative gaps, and split runtime columns.

- `artifact_eval/plot_results.py`
  - Generates:
    - `benchmark_stats_table.tex`
  - Purpose:
    - summarize benchmark characteristics (states, transitions, parameters, expressions, property).

- `artifact_eval/learning_plot_results.py`
  - Generates:
    - per-instance learning performance/guarantee PDF
    - per-instance runtime PDF
    - per-instance legend PDF
  - Purpose:
    - aggregate learner outputs over seeds and render publication-style learning curves.

## CLI Interface

Both scripts implement:

```bash
python3 <script> --input-root <results_dir> --output-root <out_dir>
```

Optional flags are available (e.g. `--output-file`, `--debug-list`).

## `run-postprocess` Integration

- `run-postprocess tables` -> runs `table_row_printer.py` into `<postprocess-root>/tables`
- `run-postprocess stats` -> runs `plot_results.py` into `<postprocess-root>/stats`
- `run-postprocess learning-plots` -> runs `learning_plot_results.py` into `<postprocess-root>/learning_plots`
- `run-postprocess plots` -> alias for `learning-plots`
- `run-postprocess all` -> runs all three

Default input root:

- `plotting_paper_with_ellipsoids/artifact_results/solver_paper_subset/parametric_convex`

Default output root:

- `plotting_paper_with_ellipsoids/artifact_results/postprocess`

Default learning input root:

- `plotting_paper_with_ellipsoids/artifact_results/learning_paper_subset/parametric_convex`
