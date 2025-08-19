package learning.ParametricConvex;

import com.gurobi.gurobi.*;

import java.util.*;

/** Holds a shared vertex set for a polytope {x : A x <= b} of a given Gurobi model. */
public final class SharedVertexSet {
    public final double[] Vflat;     // [vertexCount * nVars], or null if incomplete
    public final int vertexCount;    // number of vertices found (or > cap if incomplete)
    public final int nVars;
    public final boolean complete;   // true iff full enumeration finished within cap

    private SharedVertexSet(double[] Vflat, int vertexCount, int nVars, boolean complete) {
        this.Vflat = Vflat; this.vertexCount = vertexCount; this.nVars = nVars; this.complete = complete;
    }

    /** Build the H-rep from model and enumerate all vertices up to cap. */
    public static SharedVertexSet fromModel(GRBModel model, double tolFeas, int cap) throws GRBException {
        if (cap <= 0) throw new IllegalArgumentException("cap must be positive");
        GRBVar[] vars = model.getVars();
        int n = vars.length;
        IdentityHashMap<GRBVar,Integer> var2col = new IdentityHashMap<>(n * 2);
        for (int j = 0; j < n; j++) var2col.put(vars[j], j);

        // Build A x <= b (dense rows), including bounds; '=' split to two '≤'
        ArrayList<double[]> rows = new ArrayList<>();
        ArrayList<Double> rhs = new ArrayList<>();

        for (GRBConstr c : model.getConstrs()) {
            GRBLinExpr row = model.getRow(c);
            char s = c.get(GRB.CharAttr.Sense); // '<', '>', '='
            double r = c.get(GRB.DoubleAttr.RHS);

            double[] a = new double[n];
            int sz = row.size();
            for (int k = 0; k < sz; k++) {
                Integer j = var2col.get(row.getVar(k));
                a[j] += row.getCoeff(k);
            }
            if (s == '<') {
                rows.add(a); rhs.add(r);
            } else if (s == '>') {
                for (int j = 0; j < n; j++) a[j] = -a[j];
                rows.add(a); rhs.add(-r);
            } else { // '='
                rows.add(a.clone()); rhs.add(r);
                for (int j = 0; j < n; j++) a[j] = -a[j];
                rows.add(a); rhs.add(-r);
            }
        }
        for (int j = 0; j < n; j++) {
            double lb = vars[j].get(GRB.DoubleAttr.LB);
            double ub = vars[j].get(GRB.DoubleAttr.UB);
            if (!Double.isInfinite(ub)) { double[] a = new double[n]; a[j] = 1.0; rows.add(a); rhs.add(ub); }
            if (!Double.isInfinite(lb)) { double[] a = new double[n]; a[j] = -1.0; rows.add(a); rhs.add(-lb); }
        }

        double[][] A = rows.toArray(new double[0][]);
        double[] b = new double[rhs.size()];
        for (int i = 0; i < b.length; i++) b[i] = rhs.get(i);

        // Scale rows to unit-2-norm (for numeric stability of tightness checks)
        double[] rowNorm = new double[A.length];
        for (int i = 0; i < A.length; i++) {
            double s = 0; for (double v : A[i]) s += v * v;
            double inv = 1.0 / Math.sqrt(Math.max(1e-30, s));
            for (int j = 0; j < n; j++) A[i][j] *= inv;
            b[i] *= inv;
            rowNorm[i] = 1.0; // after scaling
        }

        // Enumerate vertices (active-set over all n-row subsets), stop once > cap
        final double TOL_SING = 1e-12;
        ArrayList<double[]> verts = new ArrayList<>(Math.min(cap, 1024));
        int m = A.length;
        int[] comb = firstComb(n);
        double[][] AS = new double[n][n];
        double[] bS = new double[n];

        while (comb != null) {
            for (int r = 0; r < n; r++) {
                System.arraycopy(A[comb[r]], 0, AS[r], 0, n);
                bS[r] = b[comb[r]];
            }
            double[] x = solve(AS, bS, TOL_SING);
            if (x != null && feasible(A, b, x, tolFeas) && tight(A, b, x, comb, tolFeas, rowNorm)) {
                if (!exists(verts, x, 1e-9)) {
                    verts.add(x.clone());
                    if (verts.size() > cap) {
                        // cap exceeded: tell caller not to use vertices
                        return new SharedVertexSet(null, verts.size(), n, false);
                    }
                }
            }
            comb = nextComb(comb, m, n);
        }

        // flatten
        int V = verts.size();
        double[] Vflat = new double[V * n];
        for (int v = 0; v < V; v++) System.arraycopy(verts.get(v), 0, Vflat, v * n, n);
        return new SharedVertexSet(Vflat, V, n, true);
    }

    // --- helpers (tight, feasible, combinations, solve) ---
    private static boolean feasible(double[][] A, double[] b, double[] x, double tol) {
        int m = A.length, n = A[0].length;
        for (int i = 0; i < m; i++) {
            double s = 0.0; for (int j = 0; j < n; j++) s += A[i][j] * x[j];
            if (s > b[i] + tol) return false;
        }
        return true;
    }
    private static boolean tight(double[][] A, double[] b, double[] x, int[] active, double tol, double[] rowNorm) {
        int n = A[0].length;
        for (int idx : active) {
            double s = 0.0; for (int j = 0; j < n; j++) s += A[idx][j] * x[j];
            if (Math.abs(s - b[idx]) > tol * (1.0 + rowNorm[idx])) return false;
        }
        return true;
    }
    private static boolean exists(ArrayList<double[]> list, double[] x, double tol) {
        outer:
        for (double[] y : list) {
            if (y.length != x.length) continue;
            for (int i = 0; i < x.length; i++) if (Math.abs(x[i] - y[i]) > tol) continue outer;
            return true;
        }
        return false;
    }
    private static int[] firstComb(int k) { int[] c = new int[k]; for (int i = 0; i < k; i++) c[i] = i; return c; }
    private static int[] nextComb(int[] c, int m, int k) {
        int i = k - 1;
        while (i >= 0 && c[i] == m - k + i) i--;
        if (i < 0) return null;
        c[i]++;
        for (int j = i + 1; j < k; j++) c[j] = c[j - 1] + 1;
        return c;
    }
    private static double[] solve(double[][] A, double[] b, double eps) {
        int n = b.length;
        double[][] M = new double[n][n];
        double[] rhs = new double[n];
        for (int i = 0; i < n; i++) { System.arraycopy(A[i], 0, M[i], 0, n); rhs[i] = b[i]; }
        for (int k = 0; k < n; k++) {
            int sel = k; double best = Math.abs(M[k][k]);
            for (int i = k + 1; i < n; i++) { double v = Math.abs(M[i][k]); if (v > best) { best = v; sel = i; } }
            if (best < eps) return null;
            if (sel != k) { double[] tmp = M[k]; M[k] = M[sel]; M[sel] = tmp; double t = rhs[k]; rhs[k] = rhs[sel]; rhs[sel] = t; }
            double piv = M[k][k];
            for (int i = k + 1; i < n; i++) {
                double f = M[i][k] / piv; if (f == 0) continue;
                rhs[i] -= f * rhs[k];
                for (int j = k; j < n; j++) M[i][j] -= f * M[k][j];
            }
        }
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double s = rhs[i]; for (int j = i + 1; j < n; j++) s -= M[i][j] * x[j];
            double d = M[i][i]; if (Math.abs(d) < eps) return null;
            x[i] = s / d;
        }
        return x;
    }
}