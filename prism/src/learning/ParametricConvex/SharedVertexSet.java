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

    public static SharedVertexSet fromModel(GRBModel model, double tolFeas, int cap) throws GRBException {
        if (cap <= 0) throw new IllegalArgumentException("cap must be > 0");

        final double BIG = 1e90; // treat |bound| >= BIG as unbounded

        GRBVar[] vars = model.getVars();
        final int nAll = vars.length;

        // detect fixed vars and build maps
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
        int[] colMap = new int[nAll];
        Arrays.fill(colMap, -1);
        for (int j = 0, k = 0; j < nAll; j++) if (!fixed[j]) { colMap[j] = k; freeCols[k++] = j; }

        // short-circuit: no free vars → single “vertex”
        if (nFree == 0) {
            return new SharedVertexSet(new double[0], 1, 0, new int[0], colMap, fixedVal, true);
        }

        // Build A_free x_free <= b' (substitute fixed vars into RHS)
        ArrayList<double[]> rows = new ArrayList<>();
        ArrayList<Double> rhs = new ArrayList<>();

        for (GRBConstr c : model.getConstrs()) {
            GRBLinExpr row = model.getRow(c);
            char s = c.get(GRB.CharAttr.Sense); // '<', '>', '='
            double r = c.get(GRB.DoubleAttr.RHS);

            double[] aFree = new double[nFree];
            double shift = 0.0;

            int sz = row.size();
            for (int k = 0; k < sz; k++) {
                GRBVar v = row.getVar(k);
                int j = v.index(); // stable index in this model
                double coef = row.getCoeff(k);
                if (fixed[j]) {
                    shift += coef * fixedVal[j];
                } else {
                    int jj = colMap[j];
                    aFree[jj] += coef;
                }
            }
            double bval = r - shift;

            if (s == '<') {
                rows.add(aFree); rhs.add(bval);
            } else if (s == '>') {
                for (int j = 0; j < nFree; j++) aFree[j] = -aFree[j];
                rows.add(aFree); rhs.add(-bval);
            } else { // '='
                rows.add(aFree.clone()); rhs.add(bval);
                for (int j = 0; j < nFree; j++) aFree[j] = -aFree[j];
                rows.add(aFree); rhs.add(-bval);
            }
        }

        // add bounds for FREE vars only
        for (int j = 0; j < nAll; j++) {
            if (fixed[j]) continue;
            boolean infLB = Double.isInfinite(lb[j]) || Math.abs(lb[j]) >= BIG;
            boolean infUB = Double.isInfinite(ub[j]) || Math.abs(ub[j]) >= BIG;
            if (!infUB) { double[] a = new double[nFree]; a[colMap[j]] =  1.0; rows.add(a); rhs.add( ub[j]); }
            if (!infLB) { double[] a = new double[nFree]; a[colMap[j]] = -1.0; rows.add(a); rhs.add(-lb[j]); }
        }

        double[][] A = rows.toArray(new double[0][]);
        double[] b = new double[rhs.size()];
        for (int i = 0; i < b.length; i++) b[i] = rhs.get(i);

        // scale rows (cheap normalization used only for comparisons)
        double[] rowNorm = new double[A.length];
        for (int i = 0; i < A.length; i++) {
            double s = 0; for (double v : A[i]) s += v*v;
            double inv = 1.0 / Math.sqrt(Math.max(1e-30, s));
            for (int j = 0; j < nFree; j++) A[i][j] *= inv;
            b[i] *= inv;
            rowNorm[i] = 1.0; // keep semantics identical to before
        }

        // enumerate vertices over nFree
        ArrayList<double[]> verts = new ArrayList<>(Math.min(cap, 1024));
        int m = A.length;
        int[] comb = firstComb(nFree);
        final double TOL_SING = 1e-12;

        // Reusable linear-solve workspace
        LinWS ws = new LinWS(nFree);

        while (comb != null) {
            // Fill workspace from active rows (no AS/bS allocations)
            for (int r = 0; r < nFree; r++) {
                System.arraycopy(A[comb[r]], 0, ws.M[r], 0, nFree);
                ws.rhs[r] = b[comb[r]];
            }

            if (solveInPlace(nFree, ws, TOL_SING)) {
                double[] x = ws.x;
                if (feasible(A, b, x, tolFeas) && tight(A, b, x, comb, tolFeas, rowNorm)) {
                    if (!exists(verts, x, 1e-9)) {
                        verts.add(x.clone()); // copy before next iteration overwrites ws.x
                        if (verts.size() > cap) {
                            return new SharedVertexSet(null, verts.size(), nFree, freeCols, colMap, fixedVal, false);
                        }
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

    // ---------- helpers ----------

    private static boolean feasible(double[][] A, double[] b, double[] x, double tol) {
        int m = A.length, n = A[0].length;
        for (int i = 0; i < m; i++) {
            double s = 0.0; for (int j = 0; j < n; j++) s += A[i][j]*x[j];
            if (s > b[i] + tol) return false;
        }
        return true;
    }

    private static boolean tight(double[][] A, double[] b, double[] x, int[] act, double tol, double[] rowNorm) {
        int n = A[0].length;
        for (int idx : act) {
            double s = 0.0; for (int j = 0; j < n; j++) s += A[idx][j]*x[j];
            if (Math.abs(s - b[idx]) > tol * (1.0 + rowNorm[idx])) return false;
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
    private static int[] nextComb(int[] c,int m,int k){
        int i=k-1; while(i>=0 && c[i]==m-k+i) i--;
        if(i<0) return null;
        c[i]++; for(int j=i+1;j<k;j++) c[j]=c[j-1]+1;
        return c;
    }

    // ---------- Linear solve (workspace-based, in-place) ----------

    private static final class LinWS {
        final double[][] M;   // n x n work matrix
        final double[] rhs;   // n work rhs
        final double[] x;     // n solution buffer (reused)
        LinWS(int n) { M = new double[n][n]; rhs = new double[n]; x = new double[n]; }
    }

    /**
     * Solve M x = rhs in-place using partial-pivot Gaussian elimination.
     * The matrix and rhs must be pre-filled in the workspace. Returns true if solved; false if singular.
     */
    private static boolean solveInPlace(int n, LinWS ws, double eps) {
        // Forward elimination
        for (int k = 0; k < n; k++) {
            int sel = k; double best = Math.abs(ws.M[k][k]);
            for (int i = k + 1; i < n; i++) { double v = Math.abs(ws.M[i][k]); if (v > best) { best = v; sel = i; } }
            if (best < eps) return false; // singular / ill-conditioned

            if (sel != k) {
                double[] tmp = ws.M[k]; ws.M[k] = ws.M[sel]; ws.M[sel] = tmp;
                double t = ws.rhs[k];   ws.rhs[k] = ws.rhs[sel]; ws.rhs[sel] = t;
            }

            double piv = ws.M[k][k];
            double invPiv = 1.0 / piv;
            for (int i = k + 1; i < n; i++) {
                double f = ws.M[i][k] * invPiv;
                if (f == 0.0) continue;
                ws.rhs[i] -= f * ws.rhs[k];
                ws.M[i][k] = 0.0; // exact zero; saves one multiply below
                for (int j = k + 1; j < n; j++) ws.M[i][j] -= f * ws.M[k][j];
            }
        }

        // Back substitution
        for (int i = n - 1; i >= 0; i--) {
            double s = ws.rhs[i];
            for (int j = i + 1; j < n; j++) s -= ws.M[i][j] * ws.x[j];
            double d = ws.M[i][i];
            if (Math.abs(d) < eps) return false; // extra guard
            ws.x[i] = s / d;
        }
        return true;
    }
}