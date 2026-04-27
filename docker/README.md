# QEST+FORMATS 2026 Artifact Guide (Reviewer-Focused)

This artifact accompanies the paper:

- **Robust Parameter Learning for Uncertain MDPs**
- **Authors:** Yannik Schnitzer, Alessandro Abate, David Parker



## What Each Command Reproduces

| Command | What it reproduces | Main output folder |
|---|---|---|
| `run-ae quick` | End-to-end smoke test of learner + solver on one small instance (`AIRCRAFT_MIXTURE_POSITION`) | `artifact_results/learning_quick`, `artifact_results/solver_quick` |
| `run-ae quick --appendix` | Quick smoke test plus ellipsoid solver runs for the same instance | same as above |
| `run-ae learner-paper-subset` | Learning experiments on the fixed 6-model paper subset with the 4 learning run configurations | `artifact_results/learning_paper_subset` |
| `run-ae solver-paper-subset` | Solver reproduction on the same 6-model paper subset with main-paper solver runs only (`PARAMETER_TYING`, `PARAMETRIC_CONVEX`, `LP_TO_INTERVAL_EXACT`, `LP_TO_INTERVAL_FAST`) | `artifact_results/solver_paper_subset` |
| `run-ae solver-paper-subset --appendix` | Adds appendix ellipsoid solver runs (`ELLIPSOID`, `ELLIPSOID_TO_INTERVAL_EXACT`, `ELLIPSOID_TO_INTERVAL_FAST`) on top of the main-paper runs | `artifact_results/solver_paper_subset` |
| `run-ae all-paper` | Convenience command: `learner-paper-subset` then `solver-paper-subset` | Both folders above |
| `run-ae all-paper --appendix` | `all-paper` plus appendix ellipsoid solver runs | Both folders above |
| `run-ae solver-full` | Solver on the full benchmark input set with main-paper solver runs only | `artifact_results/solver_full` |
| `run-ae solver-full --appendix` | `solver-full` plus appendix ellipsoid solver runs | `artifact_results/solver_full` |
| `run-ae postprocess all` | Full postprocessing pipeline (tables + benchmark stats + learning plots) | `artifact_results/postprocess` |
| `run-ae postprocess tables` | Solver result LaTeX table (intervals + runtime) | `artifact_results/postprocess/tables` |
| `run-ae postprocess stats` | Benchmark statistics LaTeX table | `artifact_results/postprocess/stats` |
| `run-ae postprocess learning-plots` | Learning PDF plots from learner outputs | `artifact_results/postprocess/learning_plots` |

## Why a Subset Is Used

The default reviewer target is `solver-paper-subset` (6 instances, one per model), not `solver-full` (19 instances), to keep wall-clock runtime practical.

- Main-paper solver subset jobs: `6 instances x 4 run configurations = 24 runs`
- Appendix solver subset jobs: `6 instances x 7 run configurations = 42 runs`
- Main-paper solver full jobs: `19 instances x 4 run configurations = 76 runs`
- Appendix solver full jobs: `19 instances x 7 run configurations = 133 runs`
- Default per-run timeout: `7200s` (2h), default timeout mode: `soft` (in-process)

Ellipsoid runs are the most timeout-prone, so they are now opt-in via `--appendix`.

## 1) Prerequisites

You need:

- Docker
- A **Linux** Gurobi installation on the host
- A valid Gurobi license file (recommended: WLS)

Important compatibility note:

- This artifact expects **Gurobi 12.x** (`libGurobiJni120.so`)
- Gurobi 13.x is not ABI-compatible with this build

Gurobi is not bundled in the image (license and redistribution reasons), so you mount your own host installation and license file at runtime.


## 2) Set Up Gurobi (License + Host Installation)

This section is intentionally step-by-step so reviewers can start from zero.

### 2.1 Create an Academic Gurobi Account and WLS License

1. Open: https://www.gurobi.com/academia/academic-program-and-licenses/
2. Sign in with your institutional email and complete the academic eligibility flow.
3. In the Gurobi user portal, create a **WLS** license (recommended for Docker).
4. Copy the three WLS fields from the portal:
   - `WLSACCESSID`
   - `WLSSECRET`
   - `LICENSEID`

### 2.2 Create a Dedicated Docker License File

Create a dedicated license file (recommended):

```bash
mkdir -p "$HOME/.gurobi"
cat > "$HOME/.gurobi/gurobi-docker-wls.lic" <<'EOF'
WLSACCESSID=PASTE_YOURS
WLSSECRET=PASTE_YOURS
LICENSEID=PASTE_YOURS
EOF
chmod 600 "$HOME/.gurobi/gurobi-docker-wls.lic"
```

Validate content shape (redacted output):

```bash
awk -F= '/^(WLSACCESSID|WLSSECRET|LICENSEID)=/{print $1"=***"}' "$HOME/.gurobi/gurobi-docker-wls.lic"
```

### 2.3 Install Gurobi 12.x Linux Binaries on Host

Download from:

- https://www.gurobi.com/downloads/

Use **Gurobi 12.x** Linux package matching your CPU:

- ARM64: `gurobi12.0.x_armlinux64.tar.gz`
- x86_64: `gurobi12.0.x_linux64.tar.gz`

Example extraction:

```bash
mkdir -p "$HOME/gurobi"
# adjust file name as needed:
tar -xzf "$HOME/Downloads/gurobi12.0.3_armlinux64.tar.gz" -C "$HOME/gurobi"
```

Export runtime variables:

```bash
# ARM64 hosts:
export GUROBI_HOME="$HOME/gurobi/gurobi1203/armlinux64"

# x86_64 hosts (use this instead on x86_64):
# export GUROBI_HOME="$HOME/gurobi/gurobi1203/linux64"

export HOST_GUROBI_LIC="$HOME/.gurobi/gurobi-docker-wls.lic"
```

Hard checks before Docker:

```bash
ls "$GUROBI_HOME/lib"/libgurobi*.so
ls "$GUROBI_HOME/lib"/libGurobiJni120.so
test -f "$HOST_GUROBI_LIC" && echo "license file found"
```

If `libGurobiJni120.so` is missing, the version/path is wrong for this artifact.

## 3) Load the Image Archive (Step-by-Step)

### 3.1 Detect Host Architecture

```bash
uname -m
```

Use this mapping:

- `x86_64` -> load `prism-convex-ae-amd64.tar.gz`
- `arm64` or `aarch64` -> load `prism-convex-ae-arm64.tar.gz`

### 3.2 Load the Matching Archive

From the folder containing the archive:

```bash
gunzip -c prism-convex-ae-<amd64|arm64>.tar.gz | docker load
```

Optional integrity check before load:

```bash
shasum -a 256 prism-convex-ae-<amd64|arm64>.tar.gz
```

### 3.3 Confirm Image Is Available and Capture Tag

```bash
docker images | grep prism-convex
IMG="$(docker images --format '{{.Repository}}:{{.Tag}}' | grep '^prism-convex:' | head -n1)"
echo "Using image: $IMG"
```

If `IMG` is empty, the load did not succeed.

## 4) Define a Reusable Docker Wrapper

```bash
ARTIFACT_ROOT="$(pwd)"
RESULTS_DIR="$ARTIFACT_ROOT/results"
mkdir -p "$RESULTS_DIR"

ae_run() {
  docker run --rm -it \
    -v "$GUROBI_HOME:/opt/gurobi/linux64:ro" \
    -v "$HOST_GUROBI_LIC:/licenses/gurobi.lic:ro" \
    -v "$RESULTS_DIR:/workspace/prism_convex/prism/plotting_paper_with_ellipsoids/artifact_results" \
    -e GRB_LICENSE_FILE=/licenses/gurobi.lic \
    -e AE_POSTPROCESS_INPUT_ROOT \
    -e AE_POSTPROCESS_LEARNING_INPUT_ROOT \
    -e AE_POSTPROCESS_OUTPUT_ROOT \
    -e AE_TABLE_SCRIPT \
    -e AE_STATS_SCRIPT \
    -e AE_LEARNING_PLOT_SCRIPT \
    "$IMG" run-ae "$@"
}
```

Important:

- Run `ae_run ...` from the same shell where `ARTIFACT_ROOT`/`RESULTS_DIR` were defined.
- This avoids accidental nested mounts when calling commands from subdirectories.

Path mapping (container -> host):

- The container writes to `plotting_paper_with_ellipsoids/artifact_results/...`.
- With the wrapper above, this is mounted to host `results/...`.
- Example:
  `plotting_paper_with_ellipsoids/artifact_results/solver_quick/parametric_convex`
  appears on host as
  `results/solver_quick/parametric_convex`.

## 5) Recommended Reproduction Flow

1. Quick sanity check:

```bash
ae_run quick
```

2. Reproduce learner paper subset:

```bash
ae_run learner-paper-subset
```

3. Reproduce solver paper subset (main-paper runs only):

```bash
ae_run solver-paper-subset
```

4. Optional appendix solver runs (ellipsoid variants):

```bash
ae_run solver-paper-subset --appendix
```

5. Optional full solver sweep (long):

```bash
ae_run solver-full
```

Timeout mode override (optional):

```bash
# Default is soft (in-process per-run timeout, no subprocess spawning)
ae_run solver-paper-subset --benchmark-timeout-mode=soft

# Hard mode uses per-run subprocesses and force-kill on timeout
ae_run solver-paper-subset --benchmark-timeout-mode=hard
```

## 6) Full Postprocessing Walkthrough

Postprocessing is driven by `run-ae postprocess ...`, which delegates to `run-postprocess`.
It works out of the box with the default `ae_run` wrapper above (no `/workspace/...` script overrides needed).

Run complete postprocessing:

```bash
ae_run postprocess all
```

Or run individual modes:

```bash
ae_run postprocess tables
ae_run postprocess stats
ae_run postprocess learning-plots
```

Note:

- `learning-plots` needs learner outputs (run `ae_run learner-paper-subset` or `ae_run all-paper` first).
- `tables`/`stats` can be generated from partial solver outputs, but missing runs/instances appear as blanks (`--`) or fewer rows.
- Defaults auto-detect available roots:
  solver input `solver_paper_subset -> solver_quick -> solver_full`,
  learning input `learning_paper_subset -> learning_quick`.

What each mode produces:

- `tables` -> `postprocess/tables/benchmark_results_table_landscape_split_runtime.tex`
- `stats` -> `postprocess/stats/benchmark_stats_table.tex`
- `learning-plots` -> `postprocess/learning_plots/<MODEL>/<PARAM_DIR>/*.pdf`

Python scripts invoked:

- `artifact_eval/table_row_printer.py`
- `artifact_eval/plot_results.py`
- `artifact_eval/learning_plot_results.py`

Default postprocess roots (inside container/workdir, auto-detected):

- Solver input root: `solver_paper_subset` fallback to `solver_quick` then `solver_full`
- Learning input root: `learning_paper_subset` fallback to `learning_quick`
- Output root: `postprocess` / `postprocess_quick` / `postprocess_full` (based on selected solver input)

Host equivalents (with the wrapper mount in Section 4):

- `results/solver_paper_subset/parametric_convex`
- `results/learning_paper_subset/parametric_convex`
- `results/postprocess`

Override roots if needed by passing environment variables (wrapper forwards them):

```bash
AE_POSTPROCESS_INPUT_ROOT=plotting_paper_with_ellipsoids/artifact_results/solver_full/parametric_convex \
AE_POSTPROCESS_LEARNING_INPUT_ROOT=plotting_paper_with_ellipsoids/artifact_results/learning_paper_subset/parametric_convex \
AE_POSTPROCESS_OUTPUT_ROOT=plotting_paper_with_ellipsoids/artifact_results/postprocess_full \
ae_run postprocess all
```

## 7) Output Locations on Host

Because of the bind mount, container outputs are written to:

- `./results`

Key subfolders:

- `results/learning_quick/parametric_convex`
- `results/solver_quick/parametric_convex`
- `results/learning_paper_subset/parametric_convex`
- `results/solver_paper_subset/parametric_convex`
- `results/solver_full/parametric_convex`
- `results/postprocess/tables`
- `results/postprocess/stats`
- `results/postprocess/learning_plots`

## 8) Troubleshooting

### `ERROR: required command not found: docker`

Docker is not installed or not running.

### `No libgurobi*.so found` or `No libGurobiJni*.so found`

`$GUROBI_HOME` is wrong or incomplete. Check:

```bash
ls "$GUROBI_HOME/lib"/libgurobi*.so
ls "$GUROBI_HOME/lib"/libGurobiJni120.so
```

### `java.lang.UnsatisfiedLinkError: no GurobiJni120 in java.library.path`

Wrong Gurobi major version is mounted. Use 12.x.

### `GRB_LICENSE_FILE points to a missing file`

`$HOST_GUROBI_LIC` path is incorrect.

### `HostID mismatch (... hostid is 0)`

You are using a node-locked license for a container run. Use a WLS license file.

### WLS license errors

Check:

- `WLSACCESSID`, `WLSSECRET`, `LICENSEID` values
- outbound internet access from the runtime environment
- no whitespace/typos in `gurobi.lic`

### `Too many sessions, X active sessions for a baseline of Y`

This is a Gurobi WLS session-cap issue. Practical mitigations:

- Prefer default `soft` timeout mode (no per-run child JVM spawn): `--benchmark-timeout-mode=soft`
- Run main-paper solver set first (`run-ae solver-paper-subset`), then appendix only if needed (`--appendix`)
- Stop concurrent runs using the same license and wait/clear sessions in the Gurobi portal

### Packaging/export unexpectedly huge (e.g., tens of GB)

If `docker/package-artifact.sh` spends a long time on `Exporting Docker image` and produces very large archives, you are likely packaging with local result/archive files included in build context. This is fixed by current `.dockerignore`; pull latest changes and remove stale local archives before repackaging:

```bash
rm -rf artifact_dist
ARCHES=amd64,arm64 SKIP_EXISTING=0 bash docker/package-artifact.sh
```

Appendix solver flow:

```bash
ae_run solver-paper-subset --appendix
```
