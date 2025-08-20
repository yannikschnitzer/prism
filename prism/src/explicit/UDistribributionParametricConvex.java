package explicit;

import com.gurobi.gurobi.*;
import learning.ParametricConvex.ExpressionTranslator;
import learning.ParametricConvex.SharedVertexSet;
import param.Function;
import prism.PrismException;

import java.lang.reflect.Method;
import java.util.*;

public class UDistribributionParametricConvex<Value> implements UDistribution<Value> {

    protected Distribution<Function> pdist;
    protected GRBModel model;
    protected ExpressionTranslator trans;

    // optional shared vertices
    private final SharedVertexSet shared;
    private boolean useVertices = false;

    // pd & successor term extraction
    private ParametricDistribution pd;
    private int succCount;
    private int[] pdIndex;
    private int[][] termCols;
    private double[][] termCoeff;
    private double[] termConst;

    // precomputed P at vertices (only if useVertices)
    private double[] Pflat;           // [vertexCount * succCount]
    private int vertexCount;
    private double[] weightsBuf = new double[128];

    public UDistribributionParametricConvex(Distribution<Function> pdist,
                                            GRBModel model,
                                            ExpressionTranslator trans) {
        this(pdist, model, trans, null);
    }

    public UDistribributionParametricConvex(Distribution<Function> pdist,
                                            GRBModel model,
                                            ExpressionTranslator trans,
                                            SharedVertexSet shared) {
        this.pdist = pdist;
        this.model = model;
        this.trans = trans;
        this.shared = shared;

        try {
            this.pd = ParametricDistribution.extractParametricDistribution(pdist);
            if (pdist.size() <= 1) return; // trivial case handled in mvMultUnc

            // always needed

            if (shared != null && shared.complete) {

                buildSuccessorTerms();

                this.vertexCount = shared.vertexCount;
                this.succCount = pd.size;
                this.Pflat = new double[vertexCount * succCount];

                // Evaluate each successor expression at each vertex
                for (int v = 0; v < vertexCount; v++) {
                    int base = v * succCount;
                    int off = v * shared.nVars;
                    for (int i = 0; i < succCount; i++) {
                        double s = termConst[i];
                        int[] idx = termCols[i];
                        double[] cf = termCoeff[i];
                        for (int k = 0; k < idx.length; k++) {
                            s += cf[k] * shared.Vflat[off + idx[k]];
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
                        double s = 0.0; for (int i = 0; i < m; i++) s += weightsBuf[i] * Pflat[base + i];
                        if (s < best) best = s;
                    }
                } else {
                    for (int v = 0; v < vertexCount; v++, base += m) {
                        double s = 0.0; for (int i = 0; i < m; i++) s += weightsBuf[i] * Pflat[base + i];
                        if (s > best) best = s;
                    }
                }
                return best;
            } else {
                // original LP path (reuse basis; only objective changes)
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
        if (useVertices) s += " (vertices=" + vertexCount + ")";
        return s;
    }

    // ------- successor term extraction p_i(x) = a_i^T x + c_i -------
    private void buildSuccessorTerms() throws GRBException, PrismException {
        this.succCount = pd.size;
        this.pdIndex   = new int[succCount];
        this.termCols  = new int[succCount][];
        this.termCoeff = new double[succCount][];
        this.termConst = new double[succCount];

        // map var -> col using IdentityHashMap for speed
        GRBVar[] vars = model.getVars();
        IdentityHashMap<GRBVar,Integer> var2col = new IdentityHashMap<>(vars.length * 2);
        for (int j = 0; j < vars.length; j++) var2col.put(vars[j], j);

        for (int i = 0; i < succCount; i++) {
            GRBLinExpr e = trans.translateLinearExpression(pd.probs[i].asExpression(), 1.0);

            HashMap<Integer, Double> map = new HashMap<>();
            int sz = e.size();
            for (int k = 0; k < sz; k++) {
                Integer j = var2col.get(e.getVar(k));
                map.put(j, map.getOrDefault(j, 0.0) + e.getCoeff(k));
            }
            int nnz = map.size();
            int[] idx = new int[nnz];
            double[] cf = new double[nnz];
            int t = 0;
            for (Map.Entry<Integer, Double> en : map.entrySet()) { idx[t] = en.getKey(); cf[t] = en.getValue(); t++; }
            sortByIndex(idx, cf);

            termCols[i]  = idx;
            termCoeff[i] = cf;
            termConst[i] = getConstantSafe(e);
            pdIndex[i]   = pd.index[i];
        }
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
        int[] idx2 = new int[idx.length]; double[] cf2 = new double[idx.length];
        for (int i = 0; i < idx.length; i++) { idx2[i] = idx[ord[i]]; cf2[i] = cf[ord[i]]; }
        System.arraycopy(idx2, 0, idx, 0, idx.length);
        System.arraycopy(cf2, 0, cf, 0, cf.length);
    }
}