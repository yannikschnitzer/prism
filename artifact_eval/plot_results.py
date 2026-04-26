#!/usr/bin/env python3
"""
Generate a LaTeX benchmark-statistics table from solver YAML outputs.

This script currently writes a table (not plots) and is wired to:
  run-ae postprocess stats
and alias:
  run-ae postprocess plots
for backward compatibility.
"""

from __future__ import annotations

import argparse
import re
from pathlib import Path

import yaml


BENCHMARK_NAME_MAP = {
    "UAV": "UAV",
    "UAV_CONVEX": "UAV",
    "AIRCRAFT_COLLISION": "Aircraft Collision",
    "AIRCRAFT": "Aircraft Collision",
    "AIRCRAFT_MIXTURE_POSITION": "Aircraft Collision",
    "FIREWIRE": "Firewire",
    "SEMI_AUTONOMOUS_VEHICLE": "Semi-Auton. Vehicle",
    "SEMIAUTONOMOUS_VEHICLE": "Semi-Auton. Vehicle",
    "BETTING_GAME": "Betting Game",
    "BETTING_GAME_CONVEX": "Betting Game",
    "BETTING_GAME_CONVEX_ADAPTIVE": "Betting Game Adaptive",
    "BETTING_GAME_PARALLEL": "Betting Game Parallel",
    "CHAIN": "Chain",
    "ENGAGEMENT_ADAPTIVE_5": "Engagement",
    "GLIDER": "Glider",
    "SAV2_ADAPTIVE_5": "Mars Rover",
}

ROW_ORDER = {
    "UAV": 0,
    "Aircraft Collision": 1,
    "Firewire": 2,
    "Semi-Auton. Vehicle": 3,
    "Betting Game": 4,
    "Betting Game Adaptive": 5,
    "Betting Game Parallel": 6,
    "Engagement": 7,
    "Mars Rover": 8,
    "Glider": 9,
    "Chain": 10,
}

NEEDED_KEYS = {"NumStates", "NumTransitions", "NumParameters"}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Render benchmark statistics table (LaTeX).")
    parser.add_argument(
        "--input-root",
        type=Path,
        required=True,
        help="Root directory containing benchmark results (model/params/seed/*.yaml).",
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
        default="benchmark_stats_table.tex",
        help="Output .tex filename.",
    )
    parser.add_argument(
        "--include-property",
        action="store_true",
        default=True,
        help="Include the Property column (default: enabled).",
    )
    parser.add_argument(
        "--no-include-property",
        dest="include_property",
        action="store_false",
        help="Disable the Property column.",
    )
    parser.add_argument("--debug-list", action="store_true", help="Print discovered YAML diagnostics.")
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


def safe_load_yaml_info(path: Path):
    try:
        with path.open("r", encoding="utf-8") as handle:
            data = yaml.safe_load(handle)
    except Exception as exc:
        print(f"WARNING: failed to load YAML: {path} ({exc})")
        return None
    if not isinstance(data, dict):
        return None
    if "ExperimentInfo" in data and isinstance(data["ExperimentInfo"], dict):
        return data["ExperimentInfo"]
    return data


def pick_base_info(yaml_paths: list[Path]):
    for ypath in yaml_paths:
        info = safe_load_yaml_info(ypath)
        if isinstance(info, dict) and (NEEDED_KEYS <= set(info.keys())):
            return info, ypath
    for ypath in yaml_paths:
        info = safe_load_yaml_info(ypath)
        if isinstance(info, dict):
            return info, ypath
    return None, None


def find_parconvex_yaml(param_dir: Path):
    exact = sorted(param_dir.rglob("*PARCONVEX_FULL_TYING_NOBISIM.yaml"))
    if exact:
        return exact[0]
    any_par = sorted([path for path in param_dir.rglob("*.yaml") if "PARCONVEX" in path.name.upper()])
    if any_par:
        return any_par[0]
    csv_match = sorted(param_dir.rglob("*PARCONVEX_FULL_TYING_NOBISIM.csv"))
    for csv_path in csv_match:
        yaml_path = csv_path.with_suffix(".yaml")
        if yaml_path.exists():
            return yaml_path
    return None


def detect_property(info: dict) -> str:
    t = str(info.get("Type", "")).upper()
    spec = str(info.get("Specification", "")).upper()
    if t.startswith("REW"):
        return r"$\mathbb{E}$"
    if re.search(r"\bR(?:MAX|MIN|MAXMIN|MAXMAX)?\s*=\s*\?", spec):
        return r"$\mathbb{E}$"
    return r"$\mathbb{P}$"


def tex_escape(s: str) -> str:
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


def sort_key(row: dict) -> tuple:
    base = ROW_ORDER.get(row["Benchmark"], 10_000)
    return (base, row["Benchmark"], row["Param"])


def _to_int(x, default: int = 10**18) -> int:
    try:
        return int(str(x).replace(",", "").strip())
    except Exception:
        return default


def _fmt_param_number(val, decimals: int = 2) -> str:
    try:
        f = float(val)
    except Exception:
        return str(val)
    if abs(f - round(f)) < 1e-12:
        return str(int(round(f)))
    return f"{round(f, decimals):.{decimals}f}"


def round_param_values(param_vals, decimals: int = 2) -> str:
    if param_vals is None:
        return ""
    if isinstance(param_vals, dict):
        parts = [f"{k}={_fmt_param_number(v, decimals)}" for k, v in param_vals.items()]
        return ",".join(parts)
    if isinstance(param_vals, (list, tuple)):
        return ",".join(_fmt_param_number(v, decimals) for v in param_vals)
    if isinstance(param_vals, (int, float)):
        return _fmt_param_number(param_vals, decimals)

    s = str(param_vals).strip()
    pieces = [p.strip() for p in s.split(",") if p.strip()]
    out = []
    for piece in pieces:
        if "=" in piece:
            key, value = piece.split("=", 1)
            out.append(f"{key.strip()}={_fmt_param_number(value.strip(), decimals)}")
        else:
            out.append(_fmt_param_number(piece, decimals))
    return ",".join(out)


def stringify_params_limited(param_vals, limit: int = 2, decimals: int = 2) -> str:
    if param_vals is None:
        return ""
    if isinstance(param_vals, dict):
        items = list(param_vals.items())[:limit]
        return ",".join(f"{k}={_fmt_param_number(v, decimals)}" for k, v in items)
    if isinstance(param_vals, (list, tuple)):
        return ",".join(_fmt_param_number(v, decimals) for v in list(param_vals)[:limit])
    if isinstance(param_vals, (int, float)):
        return _fmt_param_number(param_vals, decimals)

    s = str(param_vals).strip()
    pieces = [p.strip() for p in s.split(",") if p.strip()][:limit]
    out = []
    for piece in pieces:
        if "=" in piece:
            key, value = piece.split("=", 1)
            out.append(f"{key.strip()}={_fmt_param_number(value.strip(), decimals)}")
        else:
            out.append(_fmt_param_number(piece, decimals))
    return ",".join(out)


def main() -> None:
    args = parse_args()
    input_root = args.input_root.resolve()
    output_root = args.output_root.resolve()
    output_root.mkdir(parents=True, exist_ok=True)
    out_tex = output_root / args.output_file

    if not input_root.exists():
        raise FileNotFoundError(f"No such folder: {input_root}")

    flat_rows = []
    seen = set()

    benchmarks = [d for d in sorted(input_root.iterdir()) if d.is_dir()]
    if args.debug_list:
        print(f"Found {len(benchmarks)} benchmarks under {input_root}")

    for env_dir in benchmarks:
        env_name = env_dir.name
        bench_pretty = pretty_benchmark_name(env_name)

        params = [p for p in sorted(env_dir.iterdir()) if p.is_dir()]
        if args.debug_list:
            print(f"- {env_name}: {len(params)} params")

        for param_dir in params:
            param_name = param_dir.name
            key = (env_name, param_name)
            if key in seen:
                continue

            yaml_paths = sorted(param_dir.rglob("*.yaml"))
            if args.debug_list:
                print(f"  · {param_name}: {len(yaml_paths)} yaml files")

            if not yaml_paths:
                print(f"WARNING: no YAML files under {param_dir}, skipping.")
                continue

            base_info, _ = pick_base_info(yaml_paths)
            if not isinstance(base_info, dict):
                print(f"WARNING: no usable YAML with required keys under {param_dir}, skipping.")
                continue

            par_yaml = find_parconvex_yaml(param_dir)
            par_info = safe_load_yaml_info(par_yaml) if par_yaml else None

            num_states = base_info.get("NumStates", "")
            num_trans = base_info.get("NumTransitions", "")
            num_params = base_info.get("NumParameters", "")
            if isinstance(par_info, dict) and "NumLearnableComponents" in par_info:
                num_expr = par_info["NumLearnableComponents"]
            else:
                num_expr = base_info.get("NumLearnableComponents", "")

            prop = detect_property(base_info) if args.include_property else ""
            raw_param_vals = base_info.get("ParameterValues", param_name)

            nparams_int = _to_int(num_params, default=0)
            if nparams_int > 4:
                param_vals = stringify_params_limited(raw_param_vals, limit=2, decimals=2)
            else:
                param_vals = round_param_values(raw_param_vals, decimals=2)

            flat_rows.append(
                {
                    "Benchmark": bench_pretty,
                    "Param": param_name,
                    "ParamVals": param_vals,
                    "States": num_states,
                    "Transitions": num_trans,
                    "Parameters": num_params,
                    "Expressions": num_expr,
                    "Property": prop,
                }
            )
            seen.add(key)

    flat_rows.sort(key=sort_key)
    grouped = {}
    for row in flat_rows:
        grouped.setdefault(row["Benchmark"], []).append(row)
    for _, rows in grouped.items():
        rows.sort(key=lambda row: _to_int(row["States"]))

    if args.include_property:
        header_cols = [
            (r"\textbf{Benchmark}", r">{\centering\arraybackslash}m{3.8cm}"),
            (r"\textbf{Param.\ Values}", r">{\centering\arraybackslash}m{3.0cm}"),
            (r"\textbf{$|\Theta|$}", r">{\centering\arraybackslash}m{1cm}"),
            (r"\textbf{$|S|$}", r">{\centering\arraybackslash}m{1.5cm}"),
            (r"\textbf{$|T|$}", r">{\centering\arraybackslash}m{1.5cm}"),
            (r"\textbf{$|\mathcal{E}|$}", r">{\centering\arraybackslash}m{1.5cm}"),
            (r"\textbf{Property}", r">{\centering\arraybackslash}m{1.5cm}"),
        ]
        col_keys = ["ParamVals", "Parameters", "States", "Transitions", "Expressions", "Property"]
    else:
        header_cols = [
            (r"\textbf{Benchmark}", r">{\centering\arraybackslash}m{4.0cm}"),
            (r"\textbf{Param.\ Values}", r">{\centering\arraybackslash}m{3.0cm}"),
            (r"\textbf{\#Parameters}", r">{\centering\arraybackslash}m{1.5cm}"),
            (r"\textbf{\#States}", r">{\centering\arraybackslash}m{1.5cm}"),
            (r"\textbf{\#Transitions}", r">{\centering\arraybackslash}m{1.5cm}"),
            (r"\textbf{\#Expressions}", r">{\centering\arraybackslash}m{1.5cm}"),
        ]
        col_keys = ["ParamVals", "Parameters", "States", "Transitions", "Expressions"]

    col_spec = " ".join(spec for _, spec in header_cols)
    header_line = " & ".join(h for h, _ in header_cols) + r" \\"

    body_lines = []
    group_list = list(grouped.items())
    for gi, (bench_name, rows) in enumerate(group_list):
        n = len(rows)
        bench_tex = tex_escape(bench_name)
        for idx, row in enumerate(rows):
            vals = []
            for key in col_keys:
                value = "" if row[key] is None else str(row[key])
                vals.append(value if key == "Property" else tex_escape(value))
            first_cell = rf"\multirow[c]{{{n}}}{{*}}{{{bench_tex}}}" if idx == 0 else ""
            body_lines.append(" & ".join([first_cell] + vals) + r" \\")
        if gi != len(group_list) - 1:
            body_lines.append(r"\addlinespace[4pt]")

    latex = [
        r"\begin{table}[t]",
        r"\centering",
        r"\caption{Salient characteristics of the evaluated benchmarks.}",
        r"\resizebox{0.93\textwidth}{!}{%",
        rf"\begin{{tabular}}{{{col_spec}}}",
        r"\toprule",
        header_line,
        r"\midrule",
        *body_lines,
        r"\bottomrule",
        r"\end{tabular}",
        r"} % end of resizebox",
        r"\label{tab:stats}",
        r"\end{table}",
    ]

    with out_tex.open("w", encoding="utf-8") as handle:
        handle.write("% Requires \\usepackage{booktabs,multirow,array}\n")
        handle.write("\n".join(latex))

    print(f"Wrote LaTeX table: {out_tex}")
    print(f"Benchmarks: {len(grouped)} | Rows total: {sum(len(v) for v in grouped.values())}")


if __name__ == "__main__":
    main()
