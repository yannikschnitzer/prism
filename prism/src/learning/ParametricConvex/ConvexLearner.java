package learning.ParametricConvex;

import com.gurobi.gurobi.*;
import common.Interval;
import explicit.*;
import param.Function;
import parser.ast.Expression;
import prism.Evaluator;
import prism.Pair;
import prism.PrismException;

import java.util.*;

/**
 * Builds the convex/IMDP LP, optionally runs iterative OBBT to tighten
 * parameter bounds (which strengthens the McCormick envelopes), and
 * provides the UMDP view with optional shared vertex enumeration.
 *
 * This version:
 *  - Caches every (pmdp, imdp) passed to setConstraints(..) and replays them
 *    on each OBBT rebuild (so ground + bisim are both preserved).
 *  - Does not clear cachedSets in resetModel() (fixes overwrite).
 *  - Pre-commits once before OBBT to ensure bound LPs see constraints.
 */
public class ConvexLearner {

    private final GRBEnv env;
    private GRBModel model;

    private MDPSimple<Function> mdpParam; // default param model when single-arg setConstraints is used
    private ExpressionTranslator trans;

    // Constraint caches (for the *current* model only)
    private final HashMap<String, Pair<GRBLinExpr, Double>> constrLowerBounds = new HashMap<>();
    private final HashMap<String, Pair<GRBLinExpr, Double>> constrUpperBounds = new HashMap<>();
    private final HashMap<String, Expression> sumExps = new HashMap<>();

    // Keep *all* sources added via setConstraints(pmdp, imdp)
    private final ArrayList<Pair<MDPSimple<Function>, UMDP<Double>>> cachedSets = new ArrayList<>();

    // Vertex enumeration (shared across distributions)
    private SharedVertexSet sharedVertices;     // null if not precomputed or cap exceeded
    private int vertexCap = 10_000;            // default cap

    public void setVertexCap(int cap) { this.vertexCap = cap; }

    // -------- OBBT controls --------
    private int obbtMaxRounds = 0;       // 0 => disabled
    private double obbtEps = 1e-6;       // convergence tolerance
    private boolean hasCommittedConstraints = false;

    /** Enable iterative OBBT with a maximum number of rounds and a stopping eps. */
    public void enableOBBT(int maxRounds, double eps) {
        this.obbtMaxRounds = Math.max(0, maxRounds);
        this.obbtEps = Math.max(0.0, eps);
    }

    public ConvexLearner(GRBEnv env) {
        try {
            this.env = env;
            this.model = new GRBModel(env);
            this.trans = new ExpressionTranslator(model);
            // simplex tends to be nicer for repeated objective changes
            this.model.set(GRB.IntParam.Method, 1);
            this.model.set(GRB.IntParam.OutputFlag, 0);
            this.model.set(GRB.DoubleParam.FeasibilityTol, 1e-9);
            this.model.set(GRB.DoubleParam.OptimalityTol, 1e-9);
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    public GRBModel getModel() { return model; }
    public void setModel(GRBModel model) { this.model = model; }
    public MDP<Function> getParamModel() { return mdpParam; }
    public void setParamModel(MDPSimple<Function> mdpParam) { this.mdpParam = mdpParam; }

    /**
     * Reset the *model* and per-build caches, but DO NOT clear cachedSets.
     * cachedSets holds the list of (pmdp,imdp) sources to replay on OBBT rebuilds.
     */
    public void resetModel() throws GRBException {
        this.model = new GRBModel(env);
        this.model.set(GRB.IntParam.Method, 1);
        this.model.set(GRB.IntParam.OutputFlag, 0);
        this.model.set(GRB.DoubleParam.FeasibilityTol, 1e-9);
        this.model.set(GRB.DoubleParam.OptimalityTol, 1e-9);
        this.trans = new ExpressionTranslator(model);

        this.constrLowerBounds.clear();
        this.constrUpperBounds.clear();
        this.sumExps.clear();

        this.sharedVertices = null;
        this.hasCommittedConstraints = false;
    }

    // ---------- Public setConstraints variants (unchanged API) ----------

    /** Build with mdpParam by default. */
    public void setConstraints(UMDP<Double> imdp) throws PrismException, GRBException {
        if (this.mdpParam == null) {
            throw new PrismException("setConstraints(UMDP) called without setParamModel(..)");
        }
        // treat this the same as the (pmdp, imdp) variant and cache it
        cachedSets.add(new Pair<>(mdpParam, imdp));
        setConstraintsCore(mdpParam, imdp);
        model.update();
    }

    /** Adds constraints from (pmdp, imdp) and remembers the source for OBBT replay. */
    public void setConstraints(MDPSimple<Function> pmdp, UMDP<Double> imdp) throws PrismException, GRBException {
        cachedSets.add(new Pair<>(pmdp, imdp));     // <-- remember this source
        setConstraintsCore(pmdp, imdp);
        model.update();
    }

    // ---------- Core builder (no side-effects like OBBT) ----------

    private void setConstraintsCore(MDPSimple<Function> pmdp, UMDP<Double> imdp) throws PrismException, GRBException {
        for (int s = 0; s < imdp.getNumStates(); s++) {
            for (int a = 0; a < imdp.getNumChoices(s); a++) {
                UDistribution<Double> udist = imdp.getUncertainDistribution(s, a);
                if (!(udist instanceof UDistributionIntervals<Double> dist)) {
                    throw new PrismException("Only Interval MDPs supported.");
                }
                Distribution<Interval<Double>> idist = dist.getIntervals();
                Distribution<Function> pdist = pmdp.getDistribution(s, a);

                // Per successor bound constraints
                for (int i : idist.getSupport()) {
                    GRBLinExpr exp = trans.translateLinearExpression(pdist.get(i).asExpression());
                    model.update(); // ensure vars exist before stringifying for key
                    String exprString = ExpressionTranslator.formatGRBExpression(exp);

                    double lower = idist.get(i).getLower();
                    double upper = idist.get(i).getUpper();
                    if (constrUpperBounds.containsKey(exprString)) {
                        lower = Math.max(lower, constrLowerBounds.get(exprString).second);
                        upper = Math.min(upper, constrUpperBounds.get(exprString).second);
                    }
                    constrLowerBounds.put(exprString, new Pair<>(exp, lower));
                    constrUpperBounds.put(exprString, new Pair<>(exp, upper));
                }

                // Normalization: sum of successor probabilities for (s,a) equals 1
                if (pdist.getSupport().size() > 1) {
                    Function sumFunc = pdist.get(pdist.getSupport().iterator().next()).getFactory().getZero();
                    for (int iSucc : pdist.getSupport()) sumFunc = sumFunc.add(pdist.get(iSucc));
                    String sumString = sumFunc.toString();
                    if (!sumExps.containsKey(sumString) && !sumFunc.isOne()) {
                        sumExps.put(sumString, sumFunc.asExpression());
                    }
                }
            }
        }
    }

    /** Materialize cached bound and normalization constraints into the model. */
    public void commitConstraints() throws GRBException, PrismException {
        for (String expString : constrLowerBounds.keySet()) {
            GRBLinExpr exp = constrLowerBounds.get(expString).first;
            model.addConstr(exp, GRB.GREATER_EQUAL, constrLowerBounds.get(expString).second, null);
            model.addConstr(exp, GRB.LESS_EQUAL,   constrUpperBounds.get(expString).second, null);
        }
        for (String sumKey : sumExps.keySet()) {
            GRBLinExpr exp = trans.translateLinearExpression(sumExps.get(sumKey));
            model.addConstr(exp, GRB.EQUAL, 1.0, null);
        }
        hasCommittedConstraints = true;  // bound LPs can safely run now
    }

    // ---------- OBBT: iterative min/max per base variable + rebuild ----------

    public void runObbtLoopAndRebuild() throws GRBException, PrismException {
        if (obbtMaxRounds <= 0) return;

        // Ensure the first bound LPs see the constraints
        if (!hasCommittedConstraints) {
            commitConstraints();
            model.update();
        }

        Map<String, double[]> prev = null;

        for (int round = 0; round < obbtMaxRounds; round++) {
            model.update();

            System.out.println("Running OBBT round " + (round + 1) +  " ...");
            Map<String, double[]> bounds = solveBoundsForBaseParams();
            if (bounds.isEmpty()) break;

            double maxDelta = (prev == null) ? Double.POSITIVE_INFINITY : maxChange(bounds, prev);
            if (prev != null && maxDelta < obbtEps) break;
            prev = bounds;

            // Rebuild model with tightened bounds; keep all sources in cachedSets
            resetModel();

            // push tighter bounds into the translator (used by McCormick variables)
            for (Map.Entry<String, double[]> e : bounds.entrySet()) {
                String name = e.getKey();
                double[] b = e.getValue();
                // clamp if your parameters are probabilities; otherwise remove clamp
                double lb = Math.max(0.0, Math.min(1.0, b[0]));
                double ub = Math.max(0.0, Math.min(1.0, b[1]));
                if (ub - lb < 1e-9) {
                    double mid = 0.5 * (lb + ub);
                    lb = Math.max(0.0, mid - 5e-10);
                    ub = Math.min(1.0, mid + 5e-10);
                }
                trans.setVarBounds(name, lb, ub);
            }

            // Replay *all* sources (ground + bisim, etc.)
            for (Pair<MDPSimple<Function>, UMDP<Double>> src : cachedSets) {
                setConstraintsCore(src.first, src.second);
            }
            commitConstraints();
            model.update();
        }
    }

    private double maxChange(Map<String, double[]> a, Map<String, double[]> b) {
        double md = 0.0;
        for (Map.Entry<String, double[]> e : a.entrySet()) {
            String k = e.getKey();
            double[] va = e.getValue();
            double[] vb = b.get(k);
            if (vb == null) continue;
            md = Math.max(md, Math.abs(va[0] - vb[0]));
            md = Math.max(md, Math.abs(va[1] - vb[1]));
        }
        return md;
    }

    /** Solve two LPs per base parameter to get tight feasible [LB,UB]. */
    private Map<String, double[]> solveBoundsForBaseParams() throws GRBException {
        HashMap<String, double[]> out = new HashMap<>();
        model.update();

        for (GRBVar v : model.getVars()) {
            String name = v.get(GRB.StringAttr.VarName);
            if (!isBaseParamName(name)) continue;

            double lb = solveOneObjective(v, GRB.MINIMIZE);
            double ub = solveOneObjective(v, GRB.MAXIMIZE);

            if (Double.isNaN(lb)) lb = 0.001;
            if (Double.isNaN(ub)) ub = 0.999;
            if (lb > ub) { double t = lb; lb = ub; ub = t; }

            out.put(name, new double[]{lb, ub});
        }
        return out;
    }

    /** Base params are those that are not constants c_*, nor auxiliary * or ^ variables. */
    private boolean isBaseParamName(String name) {
        if (name == null) return false;
        if (name.startsWith("c_")) return false;  // literal constants
        if (name.contains("*"))   return false;   // bilinear McCormick aux
        if (name.contains("^"))   return false;   // square   McCormick aux
        return true;
    }

    private double solveOneObjective(GRBVar v, int sense) throws GRBException {
        GRBLinExpr obj = new GRBLinExpr();
        obj.addTerm(1.0, v);
        model.setObjective(obj, sense);
        model.optimize();
        int status = model.get(GRB.IntAttr.Status);
        if (status == GRB.Status.OPTIMAL) return model.get(GRB.DoubleAttr.ObjVal);
        return Double.NaN;
    }

    // ---------- Vertex enumeration + UMDP export ----------

    // Call this after the final build (after OBBT if enabled)
    public void precomputeVertices() throws GRBException {
        model.update();
        System.out.println("Enumerating Vertices");
        SharedVertexSet.RowCache cache = SharedVertexSet.buildRowCache(model);
        SharedVertexSet sv = SharedVertexSet.fromModel(model, cache, 1e-9, vertexCap, "_mcc");
        if (sv.complete) {
            System.out.println("Precomputed vertices: " + sv.vertexCount);
            this.sharedVertices = sv;
        } else {
            System.out.println("Vertex enumeration exceeded cap (" + vertexCap + "); using LP mode.");
            this.sharedVertices = null; // fall back to LP mode if cap exceeded
        }
    }

    public UMDPSimple<Double> getUMDP() {
        UMDPSimple<Double> convexUMDP = new UMDPSimple<>(mdpParam.getNumStates());
        for (int s = 0; s < mdpParam.getNumStates(); s++) {
            for (int a = 0; a < mdpParam.getNumChoices(s); a++) {
                Distribution<Function> pdist = mdpParam.getDistribution(s, a);
                Object action = mdpParam.getAction(s, a);

                UDistribributionParametricConvex<Double> convex_dist =
                        new UDistribributionParametricConvex<>(pdist, this.model, this.trans, this.sharedVertices);
                convexUMDP.addActionLabelledChoice(s, convex_dist, action);
            }
        }
        return convexUMDP;
    }

    public static void printModel(GRBModel model) throws GRBException {
        model.update();
        System.out.println("-------------");
        System.out.println("Variables:");
        GRBVar[] vars = model.getVars();
        for (GRBVar v : vars) {
            String name = v.get(GRB.StringAttr.VarName);
            double lb = v.get(GRB.DoubleAttr.LB);
            double ub = v.get(GRB.DoubleAttr.UB);
            boolean fixed = Math.abs(lb - ub) < 1e-9;
            System.out.println("Name: " + name + ", LB: " + lb + ", UB: " + ub + ", Fixed: " + fixed);
        }
        System.out.println("-------------");
        System.out.println("Constraints:");
        for (GRBConstr con : model.getConstrs()) {
            System.out.println(ExpressionTranslator.formatGBRConstraint(model, con) + " " + con.get(GRB.StringAttr.ConstrName));
        }
        System.out.println("-------------");
    }

    public ExpressionTranslator getTranslator() {
        return this.trans;
    }

    public GRBEnv getEnv() {
        return this.env;
    }
}