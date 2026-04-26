# PRISM Convex Artifact (QEST+FORMATS 2026)

This directory contains an Artifact Evaluation workflow for the experiments based on:

- `learning.ParametricConvex.ParametricConvexLearner`
- `learning.ParametricConvex.ParametricConvexSolver`

It provides:

- a Docker image build (`../Dockerfile`),
- wrapper scripts:
  - `run-learner` (learner entrypoint),
  - `run-benchmarks` (solver benchmark entrypoint),
  - `run-ae` (quick/paper/full presets).

## 1) Host prerequisites

- Docker Engine / Docker Desktop (Linux containers).
- Linux Gurobi installation on the host (mounted into the container).
- A valid Gurobi license file (recommended: WLS).
- Recommended resources for larger runs: 32 GB RAM, multi-core CPU.
- Default JVM settings in the image are conservative (`-Xms2g -Xmx8g`). For larger runs, override with `-e JAVA_INITIAL_HEAP=12g -e JAVA_MAX_HEAP=28g`.

Notes:

- Gurobi JNI and native libs are architecture-specific; use matching binaries for your container architecture (`linux/amd64` or `linux/arm64`).
- The artifact itself does not need cloud services.

## 2) Free Gurobi academic setup (recommended)

For academic reviewers/authors, Gurobi provides free academic licenses.

1. Create/login to a Gurobi account with institutional email:
   - https://www.gurobi.com/academia/academic-program-and-licenses/
2. Request an academic license (WLS is recommended for containers).
3. Create a local license file, e.g. `$HOME/.gurobi/gurobi.lic`.

Typical WLS `gurobi.lic` format:

```text
WLSACCESSID=...
WLSSECRET=...
LICENSEID=...
```

Container note: WLS requires outbound network access to Gurobi's licensing service during execution.

## 3) Install Gurobi Optimizer (host)

Install Linux Gurobi on host, e.g.:

- `/opt/gurobi1203/linux64`

Verify required libraries exist:

```bash
ls /opt/gurobi1203/linux64/lib/libgurobi*.so
ls /opt/gurobi1203/linux64/lib/libGurobiJni*.so
```

## 4) Build Docker image

From repository root:

```bash
docker build -t prism-convex:ae -f Dockerfile .
```

Optional per-architecture builds:

```bash
docker buildx build --platform linux/amd64 -t prism-convex:ae-amd64 --load .
docker buildx build --platform linux/arm64 -t prism-convex:ae-arm64 --load .
```

## 5) Run quick-check (Phase I friendly)

This runs:

- a short learner smoke test,
- a short solver subset run.

```bash
mkdir -p "$PWD/prism/plotting_paper_with_ellipsoids/artifact_results"

docker run --rm -it \
  -v /opt/gurobi1203/linux64:/opt/gurobi/linux64:ro \
  -v "$HOME/.gurobi/gurobi.lic:/licenses/gurobi.lic:ro" \
  -v "$PWD/prism/plotting_paper_with_ellipsoids/artifact_results:/workspace/prism_convex/prism/plotting_paper_with_ellipsoids/artifact_results" \
  -e GRB_LICENSE_FILE=/licenses/gurobi.lic \
  prism-convex:ae \
  run-ae quick
```

Output:

- `prism/plotting_paper_with_ellipsoids/artifact_results/learning_quick/parametric_convex/...`
- `prism/plotting_paper_with_ellipsoids/artifact_results/solver_quick/parametric_convex/...`

## 6) Reproduce paper subset

`run-ae learner-paper-subset` and `run-ae solver-paper-subset` cover:

- `AIRCRAFT_MIXTURE_POSITION`
- `BETTING_GAME_CONVEX_ADAPTIVE`
- `BETTING_GAME_PARALLEL`
- `ENGAGEMENT_ADAPTIVE_5`
- `GLIDER`
- `SAV2_ADAPTIVE_5`

Default run configurations:

- Learner subset: `PARAMETER_TYING,PARAMETRIC_CONVEX,LP_TO_INTERVAL_EXACT,LP_TO_INTERVAL_FAST`
- Solver subset: `PARAMETER_TYING,PARAMETRIC_CONVEX,LP_TO_INTERVAL_EXACT,LP_TO_INTERVAL_FAST,ELLIPSOID,ELLIPSOID_TO_INTERVAL_EXACT,ELLIPSOID_TO_INTERVAL_FAST`

### Learner subset

```bash
docker run --rm -it \
  -v /opt/gurobi1203/linux64:/opt/gurobi/linux64:ro \
  -v "$HOME/.gurobi/gurobi.lic:/licenses/gurobi.lic:ro" \
  -v "$PWD/prism/plotting_paper_with_ellipsoids/artifact_results:/workspace/prism_convex/prism/plotting_paper_with_ellipsoids/artifact_results" \
  -e GRB_LICENSE_FILE=/licenses/gurobi.lic \
  prism-convex:ae \
  run-ae learner-paper-subset
```

### Solver subset

```bash
docker run --rm -it \
  -v /opt/gurobi1203/linux64:/opt/gurobi/linux64:ro \
  -v "$HOME/.gurobi/gurobi.lic:/licenses/gurobi.lic:ro" \
  -v "$PWD/prism/plotting_paper_with_ellipsoids/artifact_results:/workspace/prism_convex/prism/plotting_paper_with_ellipsoids/artifact_results" \
  -e GRB_LICENSE_FILE=/licenses/gurobi.lic \
  prism-convex:ae \
  run-ae solver-paper-subset
```

### Solver full benchmark set

```bash
docker run --rm -it \
  -v /opt/gurobi1203/linux64:/opt/gurobi/linux64:ro \
  -v "$HOME/.gurobi/gurobi.lic:/licenses/gurobi.lic:ro" \
  -v "$PWD/prism/plotting_paper_with_ellipsoids/artifact_results:/workspace/prism_convex/prism/plotting_paper_with_ellipsoids/artifact_results" \
  -e GRB_LICENSE_FILE=/licenses/gurobi.lic \
  -e JAVA_INITIAL_HEAP=12g \
  -e JAVA_MAX_HEAP=28g \
  prism-convex:ae \
  run-ae solver-full
```

## 7) Direct wrappers (advanced)

Inside the container, these are available:

- `run-learner`
- `run-benchmarks`
- `run-ae`

Examples:

```bash
# Learner on one model with overrides
run-learner \
  --model=BETTING_GAME_PARALLEL \
  --runs=PARAMETER_TYING,PARAMETRIC_CONVEX \
  --iterations=4000 \
  --max-episode-length=6 \
  --param=n=6,p_1=0.55,p_2=0.53 \
  --output-root=plotting_paper_with_ellipsoids/artifact_results/manual_learning/parametric_convex
```

```bash
# Solver benchmark reproduction for one model
run-benchmarks \
  --benchmark-input-root=plotting_paper_with_ellipsoids/results_uniform_solving_new/parametric_convex \
  --benchmark-output-root=plotting_paper_with_ellipsoids/artifact_results/manual_solver/parametric_convex \
  --model=GLIDER \
  --runs=PARAMETER_TYING,PARAMETRIC_CONVEX,LP_TO_INTERVAL_EXACT,LP_TO_INTERVAL_FAST \
  --benchmark-timeout-seconds=7200
```

## 8) Runtime/network notes

- If using WLS, network access is required during execution for license validation.
- If using an offline/local license type, execution can be fully offline after image build.
- The included benchmark input data for solver runs is local in the repository (`plotting_paper_with_ellipsoids/results_uniform_solving_new/parametric_convex`).

## 9) Packaging for AE submission

Use the helper script from repo root:

```bash
./docker/package-artifact.sh
```

With paper PDF included:

```bash
PAPER_PDF=/absolute/path/to/paper.pdf ./docker/package-artifact.sh
```

Outputs are created in `artifact_dist/` by default:

- `prism-convex-ae-image.tar.gz`
- `qest-formats-2026-ae-artifact.tar.gz`
- `qest-formats-2026-ae-artifact.tar.gz.sha256`

Manual alternative:

```bash
docker save prism-convex:ae | gzip > prism-convex-ae-image.tar.gz
```

Create artifact archive containing at least:

- this README,
- `COPYING.txt` (license),
- paper PDF,
- the Docker image tarball (`prism-convex-ae-image.tar.gz`).

SHA-256 checksum:

```bash
shasum -a 256 artifact.tar.gz
```

## 10) Clean-build workflow recommendation

- Commit only AE-relevant files on a dedicated branch.
- Push branch and clone it on a clean machine.
- Build there with `docker build ...` and package with `./docker/package-artifact.sh`.

Do not use `git pull` inside the Dockerfile:

- it makes builds non-deterministic (depends on remote HEAD),
- it requires network access at build time,
- it weakens future-proofness for artifact evaluation.
