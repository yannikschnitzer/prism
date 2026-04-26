FROM ubuntu:24.04

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y --no-install-recommends \
    bash \
    bison \
    build-essential \
    ca-certificates \
    file \
    flex \
    make \
    openjdk-21-jdk-headless \
    python3 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace/prism_convex
COPY . .

# Build PRISM (Java classes + native libs for Linux).
WORKDIR /workspace/prism_convex/prism
RUN make

# Convenience defaults used by docker/run-benchmarks.sh
ENV PRISM_DIR=/workspace/prism_convex/prism
ENV BENCHMARK_INPUT_ROOT=plotting_paper_with_ellipsoids/results_uniform_solving_new/parametric_convex
ENV BENCHMARK_TIMEOUT_SECONDS=7200
ENV JAVA_INITIAL_HEAP=2g
ENV JAVA_MAX_HEAP=16g
ENV AE_RESULTS_ROOT=plotting_paper_with_ellipsoids/artifact_results

# By default, expect Linux Gurobi libs to be mounted here.
ENV GUROBI_LIB_DIR=/opt/gurobi/linux64/lib

# Keep PRISM libs first; add Gurobi if present.
ENV LD_LIBRARY_PATH=/workspace/prism_convex/prism/lib:/opt/gurobi/linux64/lib

COPY docker/run-benchmarks.sh /usr/local/bin/run-benchmarks
COPY docker/run-learner.sh /usr/local/bin/run-learner
COPY docker/run-ae.sh /usr/local/bin/run-ae
COPY docker/run-postprocess.sh /usr/local/bin/run-postprocess
RUN chmod +x /usr/local/bin/run-benchmarks /usr/local/bin/run-learner /usr/local/bin/run-ae /usr/local/bin/run-postprocess

WORKDIR /workspace/prism_convex/prism
CMD ["bash"]
