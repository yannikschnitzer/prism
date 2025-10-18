package learning.ParametricConvex;

import com.gurobi.gurobi.*;
import explicit.Distribution;
import explicit.MDPSimple;
import param.Function;
import parser.ast.Expression;
import prism.PrismException;

import java.util.*;

/**
 * Solves a robust DTMC with a global parametric uncertainty polytope (Aθ <= b).
 * This is the FINAL, corrected version that combines two key insights:
 * 1. The sum-to-one probability constraint must be explicitly dualized for each state,
 * introducing a state-local dual variable `beta_s`. This prevents a mathematical flaw in
 * simpler formulations that leads to trivial (0.0 or 1.0) solutions.
 * 2. The Linear Program for a minimum-cost problem (min reachability, min steps) must
 * MAXIMIZE the value variables subject to an upper-bound (<=) from the robust Bellman backup.
 *
 * This implementation correctly models the dual of the inner problem:
 * min_{θ: Aθ<=b}  (c_s(x)^T θ + d_s(x))   subject to   sum_t P_s,t(θ) = 1
 *
 * which is then embedded into the master LP.
 */
public final class RobustDTMCOneShotLP {

    public enum Objective { REACH_PROB_MIN, EXPECTED_STEPS_MIN }

    public static final class Result {
        public final double[] x;
        public final double   x0;
        public final int      numDualVars;
        public final int      numConstr;
        public final int      numVars;
        Result(double[] x, double x0, int numDualVars, int numConstr, int numVars) {
            this.x = x; this.x0 = x0; this.numDualVars = numDualVars;
            this.numConstr = numConstr; this.numVars = numVars;
        }
    }

    public static boolean isDTMC(MDPSimple<?> mdp) {
        int n = mdp.getNumStates();
        for (int s = 0; s < n; s++) if (mdp.getNumChoices(s) != 1) return false;
        return true;
    }

    public static Result solve(ConvexLearner cxl,
                               MDPSimple<Function> pmdp,
                               BitSet goal,
                               Objective objType) throws GRBException, PrismException {

        if (!isDTMC(pmdp)) {
            throw new IllegalArgumentException("One-shot LP requires a DTMC (one choice per state).");
        }

        // --- 1) Extract polytope A_free θ_free <= b' ---
        GRBModel paramModel = cxl.getModel();
        paramModel.update();
        PolyFree poly = extractReducedPolytope(paramModel);

        // --- 2) Pre-translate transitions and pre-compute sum-to-one constraint coefficients ---
        final int n = pmdp.getNumStates();
        final int s0 = pmdp.getFirstInitialState();
        final int nZ = poly.nFree;

        // C_s(x)_j = sum_t alpha_{s,t,j} * x_t
        // D_s(x) = sum_t beta_{s,t} * x_t
        ArrayList<HashMap<Integer,Double>> alpha = new ArrayList<>(n * 8); // Stores alpha_{s,t,j}
        double[][] beta = new double[n][]; // Stores beta_{s,t}

        // Coefficients for sum-to-one constraint: u_s^T θ = 1 - v_s
        double[][] u = new double[n][nZ]; // u_{s,j} = sum_t alpha_{s,t,j}
        double[] v = new double[n];       // v_s = sum_t beta_{s,t}

        ExpressionTranslator trans = cxl.getTranslator();
        for (int s = 0; s < n; s++) {
            Distribution<Function> d = pmdp.getDistribution(s, 0);
            beta[s] = new double[d.getSupport().size()];
            int idx = 0;
            for (int t : d.getSupport()) {
                GRBLinExpr lin = trans.translateLinearExpression(d.get(t).asExpression());
                HashMap<Integer,Double> row = new HashMap<>();
                double betaAdd = lin.getConstant();

                for (int k = 0; k < lin.size(); k++) {
                    int jAll = lin.getVar(k).index();
                    double a = lin.getCoeff(k);
                    int jj = poly.colMap[jAll];
                    if (jj >= 0) { // Free column
                        if (a != 0.0) {
                            row.merge(jj, a, Double::sum);
                            u[s][jj] += a; // Accumulate for u_s
                        }
                    } else { // Fixed column
                        double zFix = poly.fixedVal[jAll];
                        if (!Double.isNaN(zFix) && a != 0.0) betaAdd += a * zFix;
                    }
                }
                alpha.add(row);
                beta[s][idx] = betaAdd;
                v[s] += betaAdd; // Accumulate for v_s
                idx++;
            }
        }

        int[] off = new int[n + 1];
        { int cur = 0;
            for (int s = 0; s < n; s++) { off[s] = cur; cur += pmdp.getDistribution(s, 0).getSupport().size(); }
            off[n] = cur;
        }

        // --- 3) Build the outer one-shot LP ---
        GRBEnv env = cxl.getEnv();
        GRBModel outer = new GRBModel(env);
        outer.set(GRB.IntParam.OutputFlag, 0);

        // For min-cost/min-prob problems, we MAXIMIZE the value variables.
        outer.set(GRB.IntAttr.ModelSense, GRB.MAXIMIZE);

        // (a) State value variables x
        GRBVar[] x = new GRBVar[n];
        for (int s = 0; s < n; s++) {
            if (objType == Objective.REACH_PROB_MIN) {
                double lb = goal.get(s) ? 1.0 : 0.0;
                x[s] = outer.addVar(lb, 1.0, 0.0, GRB.CONTINUOUS, "x_" + s);
            } else { // Expected steps
                x[s] = outer.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "x_" + s);
                if (goal.get(s)) {
                    x[s].set(GRB.DoubleAttr.UB, 0.0);
                }
            }
        }

        // (b) Per-state dual variables: y^(s) >= 0 and beta_s (free)
        final int m = poly.A.length;
        GRBVar[][] y = new GRBVar[n][m];
        GRBVar[] beta_s = new GRBVar[n];
        for (int s = 0; s < n; s++) {
            if (goal.get(s)) continue;
            for (int i = 0; i < m; i++) {
                y[s][i] = outer.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "y_"+s+"_"+i);
            }
            beta_s[s] = outer.addVar(-GRB.INFINITY, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "beta_"+s);
        }
        outer.update();

        // The dual of (min c'θ s.t. Aθ<=b, u'θ=1-v) is (max -b'y + (1-v)β s.t. A'y+uβ=-c)
        // This leads to the following two sets of constraints.

        // (c) Dual Feasibility: A^T*y^(s) + u_s*beta_s + C_s(x) = 0
        for (int s = 0; s < n; s++) {
            if (goal.get(s)) continue;
            Distribution<Function> d = pmdp.getDistribution(s, 0);
            for (int jj = 0; jj < nZ; jj++) {
                GRBLinExpr eq = new GRBLinExpr();
                // A^T*y^(s) term
                for (int i = 0; i < m; i++) {
                    if (poly.A[i][jj] != 0.0) eq.addTerm(poly.A[i][jj], y[s][i]);
                }
                // u_s*beta_s term
                if (u[s][jj] != 0.0) eq.addTerm(u[s][jj], beta_s[s]);
                // C_s(x) term
                int idx = 0;
                for (int t : d.getSupport()) {
                    Double coeff = alpha.get(off[s] + idx++).get(jj);
                    if (coeff != null && coeff != 0.0) eq.addTerm(coeff, x[t]);
                }
                outer.addConstr(eq, GRB.EQUAL, 0.0, "dual_feas_s"+s+"_j"+jj);
            }
        }

        // (d) Value Constraint: x_s <= D_s(x) - b^T*y^(s) + (1-v_s)*beta_s + R
        // Rearranged: x_s - D_s(x) + b^T*y^(s) - (1-v_s)*beta_s - R <= 0
        for (int s = 0; s < n; s++) {
            if (goal.get(s)) continue;
            GRBLinExpr eq = new GRBLinExpr();
            eq.addTerm(1.0, x[s]); // +x_s
            // +b^T*y^(s)
            for (int i = 0; i < m; i++) {
                if (poly.b[i] != 0.0) eq.addTerm(poly.b[i], y[s][i]);
            }
            // -D_s(x)
            Distribution<Function> d = pmdp.getDistribution(s, 0);
            int idx = 0;
            for (int t : d.getSupport()) {
                if (beta[s][idx] != 0.0) eq.addTerm(-beta[s][idx], x[t]);
                idx++;
            }
            // -(1-v_s)*beta_s
            if (1 - v[s] != 0.0) eq.addTerm(-(1 - v[s]), beta_s[s]);
            // -R (cost term)
            if (objType == Objective.EXPECTED_STEPS_MIN) {
                eq.addConstant(-1.0);
            }
            outer.addConstr(eq, GRB.LESS_EQUAL, 0.0, "value_le_s"+s);
        }

        // (e) Objective: Maximize the value at the initial state
        GRBLinExpr obj = new GRBLinExpr();
        obj.addTerm(1.0, x[s0]);
        outer.setObjective(obj, GRB.MAXIMIZE);

        // --- 4) Solve ---
        outer.optimize();
        ConvexLearner.printModel(outer);
        int status = outer.get(GRB.IntAttr.Status);
        if (status != GRB.Status.OPTIMAL) {
            if (status == GRB.Status.INFEASIBLE || status == GRB.Status.INF_OR_UNBD) {
                outer.computeIIS();
                outer.write("model.ilp");
                throw new GRBException("LP is infeasible or unbounded. IIS written to model.ilp. Status=" + status);
            }
            throw new GRBException("LP not optimal; status=" + status);
        }

        double[] xv = new double[n];
        for (int s = 0; s < n; s++) xv[s] = x[s].get(GRB.DoubleAttr.X);
        Result res = new Result(xv, xv[s0], m*n + n, outer.get(GRB.IntAttr.NumConstrs), outer.get(GRB.IntAttr.NumVars));
        outer.dispose();
        return res;
    }

    private static final class PolyFree {
        final double[][] A; final double[] b; final int[] freeCols;
        final int[] colMap; final double[] fixedVal; final int nFree;
        PolyFree(double[][] A, double[] b, int[] freeCols, int[] colMap, double[] fixedVal) {
            this.A = A; this.b = b; this.freeCols = freeCols; this.colMap = colMap; this.fixedVal = fixedVal;
            this.nFree = freeCols.length;
        }
    }

    private static PolyFree extractReducedPolytope(GRBModel model) throws GRBException {
        final double BIG = 1e90;
        GRBVar[] vars = model.getVars();
        int nAll = vars.length;
        double[] lb = new double[nAll], ub = new double[nAll];
        boolean[] fixed = new boolean[nAll];
        double[] fixedVal = new double[nAll];
        int nFree = 0;
        for (int j = 0; j < nAll; j++) {
            if (!vars[j].get(GRB.StringAttr.VarName).startsWith("c_")) { // Exclude constant vars
                lb[j] = vars[j].get(GRB.DoubleAttr.LB);
                ub[j] = vars[j].get(GRB.DoubleAttr.UB);
                boolean isFixed = Math.abs(ub[j] - lb[j]) <= 1e-12;
                fixed[j] = isFixed;
                fixedVal[j] = isFixed ? lb[j] : Double.NaN;
                if (!isFixed) nFree++;
            } else {
                fixed[j] = true; // Treat constants as fixed
                fixedVal[j] = Double.NaN;
            }
        }
        int[] freeCols = new int[nFree];
        int[] colMap = new int[nAll];
        Arrays.fill(colMap, -1);
        for (int j = 0, k = 0; j < nAll; j++) if (!fixed[j] && !vars[j].get(GRB.StringAttr.VarName).startsWith("c_")) { colMap[j] = k; freeCols[k++] = j; }
        ArrayList<double[]> rows = new ArrayList<>();
        ArrayList<Double> rhs = new ArrayList<>();
        for (GRBConstr c : model.getConstrs()) {
            GRBLinExpr row = model.getRow(c);
            char s = c.get(GRB.CharAttr.Sense);
            double r = c.get(GRB.DoubleAttr.RHS);
            double[] aFree = new double[nFree];
            double shift = 0.0;
            for (int k = 0; k < row.size(); k++) {
                GRBVar v = row.getVar(k);
                int jAll = v.index();
                double coef = row.getCoeff(k);
                if (fixed[jAll]) {
                    shift += coef * fixedVal[jAll];
                } else {
                    int jj = colMap[jAll];
                    if (jj != -1) aFree[jj] += coef;
                }
            }
            double bval = r - shift;
            if (s == GRB.LESS_EQUAL) {
                rows.add(aFree); rhs.add(bval);
            } else if (s == GRB.GREATER_EQUAL) {
                for (int j = 0; j < nFree; j++) aFree[j] = -aFree[j];
                rows.add(aFree); rhs.add(-bval);
            } else {
                rows.add(aFree.clone()); rhs.add(bval);
                for (int j = 0; j < nFree; j++) aFree[j] = -aFree[j];
                rows.add(aFree); rhs.add(-bval);
            }
        }
        for (int jAll = 0; jAll < nAll; jAll++) {
            if (fixed[jAll] || vars[jAll].get(GRB.StringAttr.VarName).startsWith("c_")) continue;
            boolean infLB = Double.isInfinite(lb[jAll]) || Math.abs(lb[jAll]) >= BIG;
            boolean infUB = Double.isInfinite(ub[jAll]) || Math.abs(ub[jAll]) >= BIG;
            int jj = colMap[jAll];
            if (jj != -1) {
                if (!infUB) { double[] a = new double[nFree]; a[jj] =  1.0; rows.add(a); rhs.add( ub[jAll]); }
                if (!infLB) { double[] a = new double[nFree]; a[jj] = -1.0; rows.add(a); rhs.add(-lb[jAll]); }
            }
        }
        double[][] A = rows.toArray(new double[0][]);
        double[] b = new double[rhs.size()];
        for (int i = 0; i < b.length; i++) b[i] = rhs.get(i);
        return new PolyFree(A, b, freeCols, colMap, fixedVal);
    }
}