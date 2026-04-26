#!/usr/bin/env python3
"""
Generate learning benchmark plots from learner CSV outputs.

Default expected input layout:
  <input-root>/<MODEL>/<PARAM_DIR>/<SEED>/*.csv

Outputs:
  <output-root>/<MODEL>/<PARAM_DIR>/<MODEL>_<PARAM>.pdf
  <output-root>/<MODEL>/<PARAM_DIR>/<MODEL>_<PARAM>_runtime_processed.pdf
  <output-root>/<MODEL>/<PARAM_DIR>/<MODEL>_<PARAM>_legend.pdf
"""

from __future__ import annotations

import argparse
import shutil
from pathlib import Path

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from matplotlib.ticker import AutoMinorLocator, LogLocator, MaxNLocator


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Render learning benchmark plots from learner CSV outputs.")
    parser.add_argument("--input-root", type=Path, required=True, help="Root directory with learning CSV outputs.")
    parser.add_argument("--output-root", type=Path, required=True, help="Output directory for generated plots.")
    parser.add_argument("--dpi", type=int, default=300, help="Figure DPI.")
    parser.add_argument("--show-inline", type=int, default=0, help="Show first N plots interactively.")
    parser.add_argument(
        "--usetex",
        choices=["auto", "on", "off"],
        default="auto",
        help="LaTeX rendering mode for text: auto (default), on, off.",
    )
    return parser.parse_args()


def configure_matplotlib(usetex_mode: str) -> bool:
    latex_available = shutil.which("latex") is not None
    if usetex_mode == "on":
        use_tex = True
    elif usetex_mode == "off":
        use_tex = False
    else:
        use_tex = latex_available

    plt.rcParams.update(
        {
            "text.usetex": use_tex,
            "text.latex.preamble": r"\usepackage{amsmath,amssymb}",
            "font.family": "serif",
            "font.serif": ["Times New Roman", "Times", "DejaVu Serif", "Serif"],
            "font.size": 12,
            "axes.titlesize": 16,
            "axes.labelsize": 16,
            "xtick.direction": "in",
            "ytick.direction": "in",
            "xtick.major.size": 8,
            "ytick.major.size": 8,
            "xtick.minor.size": 4,
            "ytick.minor.size": 4,
            "xtick.major.width": 1.2,
            "ytick.major.width": 1.2,
            "xtick.minor.width": 1.0,
            "ytick.minor.width": 1.0,
            "axes.linewidth": 5,
            "legend.fontsize": 14,
            "legend.frameon": True,
            "legend.framealpha": 0.8,
            "legend.fancybox": True,
            "axes.grid": True,
            "grid.color": "#bbbbbb",
            "grid.linestyle": "--",
            "grid.linewidth": 0.8,
            "savefig.format": "pdf",
            "savefig.bbox": "standard",
            "mathtext.fontset": "stix",
            "mathtext.default": "regular",
        }
    )
    return use_tex


FIGSIZE = (7.4, 5.2)
LIGHTRED = "#F1E1DE"
OFFWHITE = "#FBF4EC"
ANTIQUEWHITE = "#FAEBD7"
BRICKRED = "#A63A2B"
COLOR1 = "#D58E63"
ALIZARIN = "#D96B4D"
PIGMENT = "#7C4A21"
FALLBACK_COLORS = ["#7A6A58", "#6E7F80", "#8C6F5E", "#6D6488"]
ALGO_STYLES = {
    "imdp_full": {"label": r"$\mathcal{P}_I$", "color": COLOR1, "fill": "#F4DDD1"},
    "lpinterval_exact": {"label": r"$\mathcal{P}_{\Lambda(\mathcal{U})}$", "color": BRICKRED, "fill": LIGHTRED},
    "lpinterval_fast": {"label": r"$\mathcal{P}_{\Theta(\mathcal{U})}$", "color": ALIZARIN, "fill": "#F2D6CD"},
    "parconvex_full": {"label": r"$\mathcal{P}_{R(\mathcal{U})}$", "color": PIGMENT, "fill": "#F1DDB8"},
    "imdp_none": {"label": "IMDP", "color": "#7A6A58", "fill": ANTIQUEWHITE},
}
LINESTYLES = {"performance": "-", "guarantee": (0, (5, 2.2))}


def canonical_algo_key(algo_name: str) -> str:
    u = algo_name.upper()
    if "IMDP" in u and "FULL_TYING" in u and "NO_TYING" not in u:
        return "imdp_full"
    if "IMDP" in u and "NO_TYING" in u:
        return "imdp_none"
    if "CPINTERVAL_EXACT" in u or "LPINTERVAL_EXACT" in u:
        return "lpinterval_exact"
    if "CPINTERVAL_FAST" in u or "LPINTERVAL_FAST" in u:
        return "lpinterval_fast"
    if "PARCONVEX" in u and "FULL_TYING" in u:
        return "parconvex_full"
    return algo_name.lower()


def pretty_algo_label(algo_name: str) -> str:
    key = canonical_algo_key(algo_name)
    if key in ALGO_STYLES:
        return ALGO_STYLES[key]["label"]
    return algo_name.replace("_", " ")


def style_for_algo(algo_name: str, fallback_idx: int = 0) -> dict:
    key = canonical_algo_key(algo_name)
    if key in ALGO_STYLES:
        return ALGO_STYLES[key]
    return {
        "label": pretty_algo_label(algo_name),
        "color": FALLBACK_COLORS[fallback_idx % len(FALLBACK_COLORS)],
        "fill": OFFWHITE,
    }


def algo_sort_key(algo_name: str) -> tuple:
    order = {
        "imdp_full": 0,
        "lpinterval_exact": 1,
        "lpinterval_fast": 2,
        "parconvex_full": 3,
        "imdp_none": 4,
    }
    key = canonical_algo_key(algo_name)
    return (order.get(key, 100), pretty_algo_label(algo_name))


def value_ylabel(env_name: str) -> str:
    if env_name.startswith("AIRCRAFT") or env_name.startswith("SAV2_ADAPTIVE"):
        return r"$\mathbb{P}(\neg\,\mathrm{crash}\,\mathsf{U}\,\mathrm{goal})$"
    if env_name in {"BETTING_GAME_PARALLEL", "BETTING_GAME_CONVEX_ADAPTIVE"}:
        return r"$\mathbb{E}(\mathrm{F}^{=T}\,\mathrm{money})$"
    if env_name == "GLIDER":
        return r"$\mathbb{E}(F\ goal)$"
    return "Value"


def find_col(columns, *keywords):
    for col in columns:
        low = col.lower()
        if all(kw.lower() in low for kw in keywords):
            return col
    return None


def apply_infinity_caps(
    ax,
    y_min: float,
    y_max: float,
    has_pos_inf: bool,
    has_neg_inf: bool,
    inverted: bool = False,
    log_scale: bool = False,
    finite_pad: float = 1.03,
    cap_pad: float = 1.08,
):
    if log_scale:
        y_min = max(y_min, np.nextafter(0, 1))
        low_lim = y_min / finite_pad
        high_lim = y_max * finite_pad

        if has_pos_inf:
            next_dec = 10.0 ** np.ceil(np.log10(y_max))
            high_lim = min(y_max * cap_pad, next_dec / 1.001)
        if has_neg_inf:
            prev_dec = 10.0 ** np.floor(np.log10(y_min))
            low_lim = max(y_min / cap_pad, prev_dec * 1.001)
    else:
        span = max(y_max - y_min, 1.0)
        low_lim = y_min - (span * (finite_pad - 1.0))
        high_lim = y_max + (span * (finite_pad - 1.0))
        if has_pos_inf:
            high_lim = y_max + (span * (cap_pad - 1.0))
        if has_neg_inf:
            low_lim = y_min - (span * (cap_pad - 1.0))

    if not inverted:
        ax.set_ylim(low_lim, high_lim)
    else:
        ax.set_ylim(high_lim, low_lim)

    ticks = [t for t in ax.get_yticks() if (min(low_lim, high_lim) < t < max(low_lim, high_lim))]

    def _fmt(t):
        if log_scale:
            k = np.log10(float(t))
            if abs(k - round(k)) < 1e-9:
                return rf"$10^{int(round(k))}$"
            return f"{float(t):g}"
        return f"{t:g}"

    labels = [_fmt(t) for t in ticks]
    if has_neg_inf:
        ticks = [low_lim] + ticks
        labels = ["−∞"] + labels
    if has_pos_inf:
        ticks = ticks + [high_lim]
        labels = labels + ["∞"]

    ax.set_yticks(ticks)
    ax.set_yticklabels(labels)


def generate_learning_plots(input_root: Path, output_root: Path, dpi: int, show_inline: int, use_tex: bool) -> int:
    if not input_root.exists():
        raise FileNotFoundError(f"No such folder: {input_root}")
    output_root.mkdir(parents=True, exist_ok=True)

    legend_rcparams = {
        "text.usetex": use_tex,
        "text.latex.preamble": r"\usepackage{amsmath,amssymb}",
        "font.family": "serif",
        "font.serif": ["Computer Modern Roman", "CMU Serif", "Latin Modern Roman", "DejaVu Serif"],
    }

    plot_count = 0

    for env_dir in sorted(input_root.iterdir()):
        if not env_dir.is_dir():
            continue
        env_name = env_dir.name

        for param_dir in sorted(env_dir.iterdir()):
            if not param_dir.is_dir():
                continue
            param_name = param_dir.name

            seed_dirs = [d for d in sorted(param_dir.iterdir()) if d.is_dir()]
            if not seed_dirs:
                continue

            combos = {}
            for seed_dir in seed_dirs:
                for csv_path in seed_dir.glob(f"{env_name}_*.csv"):
                    stem = csv_path.stem
                    prefix = env_name + "_"
                    algo_name = stem[len(prefix) :] if stem.startswith(prefix) else stem
                    combos.setdefault(algo_name, []).append(csv_path)

            if not combos:
                continue

            fig, ax = plt.subplots(figsize=FIGSIZE, dpi=dpi)
            first_algo = True
            is_minimization = False
            finite_vals_all = []
            series = []

            for i, algo_name in enumerate(sorted(combos.keys(), key=algo_sort_key)):
                csv_list = sorted(combos[algo_name])
                if not csv_list:
                    continue

                raw_dfs = []
                perf_col = None
                guar_col = None

                for csv_path in csv_list:
                    df = pd.read_csv(csv_path).rename(columns=str.strip)
                    if "Episode" not in df.columns:
                        raise KeyError(f"'Episode' column not found in {csv_path}")

                    df = df.replace(
                        ["Infinity", "Inf", "inf", "+Inf", "-Inf", "+Infinity", "-Infinity"],
                        [np.inf, np.inf, np.inf, np.inf, -np.inf, np.inf, -np.inf],
                    )
                    df = df.sort_values("Episode").set_index("Episode")

                    if perf_col is None and guar_col is None:
                        perf_col = find_col(df.columns, "performance")
                        guar_col = find_col(df.columns, "guarantee")
                        if perf_col is None or guar_col is None:
                            print(
                                f"WARNING: skipping '{algo_name}' under {env_name}/{param_name}: "
                                f"missing performance/guarantee columns in {csv_path.name}"
                            )
                            perf_col = None
                            guar_col = None

                    if perf_col is None or guar_col is None:
                        raw_dfs = []
                        break

                    cols_to_keep = [perf_col, guar_col]
                    if "Total Runtime" in df.columns:
                        cols_to_keep.append("Total Runtime")
                    raw_dfs.append(df[cols_to_keep])

                if not raw_dfs:
                    continue

                all_eps = np.sort(np.unique(np.concatenate([df.index.values for df in raw_dfs])))
                num_eps = len(all_eps)
                num_seeds = len(raw_dfs)

                perf_mat = np.full((num_eps, num_seeds), np.nan, dtype=float)
                guar_mat = np.full((num_eps, num_seeds), np.nan, dtype=float)
                runtime_mat = np.full((num_eps, num_seeds), np.nan, dtype=float)

                perf_pos_inf_any = np.zeros(num_eps, dtype=bool)
                perf_neg_inf_any = np.zeros(num_eps, dtype=bool)
                guar_pos_inf_any = np.zeros(num_eps, dtype=bool)
                guar_neg_inf_any = np.zeros(num_eps, dtype=bool)

                for col_idx, df_seed in enumerate(raw_dfs):
                    seed_x = df_seed.index.values.astype(float)
                    seed_perf = df_seed[perf_col].to_numpy(dtype=float)
                    seed_guar = df_seed[guar_col].to_numpy(dtype=float)

                    if np.any(~np.isfinite(seed_perf)):
                        perf_pos_inf_any |= np.isin(all_eps, seed_x[np.isposinf(seed_perf)])
                        perf_neg_inf_any |= np.isin(all_eps, seed_x[np.isneginf(seed_perf)])
                    if np.any(~np.isfinite(seed_guar)):
                        guar_pos_inf_any |= np.isin(all_eps, seed_x[np.isposinf(seed_guar)])
                        guar_neg_inf_any |= np.isin(all_eps, seed_x[np.isneginf(seed_guar)])

                    finite_p = np.isfinite(seed_perf)
                    if finite_p.sum() >= 2:
                        perf_mat[:, col_idx] = np.interp(all_eps, seed_x[finite_p], seed_perf[finite_p], left=np.nan, right=np.nan)

                    finite_g = np.isfinite(seed_guar)
                    if finite_g.sum() >= 2:
                        guar_mat[:, col_idx] = np.interp(all_eps, seed_x[finite_g], seed_guar[finite_g], left=np.nan, right=np.nan)

                    if "Total Runtime" in df_seed.columns:
                        seed_runtime = df_seed["Total Runtime"].to_numpy(dtype=float)
                        finite_rt = np.isfinite(seed_runtime)
                        if finite_rt.sum() >= 2:
                            runtime_mat[:, col_idx] = np.interp(all_eps, seed_x[finite_rt], seed_runtime[finite_rt], left=np.nan, right=np.nan)

                perf_mean = np.nanmean(perf_mat, axis=1)
                perf_std = np.nanstd(perf_mat, axis=1) * 0.5
                guar_mean = np.nanmean(guar_mat, axis=1)
                guar_std = np.nanstd(guar_mat, axis=1)

                has_runtime = np.isfinite(runtime_mat).any()
                if has_runtime:
                    runtime_mean = np.nanmean(runtime_mat, axis=1)
                    runtime_std = np.nanstd(runtime_mat, axis=1) * 0.8
                else:
                    runtime_mean = np.full(num_eps, np.nan, dtype=float)
                    runtime_std = np.full(num_eps, np.nan, dtype=float)

                if np.isfinite(perf_mean).any():
                    finite_vals_all.append(perf_mean[np.isfinite(perf_mean)])
                if np.isfinite(guar_mean).any():
                    finite_vals_all.append(guar_mean[np.isfinite(guar_mean)])

                if first_algo:
                    idx = np.where(np.isfinite(perf_mean))[0]
                    if idx.size >= 2 and perf_mean[idx[-1]] < perf_mean[idx[0]]:
                        is_minimization = True
                    first_algo = False

                style = style_for_algo(algo_name, i)
                series.append(
                    dict(
                        color=style["color"],
                        fill_color=style["fill"],
                        x=all_eps,
                        perf_mean=perf_mean,
                        perf_std=perf_std,
                        guar_mean=guar_mean,
                        guar_std=guar_std,
                        p_pos=perf_pos_inf_any,
                        p_neg=perf_neg_inf_any,
                        g_pos=guar_pos_inf_any,
                        g_neg=guar_neg_inf_any,
                        runtime_mean=runtime_mean,
                        runtime_std=runtime_std,
                        has_runtime=has_runtime,
                        legend_label=style["label"],
                    )
                )

            if not series:
                plt.close(fig)
                continue

            use_log_y = env_name in {"ENGAGEMENT_ADAPTIVE_5", "GLIDER"}
            invert_y = is_minimization or env_name == "GLIDER"
            ax.set_xscale("log")
            if env_name == "ENGAGEMENT_ADAPTIVE_5":
                ax.set_xlim(1e0, 1e5)
            elif env_name in {"BETTING_GAME_PARALLEL", "AIRCRAFT_MIXTURE_POSITION"}:
                ax.set_xlim(1e1, 1e5)

            if use_log_y:
                ax.set_yscale("log")

            ax.xaxis.set_major_locator(LogLocator(base=10, subs=(1.0,), numticks=100))
            ax.xaxis.set_minor_locator(LogLocator(base=10, subs=np.arange(2, 10) * 0.1, numticks=100))
            if use_log_y:
                ax.yaxis.set_major_locator(LogLocator(base=10, subs=(1.0,), numticks=100))
                ax.yaxis.set_minor_locator(LogLocator(base=10, subs=np.arange(2, 10) * 0.1, numticks=100))
            else:
                ax.yaxis.set_major_locator(MaxNLocator(nbins=6))
                ax.yaxis.set_minor_locator(AutoMinorLocator(2))
            if invert_y:
                ax.invert_yaxis()

            has_pos_inf = any(s["p_pos"].any() or s["g_pos"].any() for s in series)
            has_neg_inf = any(s["p_neg"].any() or s["g_neg"].any() for s in series)

            if finite_vals_all:
                finite_vals_all = np.concatenate(finite_vals_all)
                finite_vals_all = finite_vals_all[np.isfinite(finite_vals_all)]
                if use_log_y:
                    finite_vals_all = finite_vals_all[finite_vals_all > 0]
                if finite_vals_all.size > 0:
                    y_min = float(np.nanmin(finite_vals_all))
                    y_max = float(np.nanmax(finite_vals_all))
                    apply_infinity_caps(
                        ax,
                        y_min,
                        y_max,
                        has_pos_inf=has_pos_inf,
                        has_neg_inf=has_neg_inf,
                        inverted=invert_y,
                        log_scale=use_log_y,
                        finite_pad=1.03,
                        cap_pad=1.08,
                    )

            if env_name == "ENGAGEMENT_ADAPTIVE_5":
                ax.set_ylim(1e4, 3e1)

            ymin_cur, ymax_cur = ax.get_ylim()
            if not invert_y:
                y_bottom_marker = ymin_cur
                y_top_marker = ymax_cur
            else:
                y_top_marker = ymin_cur
                y_bottom_marker = ymax_cur

            for s in series:
                color = s["color"]
                fill_color = s["fill_color"]
                x = s["x"]
                perf_y = s["perf_mean"].copy()
                guar_y = s["guar_mean"].copy()

                if has_pos_inf:
                    perf_y[s["p_pos"]] = y_top_marker
                    guar_y[s["g_pos"]] = y_top_marker
                if has_neg_inf:
                    perf_y[s["p_neg"]] = y_bottom_marker
                    guar_y[s["g_neg"]] = y_bottom_marker

                ax.plot(x, perf_y, color=color, linestyle=LINESTYLES["performance"], linewidth=2.2, label="_nolegend_")
                ax.plot(x, guar_y, color=color, linestyle=LINESTYLES["guarantee"], linewidth=2.2, label="_nolegend_")

                mp = np.isfinite(s["perf_mean"]) & np.isfinite(s["perf_std"])
                if np.any(mp):
                    perf_lower = (s["perf_mean"] - s["perf_std"])[mp]
                    perf_upper = (s["perf_mean"] + s["perf_std"])[mp]
                    perf_x = x[mp]
                    if use_log_y:
                        perf_band_mask = perf_upper > 0
                        perf_x = perf_x[perf_band_mask]
                        perf_lower = np.clip(perf_lower[perf_band_mask], a_min=np.finfo(float).tiny, a_max=None)
                        perf_upper = np.clip(perf_upper[perf_band_mask], a_min=np.finfo(float).tiny, a_max=None)
                    ax.fill_between(perf_x, perf_lower, perf_upper, color=fill_color, alpha=0.22)

                mg = np.isfinite(s["guar_mean"]) & np.isfinite(s["guar_std"])
                if np.any(mg):
                    guar_lower = (s["guar_mean"] - s["guar_std"])[mg]
                    guar_upper = (s["guar_mean"] + s["guar_std"])[mg]
                    guar_x = x[mg]
                    if use_log_y:
                        guar_band_mask = guar_upper > 0
                        guar_x = guar_x[guar_band_mask]
                        guar_lower = np.clip(guar_lower[guar_band_mask], a_min=np.finfo(float).tiny, a_max=None)
                        guar_upper = np.clip(guar_upper[guar_band_mask], a_min=np.finfo(float).tiny, a_max=None)
                    ax.fill_between(guar_x, guar_lower, guar_upper, color=fill_color, alpha=0.18)

            for spine in ax.spines.values():
                spine.set_visible(True)
                spine.set_color("black")
                spine.set_linewidth(2)

            ax.grid(which="major", linestyle="-", linewidth=0.7, alpha=0.6)
            ax.grid(which="minor", linestyle=":", linewidth=0.5, alpha=0.6)
            ax.tick_params(axis="y", which="both", labelsize=21, pad=6)
            ax.tick_params(axis="x", which="both", labelsize=23, pad=9)
            ax.set_xlabel("Episode", fontsize=28, labelpad=10)
            ylabel = r"$\mathbb{E}(F\ purchase)$" if env_name == "ENGAGEMENT_ADAPTIVE_5" else value_ylabel(env_name)
            ax.set_ylabel(ylabel, fontsize=28, labelpad=10)
            fig.subplots_adjust(left=0.20, right=0.96, bottom=0.20, top=0.97)

            save_dir = output_root / env_name / param_name
            save_dir.mkdir(parents=True, exist_ok=True)
            file_base = f"{env_name}_{param_name}"[:250]

            out_pdf = save_dir / f"{file_base}.pdf"
            fig.savefig(out_pdf, dpi=dpi)
            print(f"Saved plot: {out_pdf}")

            runtime_series = [s for s in series if s["has_runtime"]]
            if runtime_series:
                fig_rt, ax_rt = plt.subplots(figsize=FIGSIZE, dpi=dpi)
                for s in runtime_series:
                    runtime_y = s["runtime_mean"]
                    runtime_std = s["runtime_std"]
                    eps_x = s["x"]
                    line_mask = np.isfinite(runtime_y) & (runtime_y > 0) & np.isfinite(eps_x) & (eps_x > 0)
                    if not np.any(line_mask):
                        continue
                    ax_rt.plot(eps_x[line_mask], runtime_y[line_mask], color=s["color"], linewidth=2.2)

                    band_mask = line_mask & np.isfinite(runtime_std)
                    if np.any(band_mask):
                        lower = np.clip((runtime_y - runtime_std)[band_mask], a_min=np.finfo(float).tiny, a_max=None)
                        upper = np.clip((runtime_y + runtime_std)[band_mask], a_min=np.finfo(float).tiny, a_max=None)
                        ax_rt.fill_between(eps_x[band_mask], lower, upper, color=s["fill_color"], alpha=0.22)

                ax_rt.set_xscale("log")
                ax_rt.set_yscale("log")
                if env_name in {"BETTING_GAME_PARALLEL", "AIRCRAFT_MIXTURE_POSITION"}:
                    ax_rt.set_xlim(1e1, 1e5)
                ax_rt.xaxis.set_major_locator(LogLocator(base=10, subs=(1.0,), numticks=100))
                ax_rt.xaxis.set_minor_locator(LogLocator(base=10, subs=np.arange(2, 10) * 0.1, numticks=100))
                ax_rt.yaxis.set_major_locator(LogLocator(base=10, subs=(1.0,), numticks=100))
                ax_rt.yaxis.set_minor_locator(LogLocator(base=10, subs=np.arange(2, 10) * 0.1, numticks=100))

                for spine in ax_rt.spines.values():
                    spine.set_visible(True)
                    spine.set_color("black")
                    spine.set_linewidth(2)

                ax_rt.grid(which="major", linestyle="-", linewidth=0.7, alpha=0.6)
                ax_rt.grid(which="minor", linestyle=":", linewidth=0.5, alpha=0.6)
                ax_rt.tick_params(axis="y", which="both", labelsize=21, pad=6)
                ax_rt.tick_params(axis="x", which="both", labelsize=23, pad=9)
                ax_rt.set_xlabel("Episode", fontsize=28, labelpad=10)
                ax_rt.set_ylabel("Total Runtime (s)", fontsize=28, labelpad=10)
                fig_rt.subplots_adjust(left=0.20, right=0.96, bottom=0.20, top=0.97)

                runtime_pdf = save_dir / f"{file_base}_runtime_processed.pdf"
                fig_rt.savefig(runtime_pdf, dpi=dpi)
                plt.close(fig_rt)
                print(f"Saved runtime plot: {runtime_pdf}")

            legend_handles = [
                plt.Line2D([0], [0], color="gray", lw=2.2, linestyle="-"),
                plt.Line2D([0], [0], color="gray", lw=2.2, linestyle=LINESTYLES["guarantee"]),
            ]
            legend_labels = ["Performance", "Guarantee"]

            label_styles = {}
            for s in series:
                label_styles.setdefault(s["legend_label"], s["color"])

            preferred_labels = [
                r"$\mathcal{P}_I$",
                r"$\mathcal{P}_{\Theta(\mathcal{U})}$",
                r"$\mathcal{P}_{\Lambda(\mathcal{U})}$",
                r"$\mathcal{P}_{R(\mathcal{U})}$",
            ]
            for label in preferred_labels:
                if label not in label_styles:
                    continue
                legend_handles.append(plt.Line2D([0], [0], color=label_styles[label], lw=2.2, linestyle="-"))
                legend_labels.append(label)

            if legend_handles:
                with plt.rc_context(legend_rcparams):
                    fig_leg, ax_leg = plt.subplots(figsize=(14.8, 1.5), dpi=dpi)
                    ax_leg.axis("off")
                    leg = ax_leg.legend(
                        legend_handles,
                        legend_labels,
                        loc="center",
                        ncol=6,
                        frameon=True,
                        framealpha=0.9,
                        borderpad=0.6,
                        handlelength=1.7,
                        columnspacing=1.4,
                        fontsize=14,
                    )
                    leg.get_frame().set_facecolor("#F7EDEB")
                    leg.get_frame().set_edgecolor("#8E8E8E")
                    leg.get_frame().set_linewidth(0.6)
                    fig_leg.canvas.draw()
                    renderer = fig_leg.canvas.get_renderer()
                    legend_bbox = leg.get_window_extent(renderer).expanded(1.02, 1.12)
                    legend_bbox = legend_bbox.transformed(fig_leg.dpi_scale_trans.inverted())
                    legend_pdf = save_dir / f"{file_base}_legend.pdf"
                    fig_leg.savefig(legend_pdf, dpi=dpi, bbox_inches=legend_bbox)
                    plt.close(fig_leg)
                    print(f"Saved legend: {legend_pdf}")

            plt.close(fig)
            plot_count += 1
            if plot_count <= show_inline:
                fig.show()

    return plot_count


def main() -> None:
    args = parse_args()
    use_tex = configure_matplotlib(args.usetex)
    if args.usetex == "auto" and not use_tex:
        print("LaTeX not found; using matplotlib mathtext fallback (text.usetex=False).")

    count = generate_learning_plots(
        input_root=args.input_root.resolve(),
        output_root=args.output_root.resolve(),
        dpi=args.dpi,
        show_inline=max(0, args.show_inline),
        use_tex=use_tex,
    )
    print(f"Generated learning plot sets: {count}")


if __name__ == "__main__":
    main()
