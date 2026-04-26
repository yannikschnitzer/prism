# QEST+FORMATS 2026 Artifact Instructions (For Reviewers)

This README is only for running and checking the artifact.

The artifact evaluates:

- `learning.ParametricConvex.ParametricConvexLearner`
- `learning.ParametricConvex.ParametricConvexSolver`

The main entrypoint for reviewers is:

- `run-ae` (quick check + paper subset + full runs)

## 0) Important: image transferability

Docker images here are Linux container images.

- They are usable on Linux/macOS/Windows hosts **if Docker is installed**.
- They are **not automatically cross-architecture**.

In practice:

- An image built on `linux/arm64` is for ARM64 machines.
- An image built on `linux/amd64` is for x86_64 machines.

`docker/package-artifact.sh` builds **both** by default and exports:

- `prism-convex-ae-amd64.tar.gz`
- `prism-convex-ae-arm64.tar.gz`

## 1) What you need

- Docker (Linux containers)
- Linux Gurobi installation on your host machine
- A valid Gurobi license (free academic license is sufficient)

Important:

- This artifact does **not** ship Gurobi itself or a license file.
- If you use a WLS license, internet access is required while running experiments.

Why Gurobi is not bundled in the Docker image:

- Gurobi is commercial software and redistribution in public artifacts is legally sensitive.
- Reviewer licenses are user-specific (WLS credentials / license terms).
- Shipping license credentials inside an image is unsafe.

Therefore the image expects the reviewer to mount their own local Gurobi installation and license file.

## 2) Get a free academic Gurobi license (recommended: WLS)

### Step A: Create academic account and license

1. Go to Gurobi Academic Program page and sign in with institutional email:
   - https://www.gurobi.com/academia/academic-program-and-licenses/
2. Request a free academic license.
3. In the Gurobi user portal, create/access a **WLS** license.

### Step B: Create local `gurobi.lic`

Create file:

- Linux/macOS: `$HOME/.gurobi/gurobi.lic`

With content:

```text
WLSACCESSID=...
WLSSECRET=...
LICENSEID=...
```

Secure file permissions:

```bash
chmod 600 "$HOME/.gurobi/gurobi.lic"
```

### Step C: Why WLS is preferred here

- Works well for container runs.
- No node-locked machine activation inside the container is required.
- Reviewers can use their own academic account.

## 3) Install Gurobi Optimizer on host (Linux binaries)

Install Gurobi Optimizer on your host from:

- https://www.gurobi.com/downloads/

Example install path:

- `/opt/gurobi1203/linux64`

Verify required shared libraries:

```bash
ls /opt/gurobi1203/linux64/lib/libgurobi*.so
ls /opt/gurobi1203/linux64/lib/libGurobiJni*.so
```

If your path is different, use that path in the Docker run command below.

## 4) Build the artifact image

From repository root:

```bash
docker build -t prism-convex:ae -f Dockerfile .
```

## 5) If you received prebuilt image archives (`.tar.gz`)

Pick the correct one:

- x86_64 host: `prism-convex-ae-amd64.tar.gz`
- ARM64 host: `prism-convex-ae-arm64.tar.gz`

Check host architecture:

```bash
uname -m
```

Load chosen archive:

```bash
gunzip -c prism-convex-ae-<amd64|arm64>.tar.gz | docker load
docker images | grep prism-convex
```

After loading, use image tag:

- `prism-convex:ae-amd64` or
- `prism-convex:ae-arm64`

Set:

```bash
IMG=prism-convex:ae-<amd64|arm64>
```

Below, use `$IMG`.

## 6) Quick check (recommended first)

This runs a short learner and solver sanity check.

```bash
mkdir -p "$PWD/prism/plotting_paper_with_ellipsoids/artifact_results"

docker run --rm -it \
  -v /opt/gurobi1203/linux64:/opt/gurobi/linux64:ro \
  -v "$HOME/.gurobi/gurobi.lic:/licenses/gurobi.lic:ro" \
  -v "$PWD/prism/plotting_paper_with_ellipsoids/artifact_results:/workspace/prism_convex/prism/plotting_paper_with_ellipsoids/artifact_results" \
  -e GRB_LICENSE_FILE=/licenses/gurobi.lic \
  "$IMG" \
  run-ae quick
```

## 7) Reproduce paper experiments

### Learner paper subset

```bash
docker run --rm -it \
  -v /opt/gurobi1203/linux64:/opt/gurobi/linux64:ro \
  -v "$HOME/.gurobi/gurobi.lic:/licenses/gurobi.lic:ro" \
  -v "$PWD/prism/plotting_paper_with_ellipsoids/artifact_results:/workspace/prism_convex/prism/plotting_paper_with_ellipsoids/artifact_results" \
  -e GRB_LICENSE_FILE=/licenses/gurobi.lic \
  "$IMG" run-ae learner-paper-subset
```

### Solver paper subset

```bash
docker run --rm -it \
  -v /opt/gurobi1203/linux64:/opt/gurobi/linux64:ro \
  -v "$HOME/.gurobi/gurobi.lic:/licenses/gurobi.lic:ro" \
  -v "$PWD/prism/plotting_paper_with_ellipsoids/artifact_results:/workspace/prism_convex/prism/plotting_paper_with_ellipsoids/artifact_results" \
  -e GRB_LICENSE_FILE=/licenses/gurobi.lic \
  "$IMG" run-ae solver-paper-subset
```

### Solver full benchmark set (long)

```bash
docker run --rm -it \
  -v /opt/gurobi1203/linux64:/opt/gurobi/linux64:ro \
  -v "$HOME/.gurobi/gurobi.lic:/licenses/gurobi.lic:ro" \
  -v "$PWD/prism/plotting_paper_with_ellipsoids/artifact_results:/workspace/prism_convex/prism/plotting_paper_with_ellipsoids/artifact_results" \
  -e GRB_LICENSE_FILE=/licenses/gurobi.lic \
  -e JAVA_INITIAL_HEAP=12g \
  -e JAVA_MAX_HEAP=28g \
  "$IMG" run-ae solver-full
```

## 8) Where outputs are written

All `run-ae` commands write to:

- `plotting_paper_with_ellipsoids/artifact_results`

Subdirectories:

- `learning_quick/parametric_convex`
- `solver_quick/parametric_convex`
- `learning_paper_subset/parametric_convex`
- `solver_paper_subset/parametric_convex`
- `solver_full/parametric_convex`

Because of the bind mount, these appear on your host in:

- `prism/plotting_paper_with_ellipsoids/artifact_results`

## 9) Table/plot postprocessing

Prepared hook:

```bash
run-ae postprocess all
```

Expected scripts:

- `artifact_eval/table_row_printer.py`
- `artifact_eval/plot_results.py`

Expected interface:

```bash
python3 <script> --input-root <results_dir> --output-root <out_dir>
```

Default postprocess input:

- `plotting_paper_with_ellipsoids/artifact_results/solver_paper_subset/parametric_convex`

Default postprocess output:

- `plotting_paper_with_ellipsoids/artifact_results/postprocess`

## 10) Troubleshooting

### `ERROR: required command not found: docker`

Docker is not installed/running on your machine.

### `No libgurobi*.so found` or `No libGurobiJni*.so found`

The mounted host path is wrong. Check `/opt/gurobi.../linux64/lib`.

### `GRB_LICENSE_FILE points to a missing file`

Your license file path/mount is wrong. Confirm:

```bash
ls "$HOME/.gurobi/gurobi.lic"
```

### Gurobi license error when using WLS

- Check `WLSACCESSID`, `WLSSECRET`, `LICENSEID` values.
- Ensure outbound internet access from the running environment.
- Ensure no typo/extra spaces in `gurobi.lic`.
