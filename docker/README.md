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
- This artifact is built against **Gurobi Java 12.x** (`GurobiJni120`), so the host must provide a **Gurobi 12.x Linux** distribution.
- Gurobi 13.x is not ABI-compatible with this artifact and will fail at runtime.

Why Gurobi is not bundled in the Docker image:

- Gurobi is commercial software and redistribution in public artifacts is legally sensitive.
- Reviewer licenses are user-specific (WLS credentials / license terms).
- Shipping license credentials inside an image is unsafe.

Therefore the image expects the reviewer to mount their own local Gurobi installation and license file.

## 1.1) Version compatibility (must read)

The Java code inside the artifact loads:

- `libGurobiJni120.so`

So you must mount a Gurobi installation that contains that file:

- `.../lib/libGurobiJni120.so`

If you see:

- `java.lang.UnsatisfiedLinkError: no GurobiJni120 in java.library.path`

you are mounting the wrong Gurobi major version (typically 13.x).

## 1.2) Required mounts in `docker run`

All `docker run` commands in this README use these mounts:

- `-v "$GUROBI_HOME:/opt/gurobi/linux64:ro"`  
  Host Gurobi installation.
- `-v "$HOST_GUROBI_LIC:/licenses/gurobi.lic:ro"`  
  Host license file into container.
- `-v "$PWD/results:/workspace/prism_convex/prism/plotting_paper_with_ellipsoids/artifact_results"`  
  Host output folder for persistent results.

And this environment variable:

- `-e GRB_LICENSE_FILE=/licenses/gurobi.lic`

Only the **left** side of `-v host_path:container_path` is machine-specific.

## 2) Get a free academic Gurobi license (recommended: WLS)

### Step A: Create academic account and license

1. Go to Gurobi Academic Program page and sign in with institutional email:
   - https://www.gurobi.com/academia/academic-program-and-licenses/
2. Request a free academic license.
3. In the Gurobi user portal, create/access a **WLS** license.

### Step B: Create local `gurobi.lic`

Create file (recommended dedicated file for Docker runs):

- Linux/macOS: `$HOME/.gurobi/gurobi-docker-wls.lic`

With content:

```text
WLSACCESSID=...
WLSSECRET=...
LICENSEID=...
```

Secure file permissions:

```bash
chmod 600 "$HOME/.gurobi/gurobi-docker-wls.lic"
```

### Step C: Why WLS is preferred here

- Works well for container runs.
- No node-locked machine activation inside the container is required.
- Reviewers can use their own academic account.
- Keeping a separate Docker WLS file avoids changing any local non-container license setup.

## 3) Install Gurobi Optimizer on host (Linux binaries, version 12.x)

Install Gurobi Optimizer on your host from:

- https://www.gurobi.com/downloads/

Use **Gurobi 12.0.x** Linux package:

- Apple Silicon hosts: `gurobi12.0.3_armlinux64.tar.gz`
- x86_64 hosts: `gurobi12.0.3_linux64.tar.gz`

Example (Apple Silicon / ARM64):

```bash
mkdir -p "$HOME/gurobi"
tar -xzf "$HOME/Downloads/gurobi12.0.3_armlinux64.tar.gz" -C "$HOME/gurobi"
export GUROBI_HOME="$HOME/gurobi/gurobi1203/armlinux64"
```

Example (x86_64 / AMD64):

```bash
mkdir -p "$HOME/gurobi"
tar -xzf "$HOME/Downloads/gurobi12.0.3_linux64.tar.gz" -C "$HOME/gurobi"
export GUROBI_HOME="$HOME/gurobi/gurobi1203/linux64"
```

Verify required shared libraries (especially `libGurobiJni120.so`):

```bash
ls "$GUROBI_HOME/lib"/libgurobi*.so
ls "$GUROBI_HOME/lib"/libGurobiJni120.so
```

If this check fails, do not continue to Docker runs.

## 3.1) Pre-flight environment variables

Set these once in the shell before running any Docker command below:

```bash
# Example for Apple Silicon; use linux64 on x86_64 machines.
export GUROBI_HOME="$HOME/gurobi/gurobi1203/armlinux64"
export HOST_GUROBI_LIC="$HOME/.gurobi/gurobi-docker-wls.lic"

ls "$GUROBI_HOME/lib"/libGurobiJni120.so
grep -E '^(WLSACCESSID|WLSSECRET|LICENSEID)=' "$HOST_GUROBI_LIC"
```

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

Set image variable from loaded tags:

```bash
IMG="$(docker images --format '{{.Repository}}:{{.Tag}}' | grep '^prism-convex:' | head -n1)"
echo "Using image: $IMG"
```

Below, use `$IMG`.

## 6) Quick check (recommended first)

This runs a short learner and solver sanity check.
Default quick preset:

- model: `AIRCRAFT_MIXTURE_POSITION`
- run configs: `PARAMETER_TYING,PARAMETRIC_CONVEX`
- learner: `iterations=4000`, `max-episode-length=20`
- solver: per-run timeout `900` seconds

```bash
mkdir -p "$PWD/results"

docker run --rm -it \
  -v "$GUROBI_HOME:/opt/gurobi/linux64:ro" \
  -v "$HOST_GUROBI_LIC:/licenses/gurobi.lic:ro" \
  -v "$PWD/results:/workspace/prism_convex/prism/plotting_paper_with_ellipsoids/artifact_results" \
  -e GRB_LICENSE_FILE=/licenses/gurobi.lic \
  "$IMG" \
  run-ae quick
```

## 7) Reproduce paper experiments

Default subset presets:

- `learner-paper-subset` runs 6 fixed models with run configs  
  `PARAMETER_TYING,PARAMETRIC_CONVEX,LP_TO_INTERVAL_EXACT,LP_TO_INTERVAL_FAST`
- `solver-paper-subset` runs the same 6 models with run configs  
  `PARAMETER_TYING,PARAMETRIC_CONVEX,LP_TO_INTERVAL_EXACT,LP_TO_INTERVAL_FAST,ELLIPSOID,ELLIPSOID_TO_INTERVAL_EXACT,ELLIPSOID_TO_INTERVAL_FAST`
- solver subset per-run timeout: `7200` seconds

### Learner paper subset

```bash
docker run --rm -it \
  -v "$GUROBI_HOME:/opt/gurobi/linux64:ro" \
  -v "$HOST_GUROBI_LIC:/licenses/gurobi.lic:ro" \
  -v "$PWD/results:/workspace/prism_convex/prism/plotting_paper_with_ellipsoids/artifact_results" \
  -e GRB_LICENSE_FILE=/licenses/gurobi.lic \
  "$IMG" run-ae learner-paper-subset
```

### Solver paper subset

```bash
docker run --rm -it \
  -v "$GUROBI_HOME:/opt/gurobi/linux64:ro" \
  -v "$HOST_GUROBI_LIC:/licenses/gurobi.lic:ro" \
  -v "$PWD/results:/workspace/prism_convex/prism/plotting_paper_with_ellipsoids/artifact_results" \
  -e GRB_LICENSE_FILE=/licenses/gurobi.lic \
  "$IMG" run-ae solver-paper-subset
```

### Solver full benchmark set (long)

```bash
docker run --rm -it \
  -v "$GUROBI_HOME:/opt/gurobi/linux64:ro" \
  -v "$HOST_GUROBI_LIC:/licenses/gurobi.lic:ro" \
  -v "$PWD/results:/workspace/prism_convex/prism/plotting_paper_with_ellipsoids/artifact_results" \
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

- `results`

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

The mounted host path is wrong. Check `"$GUROBI_HOME/lib"`.

### `java.lang.UnsatisfiedLinkError: no GurobiJni120 in java.library.path`

You mounted an incompatible Gurobi version.

- Required: Gurobi 12.x Linux package with `libGurobiJni120.so`
- Incompatible for this artifact: Gurobi 13.x (`libGurobiJni130.so`)

### `GRB_LICENSE_FILE points to a missing file`

Your license file path/mount is wrong. Confirm:

```bash
ls "$HOST_GUROBI_LIC"
```

### `HostID mismatch (... hostid is 0)`

You mounted a node-locked license file generated by `grbgetkey`.

- For Docker runs, use a WLS license file containing:
  - `WLSACCESSID=...`
  - `WLSSECRET=...`
  - `LICENSEID=...`
- Mount that file via `HOST_GUROBI_LIC`.

### Gurobi license error when using WLS

- Check `WLSACCESSID`, `WLSSECRET`, `LICENSEID` values.
- Ensure outbound internet access from the running environment.
- Ensure no typo/extra spaces in `gurobi.lic`.
