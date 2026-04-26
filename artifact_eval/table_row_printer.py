#!/usr/bin/env python3
"""
Generate a LaTeX table from solver benchmark CSV/YAML outputs.

This script is reviewer-facing and is intended to be called via:
  run-ae postprocess tables
or directly:
  python3 artifact_eval/table_row_printer.py --input-root <dir> --output-root <dir>
"""

from __future__ import annotations

import argparse
import math
import re
from collections import defaultdict
from pathlib import Path

import numpy as np
import pandas as pd
import yaml


BENCHMARK_NAME_MAP = {
    "UAV": "UAV",
    "UAV_CONVEX": "UAV",
    "AIRCRAFT_COLLISION": "Aircraft",
    "AIRCRAFT": "Aircraft",
    "AIRCRAFT_MIXTURE_ONEMOD_ADAPTIVE": "Aircraft",
    "AIRCRAFT_MIXTURE_POSITION": "Aircraft",
    "FIREWIRE": "Firewire",
    "SEMI_AUTONOMOUS_VEHICLE": "Semi-Auton. Vehicle",
    "SEMIAUTONOMOUS_VEHICLE": "Semi-Auton. Vehicle",
    "BETTING_GAME": "Betting",
    "BETTING_GAME_CONVEX": "Betting Game",
    "BETTING_GAME_CONVEX_ADAPTIVE": "Betting Game",
    "BETTING_GAME_PARALLEL": "Parallel Betting",
    "CHAIN": "Chain",
    "DRONE_MIXTURE": "Drone Mixture",
    "GLIDER": "Glider",
    "SAV2": "Sav2",
    "SAV2_ADAPTIVE": "Sav2 Adaptive",
    "SAV2_ADAPTIVE_100": "Sav2 Adaptive 100",
    "SAV2_ADAPTIVE_5": "Mars Rover",
    "SYSADMIN_CONVEX": "Sysadmin Convex",
    "ENGAGEMENT_ADAPTIVE": "Engagement",
    "ENGAGEMENT_ADAPTIVE_5": "Engagement",
}

ROW_ORDER = {
    "Aircraft": 1,
    "Betting Game": 2,
    "Engagement": 3,
    "Mars Rover": 4,
    "Glider": 5,
    "Parallel Betting": 6,
}

ALGO_DISPLAY = {
    "imdp_full": r"$\pmb{\mathcal{P}_I}$",
    "parconvex": r"$\pmb{\mathcal{P}_{R(\mathcal{U})}}$",
    "lp_exact": r"$\pmb{\mathcal{P}_{\Lambda(\mathcal{U})}}$",
    "lp_fast": r"$\pmb{\mathcal{P}_{\Theta(\mathcal{U})}}$",
}

ALGO_ORDER = ["imdp_full", "lp_fast", "lp_exact", "parconvex"]

UDTMC_ROBUST_CANDIDATES = ["UDTMC Result Optimal Policy Robust"]
UDTMC_OPTIMISTIC_CANDIDATES = [
    "UDTMC Result Optimal Polciy Optimistic",
    "UDTMC Result Optimal Policy Optimistic",
]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Render solver result summary table (LaTeX).")
    parser.add_argument(
        "--input-root",
        type=Path,
        required=True,
        help="Root directory containing solver result folders (model/params/seed/*.csv).",
    )
    parser.add_argument(
        "--yaml-root",
        type=Path,
        default=None,
        help="Root directory for YAML metadata lookup (default: same as --input-root).",
    )
    parser.add_argument(
        "--output-root",
        type=Path,
        required=True,
        help="Directory where output files are written.",
    )
    parser.add_argument(
        "--output-file",
        type=str,
        default="benchmark_results_table_landscape_split_runtime.tex",
        help="Output .tex filename.",
    )
    parser.add_argument("--debug-list", action="store_true", help="Print CSV/YAML parsing warnings.")
    return parser.parse_args()


def pretty_benchmark_name(env_folder_name: str) -> str:
    key = re.sub(r"[^A-Z_]", "", env_folder_name.upper())
    if env_folder_name in BENCHMARK_NAME_MAP:
        return BENCHMARK_NAME_MAP[env_folder_name]
    if key in BENCHMARK_NAME_MAP:
        return BENCHMARK_NAME_MAP[key]
    for k, v in BENCHMARK_NAME_MAP.items():
        if env_folder_name.upper().startswith(k):
            return v
    return env_folder_name.replace("_", " ").title()


def algo_key_from_filename(name: str) -> str:
    u = name.upper()
    if "IMDP" in u and "FULL_TYING" in u:
        return "imdp_full"
    if "CPINTERVAL_EXACT" in u or "LPINTERVAL_EXACT" in u:
        return "lp_exact"
    if "CPINTERVAL_FAST" in u or "LPINTERVAL_FAST" in u:
        return "lp_fast"
    if "PARCONVEX" in u:
        return "parconvex"
    return ""


def fmt_num(x, sig: int = 3, sci_hi: float = 1e5, sci_lo: float = 1e-3) -> str:
    if x is None or (isinstance(x, float) and (np.isnan(x) or np.isinf(x))):
        return r"--"
    try:
        x = float(x)
    except Exception:
        return r"--"
    ax = abs(x)
    if ax == 0:
        return "0"
    if ax >= sci_hi or ax < sci_lo:
        s = f"{x:.{max(1, sig - 1)}e}"
        s = re.sub(r"e\+?0*(\d+)", r"e\1", s)
        return s
    digits = int(math.floor(math.log10(ax))) + 1
    decimals = max(0, sig - digits)
    s = f"{x:.{decimals}f}"
    if "." in s:
        s = s.rstrip("0").rstrip(".")
    return s


def fmt_time(x) -> str:
    if x is None or (isinstance(x, float) and (np.isnan(x) or np.isinf(x))):
        return r"--"
    try:
        return f"{float(x):.2f}"
    except Exception:
        return r"--"


def fmt_gap(x) -> str:
    if x is None or (isinstance(x, float) and (np.isnan(x) or np.isinf(x))):
        return r"--"
    try:
        return f"{float(x):.2f}"
    except Exception:
        return r"--"


def tex_escape(s: str) -> str:
    if s is None:
        return ""
    return (
        s.replace("\\", r"\textbackslash{}")
        .replace("&", r"\&")
        .replace("%", r"\%")
        .replace("$", r"\$")
        .replace("#", r"\#")
        .replace("_", r"\_")
        .replace("{", r"\{")
        .replace("}", r"\}")
        .replace("~", r"\textasciitilde{}")
        .replace("^", r"\textasciicircum{}")
    )


def multiline_benchmark_tex(name: str) -> str:
    mapping = {
        "Aircraft Collision": r"\shortstack[c]{Aircraft\\Collision}",
        "Betting Game": r"\shortstack[c]{Betting\\Game}",
        "Betting Game Adaptive": r"\shortstack[c]{Betting Game\\Adaptive}",
        "Betting Game Parallel": r"\shortstack[c]{Betting Game\\Parallel}",
        "Aircraft Adaptive": r"\shortstack[c]{Aircraft\\Adaptive}",
        "Drone Mixture": r"\shortstack[c]{Drone\\Mixture}",
        "Sav2 Adaptive": r"\shortstack[c]{Sav2\\Adaptive}",
        "Sav2 Adaptive 100": r"\shortstack[c]{Sav2 Adaptive\\100}",
        "Sav2 Adaptive 5": r"\shortstack[c]{Sav2 Adaptive\\5}",
        "Sysadmin Convex": r"\shortstack[c]{Sysadmin\\Convex}",
    }
    return mapping.get(name, tex_escape(name))


def sort_key_group(row: dict) -> tuple:
    base = ROW_ORDER.get(row["Benchmark"], 10_000)
    return (base, row["Benchmark"])


def _to_int(x, default: int = 10**18) -> int:
    try:
        return int(str(x).replace(",", "").strip())
    except Exception:
        return default


def safe_load_yaml_info(path: Path):
    try:
        with path.open("r", encoding="utf-8") as handle:
            data = yaml.safe_load(handle)
    except Exception:
        return None
    if not isinstance(data, dict):
        return None
    if "ExperimentInfo" in data and isinstance(data["ExperimentInfo"], dict):
        return data["ExperimentInfo"]
    return data


NEEDED_KEYS = {"NumStates", "NumTransitions", "NumParameters"}


def pick_base_info(yaml_paths: list[Path]):
    for path in yaml_paths:
        info = safe_load_yaml_info(path)
        if isinstance(info, dict) and (NEEDED_KEYS <= set(info.keys())):
            return info
    for path in yaml_paths:
        info = safe_load_yaml_info(path)
        if isinstance(info, dict):
            return info
    return None


def fetch_yaml_stats(yaml_root: Path, env_folder: str, param_folder: str):
    ydir = yaml_root / env_folder / param_folder
    if not ydir.exists():
        return None, None, None, None
    ypaths = sorted(ydir.rglob("*.yaml"))
    if not ypaths:
        return None, None, None, None
    info = pick_base_info(ypaths)
    if not isinstance(info, dict):
        return None, None, None, None
    return (
        info.get("NumStates", None),
        info.get("ParameterValues", None),
        info.get("NumParameters", None),
        info.get("TrueOptimum", None),
    )


def _fmt_param_number(val, decimals: int = 2) -> str:
    try:
        f = float(val)
    except Exception:
        return str(val)
    if abs(f - round(f)) < 1e-12:
        return str(int(round(f)))
    return f"{round(f, decimals):.{decimals}f}"


def instance_tuple_display(param_vals, fallback, limit: int = 2, decimals: int = 2) -> str:
    values = []
    if isinstance(param_vals, dict):
        values = [_fmt_param_number(v, decimals) for _, v in list(param_vals.items())[:limit]]
    elif isinstance(param_vals, (list, tuple)):
        values = [_fmt_param_number(v, decimals) for v in list(param_vals)[:limit]]
    elif isinstance(param_vals, (int, float)):
        values = [_fmt_param_number(param_vals, decimals)]
    elif isinstance(param_vals, str) and param_vals.strip():
        pieces = [p.strip() for p in param_vals.split(",") if p.strip()]
        for piece in pieces[:limit]:
            if "=" in piece:
                _, value = piece.split("=", 1)
                values.append(_fmt_param_number(value.strip(), decimals))
            else:
                values.append(_fmt_param_number(piece, decimals))

    if not values:
        pieces = [p.strip() for p in str(fallback).split(",") if p.strip()]
        for piece in pieces[:limit]:
            if "=" in piece:
                _, value = piece.split("=", 1)
                values.append(_fmt_param_number(value.strip(), decimals))
            else:
                values.append(_fmt_param_number(piece, decimals))

    return "(" + ", ".join(values) + ")"


def clamp_small(x, thresh: float = 1e-3):
    try:
        xf = float(x)
    except Exception:
        return x
    if abs(xf) < thresh:
        return 0.0
    return xf


def is_number(x) -> bool:
    try:
        return np.isfinite(float(x))
    except Exception:
        return False


def format_interval_str(lo: float, hi: float, base_sig: int = 3, bump_sig: int = 5, diff_threshold: float = 1e-6) -> str:
    s_lo = fmt_num(lo, sig=base_sig)
    s_hi = fmt_num(hi, sig=base_sig)
    if s_lo == s_hi and abs(float(lo) - float(hi)) > diff_threshold:
        s_lo = fmt_num(lo, sig=bump_sig)
        s_hi = fmt_num(hi, sig=bump_sig)
        if s_lo == s_hi:
            s_lo = f"{lo:.3f}".rstrip("0").rstrip(".")
            s_hi = f"{hi:.3f}".rstrip("0").rstrip(".")
    return rf"$[{s_lo},\,{s_hi}]$"


def pick_first_present(colnames: list[str], candidates: list[str]):
    cols = {c.strip(): c for c in colnames}
    for cand in candidates:
        if cand in cols:
            return cols[cand]
    return None


def main() -> None:
    args = parse_args()
    input_root = args.input_root.resolve()
    yaml_root = args.yaml_root.resolve() if args.yaml_root else input_root
    output_root = args.output_root.resolve()
    output_root.mkdir(parents=True, exist_ok=True)
    out_tex = output_root / args.output_file

    if not input_root.exists():
        raise FileNotFoundError(f"No such folder: {input_root}")

    rows = []
    bench_dirs = [d for d in sorted(input_root.iterdir()) if d.is_dir()]

    for env_dir in bench_dirs:
        env_name = env_dir.name
        bench_pretty = pretty_benchmark_name(env_name)

        for param_dir in sorted(env_dir.iterdir()):
            if not param_dir.is_dir():
                continue
            param_name = param_dir.name

            num_states, param_vals_yaml, _, true_optimum = fetch_yaml_stats(yaml_root, env_name, param_name)
            states_sort_key = _to_int(num_states)
            instance_display = instance_tuple_display(param_vals_yaml, param_name, limit=2, decimals=2)

            per_algo = {k: {"lo": [], "hi": [], "build": [], "solve": []} for k in ALGO_ORDER}

            for seed_dir in sorted(param_dir.iterdir()):
                if not seed_dir.is_dir():
                    continue
                for csv_path in sorted(seed_dir.glob("*.csv")):
                    algo_key = algo_key_from_filename(csv_path.name)
                    if algo_key not in per_algo:
                        continue
                    try:
                        df = pd.read_csv(csv_path)
                        df.columns = [c.strip() for c in df.columns]
                    except Exception as exc:
                        if args.debug_list:
                            print(f"WARNING: failed to read {csv_path}: {exc}")
                        continue

                    if "Episode" in df.columns:
                        df = df.sort_values("Episode")

                    col_lo_candidate = pick_first_present(df.columns.tolist(), UDTMC_ROBUST_CANDIDATES)
                    col_hi_candidate = pick_first_present(df.columns.tolist(), UDTMC_OPTIMISTIC_CANDIDATES)

                    col_solve = None
                    for name in df.columns:
                        low = name.lower()
                        if low == "model checking time robust" or all(k in low for k in ["model", "checking", "time", "robust"]):
                            col_solve = name
                            break

                    col_build = None
                    for name in df.columns:
                        low = name.lower()
                        if low == "model building time" or all(k in low for k in ["model", "building", "time"]):
                            col_build = name
                            break

                    if col_lo_candidate is None or col_hi_candidate is None:
                        if args.debug_list:
                            print(f"WARNING: missing optimal-policy columns in {csv_path.name}")
                        continue

                    last = df.iloc[-1]
                    try:
                        v1 = float(last[col_lo_candidate])
                        v2 = float(last[col_hi_candidate])
                        lo_seed = float(clamp_small(min(v1, v2), 1e-3))
                        hi_seed = float(clamp_small(max(v1, v2), 1e-3))
                        per_algo[algo_key]["lo"].append(lo_seed)
                        per_algo[algo_key]["hi"].append(hi_seed)
                    except Exception:
                        continue

                    try:
                        t_solve = float(last[col_solve]) if col_solve is not None else 0.0
                    except Exception:
                        t_solve = 0.0
                    try:
                        t_build = float(last[col_build]) if col_build is not None else 0.0
                    except Exception:
                        t_build = 0.0
                    per_algo[algo_key]["solve"].append(t_solve)
                    per_algo[algo_key]["build"].append(t_build)

            agg = {}
            for k in ALGO_ORDER:
                lo_vals = per_algo[k]["lo"]
                hi_vals = per_algo[k]["hi"]
                build_vals = per_algo[k]["build"]
                solve_vals = per_algo[k]["solve"]
                agg[k] = {
                    "lo": float(np.mean(lo_vals)) if lo_vals else None,
                    "hi": float(np.mean(hi_vals)) if hi_vals else None,
                    "build": float(np.mean(build_vals)) if build_vals else None,
                    "solve": float(np.mean(solve_vals)) if solve_vals else None,
                }

            rows.append(
                {
                    "Benchmark": bench_pretty,
                    "ParamDisplay": instance_display,
                    "StatesSortKey": states_sort_key,
                    "TrueOptimum": true_optimum,
                    "Agg": agg,
                }
            )

    rows.sort(key=sort_key_group)
    grouped = defaultdict(list)
    for row in rows:
        grouped[row["Benchmark"]].append(row)
    for _, group in grouped.items():
        group.sort(key=lambda row: row["StatesSortKey"])

    bench_spec = r">{\centering\arraybackslash}m{2.35cm}"
    param_spec = r">{\centering\arraybackslash}m{1.65cm}"
    ival_spec = r">{\centering\arraybackslash}m{2.0cm}"
    gap_spec = r">{\centering\arraybackslash}m{0.95cm}"
    time_spec = r">{\centering\arraybackslash}m{0.95cm}"

    colspec_parts = [bench_spec, param_spec]
    for idx, _ in enumerate(ALGO_ORDER):
        colspec_parts.append(ival_spec)
        colspec_parts.append(r"@{\hspace{5pt}}")
        colspec_parts.append(gap_spec)
        colspec_parts.append(r"@{\hspace{5pt}}")
        colspec_parts.append(time_spec)
        colspec_parts.append(r"@{\hspace{5pt}}")
        colspec_parts.append(time_spec)
        if idx != len(ALGO_ORDER) - 1:
            colspec_parts.append(r"@{\hspace{9pt}}")
    col_spec = " ".join(colspec_parts)

    top_headers = [r"\multirow[c]{2}{*}{\textbf{Benchmark}}", r"\multirow[c]{2}{*}{\textbf{Instance}}"]
    for key in ALGO_ORDER:
        top_headers.append(r"\multicolumn{4}{c}{" + ALGO_DISPLAY[key] + r"}")

    sub_headers = [r"", r""]
    for _ in ALGO_ORDER:
        sub_headers.append(r"$\mathbf{[\underline{V},\overline{V}]}$")
        sub_headers.append(r"\textbf{Rel. gap}")
        sub_headers.append(r"\textbf{Build [s]}")
        sub_headers.append(r"\textbf{Solve [s]}")

    cmidrules = []
    start = 3
    for _ in ALGO_ORDER:
        end = start + 3
        cmidrules.append(rf"\cmidrule(lr){{{start}-{end}}}")
        start = end + 1

    body_lines = []
    group_list = list(grouped.items())
    for gi, (bench_name, group_rows) in enumerate(group_list):
        n = len(group_rows)
        bench_tex = multiline_benchmark_tex(bench_name)
        for idx, row in enumerate(group_rows):
            first_cell = rf"\multirow[c]{{{n}}}{{*}}{{{bench_tex}}}" if idx == 0 else ""
            row_cells = [first_cell, tex_escape(row["ParamDisplay"])]

            agg = row["Agg"]
            true_opt = row.get("TrueOptimum", None)
            row_gaps = []
            if is_number(true_opt) and float(true_opt) > 0:
                for key in ALGO_ORDER:
                    lo_raw = agg[key]["lo"]
                    hi_raw = agg[key]["hi"]
                    if is_number(lo_raw) and is_number(hi_raw):
                        lo_tmp = float(lo_raw)
                        hi_tmp = float(hi_raw)
                        if lo_tmp > hi_tmp:
                            lo_tmp, hi_tmp = hi_tmp, lo_tmp
                        row_gaps.append((key, (hi_tmp - lo_tmp) / float(true_opt)))
            min_gap = min((gap for _, gap in row_gaps), default=None)

            for key in ALGO_ORDER:
                lo_raw = agg[key]["lo"]
                hi_raw = agg[key]["hi"]
                if is_number(lo_raw) and is_number(hi_raw):
                    lo = float(lo_raw)
                    hi = float(hi_raw)
                    if lo > hi:
                        lo, hi = hi, lo
                    interval_tex = format_interval_str(lo, hi)
                    if is_number(true_opt) and float(true_opt) > 0:
                        rel_gap = (hi - lo) / float(true_opt)
                        gap_tex = fmt_gap(rel_gap)
                        if min_gap is not None and abs(rel_gap - min_gap) <= 1e-12:
                            gap_tex = rf"\textbf{{{gap_tex}}}"
                    else:
                        gap_tex = r"--"
                else:
                    interval_tex = r"--"
                    gap_tex = r"--"

                build_tex = fmt_time(agg[key]["build"])
                solve_tex = fmt_time(agg[key]["solve"])
                row_cells.extend([interval_tex, gap_tex, build_tex, solve_tex])

            body_lines.append(" & ".join(row_cells) + r" \\")

        if gi != len(group_list) - 1:
            body_lines.append(r"\addlinespace[4pt]")

    latex = [
        r"\begin{landscape}",
        r"\begin{table}[t]",
        r"\centering",
        r"\caption{Final robust/optimistic guarantees, relative gaps, and separated model-building and solving times per algorithm.}",
        r"\setlength{\tabcolsep}{0.5pt}",
        r"\small",
        r"\resizebox{1.35\textwidth}{!}{%",
        rf"\begin{{tabular}}{{{col_spec}}}",
        r"\toprule",
        " & ".join(top_headers) + r" \\",
        " ".join(cmidrules),
        " & ".join(sub_headers) + r" \\",
        r"\midrule",
        *body_lines,
        r"\bottomrule",
        r"\end{tabular}",
        r"} % end of resizebox",
        r"\label{tab:results_udtmc_optpolicy_interval_relgap_split_runtime}",
        r"\end{table}",
        r"\end{landscape}",
    ]

    with out_tex.open("w", encoding="utf-8") as handle:
        handle.write("% Requires \\usepackage{booktabs,multirow,array,pdflscape}\n")
        handle.write("\n".join(latex))

    print(f"Wrote LaTeX table: {out_tex}")
    print(f"Benchmarks: {len(grouped)} | Rows total: {sum(len(v) for v in grouped.values())}")


if __name__ == "__main__":
    main()
