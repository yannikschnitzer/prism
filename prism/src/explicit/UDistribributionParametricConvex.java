package explicit;

import com.gurobi.gurobi.*;
import learning.ParametricConvex.ExpressionTranslator;
import learning.ParametricConvex.SharedVertexSet;
import param.Function;
import prism.PrismException;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Parametric uncertain distribution backed by a shared Gurobi model.
 *
 * Backward compatible with the existing polytope/vertex workflows.
 *
 * NEW (optional): if an APS ellipsoid in parameter space is provided and it is
 * provably contained in the simplex for THIS transition row, we use a closed-form
 * min/max instead of solving an LP/SOCP.
 */
public class UDistribributionParametricConvex<Value> implements UDistribution<Value> {

    protected final Distribution<Function> pdist;
    protected final GRBModel model;
    protected final ExpressionTranslator trans;

    // optional shared vertices
    private final SharedVertexSet shared;
    private boolean useVertices = false;

    // optional APS ellipsoid data (enables closed form when safe)
    private final ApsEllipsoidData aps;
    private boolean apsClosedFormOk = false;
    private boolean forceEllipsoidSOCP = false;

    // pd & successor term extraction
    private ParametricDistribution pd;
    private int succCount;
    private int[] pdIndex;
    private int[][] termCols;
    private double[][] termCoeff;
    private double[] termConst;

    // APS param-only representation: p_i(theta) = pC[i] + pA[i]^T theta
    private double[][] pA;   // [succCount][d]
    private double[] pC;     // [succCount]

    // precomputed P at vertices (only if useVertices)
    private double[] Pflat;           // [vertexCount * succCount]
    private int vertexCount;
    private double[] weightsBuf = new double[128];

    public UDistribributionParametricConvex(Distribution<Function> pdist,
                                            GRBModel model,
                                            ExpressionTranslator trans) {
        this(pdist, model, trans, null, null);
    }

    public UDistribributionParametricConvex(Distribution<Function> pdist,
                                            GRBModel model,
                                            ExpressionTranslator trans,
                                            SharedVertexSet shared) {
        this(pdist, model, trans, shared, null);
    }

    /**
     * NEW: pass APS ellipsoid data. If "safe", mvMultUnc uses closed form.
     * If not safe, behavior is identical to the old code.
     */
    public UDistribributionParametricConvex(Distribution<Function> pdist,
                                            GRBModel model,
                                            ExpressionTranslator trans,
                                            SharedVertexSet shared,
                                            ApsEllipsoidData aps) {
        this.pdist = pdist;
        this.model = model;
        this.trans = trans;
        this.shared = shared;
        this.aps = aps;

        try {
            this.pd = ParametricDistribution.extractParametricDistribution(pdist);
            if (pdist.size() <= 1) return;

            // existing reduced-term extraction (used by the vertex fast path)
            buildSuccessorTermsReduced();

            // if APS data provided, try to enable closed-form
            if (aps != null && aps.d > 0 && aps.cholL != null) {
                buildSuccessorTermsParamOnly();
                if (this.apsClosedFormOk) {
                    this.apsClosedFormOk = checkEllipsoidInsideSimplex();
                }
                // if not ok, we simply fall back to LP/vertex like before
            }

            // old vertex precompute path (unchanged)
            if (shared != null && shared.complete) {
                this.vertexCount = shared.vertexCount;
                this.succCount = pd.size;
                this.Pflat = new double[vertexCount * succCount];

                // evaluate p_i at each reduced-space vertex
                for (int v = 0; v < vertexCount; v++) {
                    int base = v * succCount;
                    int off = v * shared.nFree;
                    for (int i = 0; i < succCount; i++) {
                        double s = termConst[i];
                        int[] idx = termCols[i];
                        double[] cf = termCoeff[i];
                        for (int k = 0; k < idx.length; k++) {
                            s += cf[k] * shared.Vflat[off + idx[k]]; // idx[k] is reduced column
                        }
                        Pflat[base + i] = s;
                    }
                }
                this.useVertices = true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ------- existing UDistribution methods (unchanged) -------
    @Override public boolean contains(int j) { return pdist.contains(j); }
    @Override public boolean isSubsetOf(BitSet set) { return pdist.isSubsetOf(set); }
    @Override public boolean containsOneOf(BitSet set) { return pdist.containsOneOf(set); }
    @Override public Set<Integer> getSupport() { return pdist.getSupport(); }
    @Override public boolean isEmpty() { return pdist.isEmpty(); }
    @Override public int size() { return pdist.size(); }

    // ------- fast path or LP fallback -------
    @Override
    public double mvMultUnc(double[] vect, MinMax minMax) {
        try {
            if (pdist.size() == 1) return vect[pd.index[0]];

            // NEW: closed form over ellipsoid if we proved the ellipsoid stays inside the simplex for this row
            if (aps != null && apsClosedFormOk && !forceEllipsoidSOCP) {
                return mvMultApsClosedForm(vect, minMax);
            }

            if (useVertices) {
                // dot-product scan over precomputed Pflat
                final boolean isMin = minMax.isMinUnc();
                final int m = succCount;
                if (weightsBuf.length < m) weightsBuf = new double[m];
                for (int i = 0; i < m; i++) weightsBuf[i] = vect[pdIndex[i]];

                double best = isMin ? Double.POSITIVE_INFINITY : -Double.POSITIVE_INFINITY;
                int base = 0;
                if (isMin) {
                    for (int v = 0; v < vertexCount; v++, base += m) {
                        double s = 0.0;
                        for (int i = 0; i < m; i++) s += weightsBuf[i] * Pflat[base + i];
                        if (s < best) best = s;
                    }
                } else {
                    for (int v = 0; v < vertexCount; v++, base += m) {
                        double s = 0.0;
                        for (int i = 0; i < m; i++) s += weightsBuf[i] * Pflat[base + i];
                        if (s > best) best = s;
                    }
                }
                return best;
            } else {
                // original solver path (reuse basis; only objective changes)
                GRBLinExpr expr = new GRBLinExpr();
                for (int i = 0; i < pd.size; i++) {
                    expr.add(trans.translateLinearExpression(pd.probs[i].asExpression(), vect[pd.index[i]]));
                }
                model.setObjective(expr, minMax.isMinUnc() ? GRB.MINIMIZE : GRB.MAXIMIZE);
                model.optimize();
                return model.get(GRB.DoubleAttr.ObjVal);
            }
        } catch (GRBException | PrismException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public UDistribution<Value> copy() { throw new UnsupportedOperationException("Not supported yet."); }
    @Override public UDistribution<Value> copy(int[] permut) { throw new UnsupportedOperationException("Not supported yet."); }

    @Override
    public String toString() {
        String s = "Polytopic, Support: " + pdist.getSupport();
        if (aps != null) s += " (APS" + (apsClosedFormOk ? ":closed" : ":solve") + ")";
        if (useVertices) s += " (vertices=" + vertexCount + ")";
        return s;
    }

    // =====================
    // APS CLOSED FORM (safe case only)
    // =====================

    private double mvMultApsClosedForm(double[] vect, MinMax minMax) {
        final int m = succCount;
        final int d = aps.d;

        // weights w_i = value(successor_i)
        if (weightsBuf.length < m) weightsBuf = new double[m];
        for (int i = 0; i < m; i++) weightsBuf[i] = vect[pdIndex[i]];

        // objective: sum_i w_i (pC[i] + pA[i]^T theta) = const + g^T theta
        double constPart = 0.0;
        double[] g = new double[d];
        for (int i = 0; i < m; i++) {
            double w = weightsBuf[i];
            constPart += w * pC[i];
            double[] ai = pA[i];
            for (int j = 0; j < d; j++) g[j] += w * ai[j];
        }

        double valAtMu = constPart + dot(g, aps.mu);

        double quad = aps.quadFormVInv(g); // g^T V^{-1} g
        if (quad < 0.0) quad = 0.0;
        double rad = aps.beta * Math.sqrt(quad);

        return minMax.isMinUnc() ? (valAtMu - rad) : (valAtMu + rad);
    }

    /**
     * Sufficient (and cheap) condition for using closed form:
     * - The row's probabilities sum to 1 identically: sum_i pA_i == 0 and sum_i pC_i == 1.
     * - For every successor i, the ellipsoid implies p_i(theta) in [0,1] for all theta in ellipsoid.
     *   Using ellipsoid support function bound: p_i(mu) ± beta * sqrt(a_i^T V^{-1} a_i).
     */
    private boolean checkEllipsoidInsideSimplex() {
        final int m = succCount;
        final int d = aps.d;

        if (pC == null || pA == null) return false;

        final double epsSum = 1e-9;
        final double epsBox = 1e-10;

        // sum identity check
        double sumC = 0.0;
        double[] sumA = new double[d];
        for (int i = 0; i < m; i++) {
            sumC += pC[i];
            for (int j = 0; j < d; j++) sumA[j] += pA[i][j];
        }
        if (Math.abs(sumC - 1.0) > 1e-7) return false;
        for (int j = 0; j < d; j++) {
            if (Math.abs(sumA[j]) > 1e-7) return false;
        }

        // per-probability box bounds over ellipsoid
        for (int i = 0; i < m; i++) {
            double muPi = pC[i] + dot(pA[i], aps.mu);
            double qi = aps.quadFormVInv(pA[i]);
            if (qi < 0.0) qi = 0.0;
            double ri = aps.beta * Math.sqrt(qi);
            double lo = muPi - ri;
            double hi = muPi + ri;
            if (lo < -epsBox) return false;
            if (hi > 1.0 + epsBox) return false;
        }

//        if (!checkEllipsoidInsideParamBounds()) {
//            System.out.println("returning false here");
//            return false;
//        }

        return true;
    }

    // =====================
    // Term extraction
    // =====================

    // ------- successor term extraction p_i(x) = a_i^T x + c_i (reduced model-var space) -------
    private void buildSuccessorTermsReduced() throws GRBException, PrismException {
        this.succCount = pd.size;
        this.pdIndex   = new int[succCount];
        this.termCols  = new int[succCount][];
        this.termCoeff = new double[succCount][];
        this.termConst = new double[succCount];

        for (int i = 0; i < succCount; i++) {
            GRBLinExpr e = trans.translateLinearExpression(pd.probs[i].asExpression(), 1.0);

            // aggregate but *map to reduced columns*, folding fixed vars into constant
            HashMap<Integer, Double> map = new HashMap<>();
            double cst = getConstantSafe(e);

            int sz = e.size();
            for (int k = 0; k < sz; k++) {
                int j = e.getVar(k).index();     // original column
                double coef = e.getCoeff(k);
                int red = (shared != null) ? shared.colMap[j] : j;
                if (shared != null && red == -1) {
                    // fixed var → fold into constant
                    double fv = shared.fixedVal[j];
                    cst += coef * fv;
                } else {
                    map.put(red, map.getOrDefault(red, 0.0) + coef);
                }
            }

            int nnz = map.size();
            int[] idx = new int[nnz];
            double[] cf = new double[nnz];
            int t = 0;
            for (Map.Entry<Integer, Double> en : map.entrySet()) {
                idx[t] = en.getKey();
                cf[t] = en.getValue();
                t++;
            }
            sortByIndex(idx, cf);

            termCols[i]  = idx;
            termCoeff[i] = cf;
            termConst[i] = cst;
            pdIndex[i]   = pd.index[i];
        }
    }

    /**
     * Build param-only affine form p_i(theta) = pC[i] + pA[i]^T theta.
     * If ANY non-parameter, non-fixed variable appears, we disable closed form.
     */
    private void buildSuccessorTermsParamOnly() throws GRBException, PrismException {
        final int m = succCount;
        final int d = (aps != null) ? aps.d : 0;
        this.pA = new double[m][d];
        this.pC = new double[m];

        for (int i = 0; i < m; i++) {
            GRBLinExpr e = trans.translateLinearExpression(pd.probs[i].asExpression(), 1.0);
            double cst = getConstantSafe(e);

            for (int k = 0; k < e.size(); k++) {
                GRBVar v = e.getVar(k);
                double coef = e.getCoeff(k);
                if (Math.abs(coef) < 1e-15) continue;

                int pj = trans.getParamIndex(v);
                if (pj >= 0) {
                    pA[i][pj] += coef;
                    continue;
                }

                // fixed var? -> fold into constant
                double lb = v.get(GRB.DoubleAttr.LB);
                double ub = v.get(GRB.DoubleAttr.UB);
                if (Double.isFinite(lb) && Double.isFinite(ub) && Math.abs(ub - lb) < 1e-12) {
                    cst += coef * lb;
                    continue;
                }

                // otherwise: not purely affine in parameters
                this.apsClosedFormOk = false;
                this.pA = null;
                this.pC = null;
                return;
            }

            pC[i] = cst;
        }
    }

    // =====================
    // small math/helpers
    // =====================

    private static double dot(double[] a, double[] b) {
        double s = 0.0;
        for (int i = 0; i < a.length; i++) s += a[i] * b[i];
        return s;
    }

    private static double getConstantSafe(GRBLinExpr e) {
        try {
            Method m = e.getClass().getMethod("getConstant");
            Object val = m.invoke(e);
            return (val instanceof Double) ? (Double) val : 0.0;
        } catch (Throwable ignore) {
            return 0.0;
        }
    }

    private static void sortByIndex(int[] idx, double[] cf) {
        Integer[] ord = new Integer[idx.length];
        for (int i = 0; i < idx.length; i++) ord[i] = i;
        Arrays.sort(ord, Comparator.comparingInt(o -> idx[o]));
        int[] idx2 = new int[idx.length];
        double[] cf2 = new double[idx.length];
        for (int i = 0; i < idx.length; i++) {
            idx2[i] = idx[ord[i]];
            cf2[i] = cf[ord[i]];
        }
        System.arraycopy(idx2, 0, idx, 0, idx.length);
        System.arraycopy(cf2, 0, cf, 0, cf.length);
    }

    // =====================
    // APS DATA CONTAINER + LINEAR SOLVES
    // =====================

    /**
     * APS ellipsoid in theta-space: (theta-mu)^T V (theta-mu) <= beta^2.
     * We store a Cholesky factor so we can compute v^T V^{-1} v fast.
     */
    public static final class ApsEllipsoidData {
        public final double[] mu;
        public final double[][] V;
        public final double beta;
        public final int d;
        private final double[][] cholL; // lower-triangular

        public ApsEllipsoidData(double[] mu, double[][] V, double beta) {
            this.mu = mu;
            this.V = V;
            this.beta = beta;
            this.d = (mu == null) ? 0 : mu.length;
            this.cholL = (d == 0) ? null : choleskyLowerSafe(V);
        }

        /** g^T V^{-1} g via Cholesky. */
        double quadFormVInv(double[] g) {
            if (cholL == null) return Double.NaN;
            double[] x = solveSPD(cholL, g); // x = V^{-1} g
            double s = 0.0;
            for (int i = 0; i < d; i++) s += g[i] * x[i];
            return s;
        }

        static double[] solveSPD(double[][] L, double[] b) {
            int n = b.length;
            // forward: L y = b
            double[] y = new double[n];
            for (int i = 0; i < n; i++) {
                double sum = b[i];
                for (int k = 0; k < i; k++) sum -= L[i][k] * y[k];
                y[i] = sum / L[i][i];
            }
            // back: L^T x = y
            double[] x = new double[n];
            for (int i = n - 1; i >= 0; i--) {
                double sum = y[i];
                for (int k = i + 1; k < n; k++) sum -= L[k][i] * x[k];
                x[i] = sum / L[i][i];
            }
            return x;
        }

        static double[][] choleskyLowerSafe(double[][] A) {
            int n = A.length;
            double[][] L = new double[n][n];
            // We add tiny jitter if we encounter non-positive pivots.
            final double jitter = 1e-14;

            for (int i = 0; i < n; i++) {
                for (int j = 0; j <= i; j++) {
                    double sum = A[i][j];
                    for (int k = 0; k < j; k++) sum -= L[i][k] * L[j][k];
                    if (i == j) {
                        if (sum <= 0.0) sum = jitter;
                        L[i][j] = Math.sqrt(sum);
                    } else {
                        L[i][j] = sum / L[j][j];
                    }
                }
            }
            return L;
        }
    }

    private boolean checkEllipsoidInsideParamBounds() {
        final int d = aps.d;
        final double eps = 1e-10;

        // we need parameter variable names in the SAME order as aps.mu (translator order)
        @SuppressWarnings("unchecked")
        final List<String> names = (List<String>) trans.paramNames;

        // reuse one basis vector to avoid allocations
        double[] ej = new double[d];

        for (int j = 0; j < d; j++) {
            String vname = names.get(j);
            GRBVar v;
            try {
                v = model.getVarByName(vname);
                if (v == null) return false;

                double lb = v.get(GRB.DoubleAttr.LB);
                double ub = v.get(GRB.DoubleAttr.UB);

                // If bounds are infinite, skip the corresponding side
                boolean hasLb = Double.isFinite(lb);
                boolean hasUb = Double.isFinite(ub);

                // radius in coordinate j: beta * sqrt(ej^T V^{-1} ej)
                Arrays.fill(ej, 0.0);
                ej[j] = 1.0;
                double q = aps.quadFormVInv(ej);
                if (q < 0.0) q = 0.0;
                double r = aps.beta * Math.sqrt(q);

                double lo = aps.mu[j] - r;
                double hi = aps.mu[j] + r;

                if (hasLb && lo < lb - eps) return false;
                if (hasUb && hi > ub + eps) return false;

            } catch (GRBException e) {
                return false; // be conservative -> fall back to SOCP
            }
        }
        return true;
    }
}