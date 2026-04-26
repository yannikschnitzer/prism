# Postprocessing Script Contract

This folder is reserved for reviewer-facing postprocessing scripts.

The Docker helper `run-postprocess` expects:

- `artifact_eval/table_row_printer.py`
- `artifact_eval/plot_results.py`

Expected CLI interface for both scripts:

```bash
python3 <script> --input-root <results_dir> --output-root <out_dir>
```

Current default `input-root` used by `run-postprocess`:

- `plotting_paper_with_ellipsoids/artifact_results/solver_paper_subset/parametric_convex`

Current default `output-root` used by `run-postprocess`:

- `plotting_paper_with_ellipsoids/artifact_results/postprocess`
