// explicit/SharedVertexSet.java
package learning.ParametricConvex;

import com.gurobi.gurobi.*;
import java.util.*;

public final class SharedVertexSet {
    // vertices in the *reduced* space (only free vars), row-major
    public final double[] Vflat;    // length: vertexCount * nFree
    public final int vertexCount;
    public final int nFree;

    // mapping & fixed values
    public final int[] freeCols;    // original var index for each reduced column k
    public final int[] colMap;      // original col j -> reduced col k (or -1 if fixed)
    public final double[] fixedVal; // original col j -> value if fixed, else NaN

    public final boolean complete;

    private SharedVertexSet(double[] Vflat, int vertexCount, int nFree,
                            int[] freeCols, int[] colMap, double[] fixedVal, boolean complete) {
        this.Vflat = Vflat; this.vertexCount = vertexCount; this.nFree = nFree;
        this.freeCols = freeCols; this.colMap = colMap; this.fixedVal = fixedVal; this.complete = complete;
    }

    /* =========================  Row cache (optional)  ========================= */

    /** One-time cache of constraint sparsity & coefficients. */
    public static final class RowCache {
        public final GRBModel model;        // identity check
        public final int m;                 // #constr
        public final int n;                 // #vars
        public final int[][] cols;          // per row: var indices
        public final double[][] vals;       // per row: coefficients
        public final char[] sense;          // per row: sense
        public final String[] names;        // optional: names (detect McCormick)
        private RowCache(GRBModel model, int m, int n,
                         int[][] cols, double[][] vals, char[] sense, String[] names) {
            this.model = model; this.m = m; this.n = n;
            this.cols = cols; this.vals = vals; this.sense = sense; this.names = names;
        }
    }

    /** Build once after the model is finalized (constraints added + update()). */
    public static RowCache buildRowCache(GRBModel model) throws GRBException {
        GRBConstr[] constrs = model.getConstrs();
        GRBVar[] vars = model.getVars();
        int m = constrs.length, n = vars.length;

        int[][] cols = new int[m][];
        double[][] vals = new double[m][];
        char[] sense = new char[m];
        String[] names = new String[m];

        for (int i = 0; i < m; i++) {
            GRBConstr c = constrs[i];
            GRBLinExpr row = model.getRow(c);  // one-time cost
            int sz = row.size();
            int[] ci = new int[sz];
            double[] vi = new double[sz];
            for (int k = 0; k < sz; k++) {
                ci[k] = row.getVar(k).index();
                vi[k] = row.getCoeff(k);
            }
            cols[i] = ci; vals[i] = vi;
            sense[i] = c.get(GRB.CharAttr.Sense);
            names[i] = c.get(GRB.StringAttr.ConstrName); // may be empty
        }
        return new RowCache(model, m, n, cols, vals, sense, names);
    }

    /* =========================  public builders  ========================= */

    /** Original entry point (no cache). */
    public static SharedVertexSet fromModel(GRBModel model, double tolFeas, int cap) throws GRBException {
        return fromModel(model, null, tolFeas, cap, null);
    }

    /**
     * Faster entry point using a RowCache. Only rows whose name *contains* the
     * marker (e.g. "_mcc") are re-read from the model each call, because their
     * coefficients/RHS depend on current bounds. All other rows use the cached
     * coefficients.
     *
     * @param mccMarker e.g. "_mcc" to catch names like "p*q_mcc1". If null/empty,
     *                  the cache is used for *all* rows.
     */
    public static SharedVertexSet fromModel(GRBModel model,
                                            RowCache cache,
                                            double tolFeas,
                                            int cap,
                                            String mccMarker) throws GRBException {
        if (cap <= 0) throw new IllegalArgumentException("cap must be > 0");

        final double BIG = 1e90;
        GRBVar[] vars = model.getVars();
        final int nAll = vars.length;

        // ---- detect fixed vars / bounds
        double[] lb = new double[nAll], ub = new double[nAll];
        boolean[] fixed = new boolean[nAll];
        double[] fixedVal = new double[nAll];
        int nFree = 0;
        for (int j = 0; j < nAll; j++) {
            lb[j] = vars[j].get(GRB.DoubleAttr.LB);
            ub[j] = vars[j].get(GRB.DoubleAttr.UB);
            boolean infLB = Double.isInfinite(lb[j]) || Math.abs(lb[j]) >= BIG;
            boolean infUB = Double.isInfinite(ub[j]) || Math.abs(ub[j]) >= BIG;
            boolean isFixed = !infLB && !infUB && Math.abs(ub[j] - lb[j]) <= 1e-12;
            fixed[j] = isFixed;
            fixedVal[j] = isFixed ? lb[j] : Double.NaN;
            if (!isFixed) nFree++;
        }
        int[] freeCols = new int[nFree];
        int[] colMap = new int[nAll]; Arrays.fill(colMap, -1);
        for (int j = 0, k = 0; j < nAll; j++) if (!fixed[j]) { colMap[j] = k; freeCols[k++] = j; }

        if (nFree == 0) {
            return new SharedVertexSet(new double[0], 1, 0, new int[0], colMap, fixedVal, true);
        }

        // ---- Build A_free x <= b' using cached rows (except McCormick)
        final ArrayList<double[]> rows = new ArrayList<>();
        final ArrayList<Double> rhs = new ArrayList<>();

        final GRBConstr[] constrs = model.getConstrs();
        final boolean useCache = (cache != null && cache.model == model &&
                cache.n == nAll && cache.m == constrs.length);
        final boolean checkMcc = (mccMarker != null && !mccMarker.isEmpty());

        // workspace
        final double[] tmp = new double[nFree];

        for (int i = 0; i < constrs.length; i++) {
            char s;
            int[] ci;
            double[] vi;

            boolean isMcc = false;
            if (useCache) {
                String nm = cache.names[i];
                isMcc = checkMcc && nm != null && nm.contains(mccMarker);
            }

            if (useCache && !isMcc) {
                s = cache.sense[i];
                ci = cache.cols[i];
                vi = cache.vals[i];
            } else {
                GRBConstr c = constrs[i];
                s = c.get(GRB.CharAttr.Sense);
                GRBLinExpr row = model.getRow(c); // only for MCC or if no cache
                int sz = row.size();
                ci = new int[sz]; vi = new double[sz];
                for (int k = 0; k < sz; k++) { ci[k] = row.getVar(k).index(); vi[k] = row.getCoeff(k); }
            }

            // reduced row
            Arrays.fill(tmp, 0.0);
            double shift = 0.0;
            for (int k = 0; k < ci.length; k++) {
                int j = ci[k];
                double a = vi[k];
                if (fixed[j]) shift += a * fixedVal[j];
                else tmp[colMap[j]] += a;
            }
            double bval = constrs[i].get(GRB.DoubleAttr.RHS) - shift;

            if (s == GRB.LESS_EQUAL) {
                rows.add(Arrays.copyOf(tmp, nFree)); rhs.add(bval);
            } else if (s == GRB.GREATER_EQUAL) {
                double[] neg = Arrays.copyOf(tmp, nFree);
                for (int t = 0; t < nFree; t++) neg[t] = -neg[t];
                rows.add(neg); rhs.add(-bval);
            } else { // '='
                rows.add(Arrays.copyOf(tmp, nFree)); rhs.add(bval);
                double[] neg = Arrays.copyOf(tmp, nFree);
                for (int t = 0; t < nFree; t++) neg[t] = -neg[t];
                rows.add(neg); rhs.add(-bval);
            }
        }

        // bounds for FREE vars only
        for (int j = 0; j < nAll; j++) {
            if (fixed[j]) continue;
            boolean infLB = Double.isInfinite(lb[j]) || Math.abs(lb[j]) >= BIG;
            boolean infUB = Double.isInfinite(ub[j]) || Math.abs(ub[j]) >= BIG;
            if (!infUB) { double[] a = new double[nFree]; a[colMap[j]] =  1.0; rows.add(a); rhs.add( ub[j]); }
            if (!infLB) { double[] a = new double[nFree]; a[colMap[j]] = -1.0; rows.add(a); rhs.add(-lb[j]); }
        }

        // materialize A,b
        final int m = rows.size();
        double[][] A = new double[m][];
        for (int i = 0; i < m; i++) A[i] = rows.get(i);
        double[] b = new double[rhs.size()];
        for (int i = 0; i < b.length; i++) b[i] = rhs.get(i);

        return enumerateVertices(A, b, tolFeas, cap, freeCols, colMap, fixedVal);
    }

    /* =========================  enumeration core  ========================= */

    private static SharedVertexSet enumerateVertices(double[][] A, double[] b, double tolFeas, int cap,
                                                     int[] freeCols, int[] colMap, double[] fixedVal) {
        final int nFree = A[0].length;
        final int m = A.length;

        // pre-allocate solver workspace (thread-local)
        Workspace W = WORK.get();
        W.ensure(nFree);

        ArrayList<double[]> verts = new ArrayList<>(Math.min(cap, 1024));
        int[] comb = firstComb(nFree);
        final double TOL_SING = 1e-12;

        while (comb != null) {
            // copy active set into W.M, W.rhs
            for (int r = 0; r < nFree; r++) {
                System.arraycopy(A[comb[r]], 0, W.M[r], 0, nFree);
                W.rhs[r] = b[comb[r]];
            }
            // solve and check
            double[] x = solveInPlace(W, nFree, TOL_SING);
            if (x != null && feasible(A, b, x, tolFeas) && tight(A, b, x, comb, tolFeas)) {
                if (!exists(verts, x, 1e-9)) {
                    verts.add(Arrays.copyOf(x, nFree));
                    if (verts.size() > cap) {
                        return new SharedVertexSet(null, verts.size(), nFree, freeCols, colMap, fixedVal, false);
                    }
                }
            }
            comb = nextComb(comb, m, nFree);
        }

        int V = verts.size();
        double[] Vflat = new double[V * nFree];
        for (int v = 0; v < V; v++) System.arraycopy(verts.get(v), 0, Vflat, v*nFree, nFree);
        return new SharedVertexSet(Vflat, V, nFree, freeCols, colMap, fixedVal, true);
    }

    // -------- feasibility & helpers (syntax fix here) --------
    private static boolean feasible(double[][] A, double[] b, double[] x, double tol) {
        for (int i = 0; i < A.length; i++) {
            double s = 0.0;
            double[] row = A[i];
            for (int j = 0; j < row.length; j++) s += row[j] * x[j];
            if (s > b[i] + tol) return false;
        }
        return true;
    }

    private static boolean tight(double[][] A, double[] b, double[] x, int[] act, double tol) {
        int n = x.length;
        for (int idx : act) {
            double s = 0.0;
            double[] row = A[idx];
            for (int j = 0; j < n; j++) s += row[j] * x[j];
            if (Math.abs(s - b[idx]) > tol) return false;
        }
        return true;
    }

    private static boolean exists(ArrayList<double[]> list, double[] x, double tol) {
        outer: for (double[] y : list) {
            if (y.length != x.length) continue;
            for (int i = 0; i < x.length; i++) if (Math.abs(x[i]-y[i]) > tol) continue outer;
            return true;
        }
        return false;
    }

    private static int[] firstComb(int k){ int[] c=new int[k]; for(int i=0;i<k;i++) c[i]=i; return c; }
    private static int[] nextComb(int[] c,int m,int k){ int i=k-1; while(i>=0 && c[i]==m-k+i) i--; if(i<0) return null; c[i]++; for(int j=i+1;j<k;j++) c[j]=c[j-1]+1; return c; }

    /* -------- in-place Gaussian elimination with reusable workspace -------- */

    private static final class Workspace {
        double[][] M = new double[0][0];
        double[] rhs = new double[0];
        double[] x = new double[0];
        void ensure(int n){
            if (M.length < n) M = new double[n][n];
            for (int i=0;i<n;i++) if (M[i].length < n) M[i] = new double[n];
            if (rhs.length < n) rhs = new double[n];
            if (x.length < n) x = new double[n];
        }
    }
    private static final ThreadLocal<Workspace> WORK = ThreadLocal.withInitial(Workspace::new);

    private static double[] solveInPlace(Workspace W, int n, double eps) {
        double[][] M = W.M; double[] rhs = W.rhs; double[] x = W.x;

        for (int k = 0; k < n; k++) {
            int sel = k; double best = Math.abs(M[k][k]);
            for (int i = k+1; i < n; i++) { double v = Math.abs(M[i][k]); if (v > best){ best=v; sel=i; } }
            if (best < eps) return null;
            if (sel != k) { double[] tmp = M[k]; M[k] = M[sel]; M[sel] = tmp; double t=rhs[k]; rhs[k]=rhs[sel]; rhs[sel]=t; }
            double piv = M[k][k];
            for (int i = k+1; i < n; i++) {
                double f = M[i][k] / piv;
                if (f == 0.0) continue;
                rhs[i] -= f * rhs[k];
                for (int j = k; j < n; j++) M[i][j] -= f * M[k][j];
            }
        }
        for (int i = n-1; i >= 0; i--) {
            double s = rhs[i];
            for (int j = i+1; j < n; j++) s -= M[i][j] * x[j];
            double d = M[i][i]; if (Math.abs(d) < eps) return null;
            x[i] = s / d;
        }
        return x;
    }
}