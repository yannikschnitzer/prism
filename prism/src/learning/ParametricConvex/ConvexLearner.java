package learning.ParametricConvex;

import com.gurobi.gurobi.*;
import common.Interval;
import explicit.*;
import param.Function;
import param.FunctionFactory;
import prism.Evaluator;
import prism.Pair;
import prism.PrismException;
import prism.PrismSettings;

import java.util.BitSet;
import java.util.HashMap;

public class ConvexLearner {

    private final GRBEnv env;
    private GRBModel model;

    private MDPSimple<Function> mdpParam;

    private ExpressionTranslator trans;

    private HashMap<String, Pair<GRBLinExpr, Double>> constrLowerBounds = new HashMap<>();
    private HashMap<String, Pair<GRBLinExpr, Double>> constrUpperBounds = new HashMap<>();

    // add at top with other fields:
    private SharedVertexSet sharedVertices;     // null if not precomputed or cap exceeded
    private int vertexCap = 10_000;            // default cap; set via setter if you want

    public void setVertexCap(int cap) { this.vertexCap = cap; }

    public static void main(String[] args) throws PrismException, GRBException {

        PrismSettings settings = new PrismSettings();
        FunctionFactory fact = FunctionFactory.create(new String[]{"p","q"}, new String[]{"0","0"}, new String[]{"1","1"}, settings);

        //Function onemp = fact.getOne().multiply(1).subtract((fact.getVar("p").add(fact.getVar("q")).multiply(3)));
        Function onemp = fact.getOne().subtract(fact.getVar("p"));
        Function onemq = fact.getOne().subtract(fact.getVar("q"));

        // Param MDP
        MDPSimple<Function> mdp = new MDPSimple<>();
        mdp.addStates(5);

        Distribution<Function> dist = new Distribution<>(Evaluator.forRationalFunction(fact));
        dist.add(1, fact.getVar("p"));
        dist.add(4, onemp);
        mdp.addActionLabelledChoice(0,dist,"a");

        dist = new Distribution<>(Evaluator.forRationalFunction(fact));
        dist.add(1, fact.getVar("q"));
        dist.add(2, onemq);
        mdp.addActionLabelledChoice(3,dist,"b");

        dist = new Distribution<>(Evaluator.forRationalFunction(fact));
        dist.add(1, fact.getVar("p").subtract(fact.getVar("q")));
        dist.add(2, onemp.add(fact.getVar("q")));
        mdp.addActionLabelledChoice(4,dist,"a");

        dist = new Distribution<>(Evaluator.forRationalFunction(fact));
        dist.add(1, fact.getOne());
        mdp.addActionLabelledChoice(1, dist,"b");

        dist = new Distribution<>(Evaluator.forRationalFunction(fact));
        dist.add(2, fact.getOne());
        mdp.addActionLabelledChoice(2, dist,"b");

        // IMDP
        UMDPSimple<Double> umdp = new UMDPSimple<>();
        umdp.addStates(5);

        Distribution<Interval<Double>> udist = new Distribution<>(Evaluator.forDoubleInterval());
        udist.add(1, new Interval<>(0.5,0.9));
        udist.add(4, new Interval<>(0.2,0.5));
        umdp.addActionLabelledChoice(0, new UDistributionIntervals<>(udist), "a");

        udist = new Distribution<>(Evaluator.forDoubleInterval());
        udist.add(1, new Interval<>(1.0,1.0));
        umdp.addActionLabelledChoice(1, new UDistributionIntervals<>(udist), "b");

        udist = new Distribution<>(Evaluator.forDoubleInterval());
        udist.add(2, new Interval<>(1.0,1.0));
        umdp.addActionLabelledChoice(2, new UDistributionIntervals<>(udist), "b");

        udist = new Distribution<>(Evaluator.forDoubleInterval());

        udist.add(1, new Interval<>(0.2,0.5));
        udist.add(2, new Interval<>(0.5,0.9));
        umdp.addActionLabelledChoice(3, new UDistributionIntervals<>(udist), "a");

        udist = new Distribution<>(Evaluator.forDoubleInterval());
        udist.add(1, new Interval<>(0.0,0.25));
        udist.add(2, new Interval<>(0.7,1.0));
        umdp.addActionLabelledChoice(4, new UDistributionIntervals<>(udist), "a");

//        System.out.println("MDP: " + mdp);
//        System.out.println("IMDP: " + umdp);

        GRBEnv env = new GRBEnv(true);
        env.set(GRB.IntParam.OutputFlag, 0);
        env.start();

        ConvexLearner cxl = new ConvexLearner(env);
        cxl.setParamModel(mdp);
        cxl.setConstraints(umdp);
        cxl.model.update();

        for (GRBConstr con : cxl.model.getConstrs()) {
            System.out.println(ExpressionTranslator.formatGBRConstraint(cxl.model,con));
        }

        UMDPSimple<Double> convex_mdp = cxl.getUMDP();

        UMDPModelChecker mc = new UMDPModelChecker(null);
        mc.setPrecomp(true);

        BitSet target = new BitSet();
        target.set(1);
        //target.set(5);
        ModelCheckerResult res;
        //convex_mdp.findDeadlocks(true);
        res = mc.computeReachProbs(convex_mdp, target, MinMax.max().setMinUnc(false));
        System.out.println("maxmax: " + res.soln[0]);


    }

    public ConvexLearner(GRBEnv env) {
        try {
            this.env = env;
            this.model = new GRBModel(env);
            this.trans = new ExpressionTranslator(model);
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    public GRBModel getModel() {
        return model;
    }

    public void setModel(GRBModel model) {
        this.model = model;
    }

    public MDP<Function> getParamModel() {
        return mdpParam;
    }

    public void resetModel() throws GRBException {
        this.model = new GRBModel(env);
        this.trans = new ExpressionTranslator(model);
    }

    public void setParamModel(MDPSimple<Function> mdpParam) {
        this.mdpParam = mdpParam;
    }

    public void setConstraints(UMDP<Double> imdp) throws PrismException, GRBException {
        // Iterate over IMDP
        this.resetModel();
        for (int s = 0; s < imdp.getNumStates(); s++) {
            for (int a = 0; a < imdp.getNumChoices(s); a++) {
                UDistribution<Double> udist = imdp.getUncertainDistribution(s,a);
                if (udist instanceof UDistributionIntervals<Double> dist) {
                    Distribution<Interval<Double>> idist = dist.getIntervals();
                    Distribution<Function> pdist = mdpParam.getDistribution(s, a);

                    for (int i : idist.getSupport()) {
                        GRBLinExpr exp = trans.translateLinearExpression(pdist.get(i).asExpression());
                        //model.addRange(exp, idist.get(i).getLower(), idist.get(i).getUpper(), null);

//                        model.addConstr(exp, GRB.GREATER_EQUAL, idist.get(i).getLower(), null);
//                        model.addConstr(exp, GRB.LESS_EQUAL, idist.get(i).getUpper(), null);
                        model.update();
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
                } else {
                    throw new PrismException("Only Interval MDPs supported.");
                }
            }
        }

        for (String expString : constrLowerBounds.keySet()) {
            GRBLinExpr exp = constrLowerBounds.get(expString).first;

            model.addConstr(exp, GRB.GREATER_EQUAL, constrLowerBounds.get(expString).second, null);
            model.addConstr(exp, GRB.LESS_EQUAL, constrUpperBounds.get(expString).second, null);
        }
    }

    public void setConstraints(MDPSimple<Function> pmdp,UMDP<Double> imdp) throws PrismException, GRBException {
        // Iterate over IMDP
        for (int s = 0; s < imdp.getNumStates(); s++) {
            for (int a = 0; a < imdp.getNumChoices(s); a++) {
                UDistribution<Double> udist = imdp.getUncertainDistribution(s,a);
                if (udist instanceof UDistributionIntervals<Double> dist) {
                    Distribution<Interval<Double>> idist = dist.getIntervals();
                    Distribution<Function> pdist = pmdp.getDistribution(s, a);

                    for (int i : idist.getSupport()) {
                        GRBLinExpr exp = trans.translateLinearExpression(pdist.get(i).asExpression());
                        //model.addRange(exp, idist.get(i).getLower(), idist.get(i).getUpper(), null);

//                        model.addConstr(exp, GRB.GREATER_EQUAL, idist.get(i).getLower(), null);
//                        model.addConstr(exp, GRB.LESS_EQUAL, idist.get(i).getUpper(), null);
                        model.update();
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
                } else {
                    throw new PrismException("Only Interval MDPs supported.");
                }
            }
        }

        for (String expString : constrLowerBounds.keySet()) {
            GRBLinExpr exp = constrLowerBounds.get(expString).first;

            model.addConstr(exp, GRB.GREATER_EQUAL, constrLowerBounds.get(expString).second, null);
            model.addConstr(exp, GRB.LESS_EQUAL, constrUpperBounds.get(expString).second, null);
        }
    }

    // Call this after you have added all constraints (i.e., after setConstraints + model.update)
    public void precomputeVertices() throws GRBException {
        model.update(); // ensure model is finalized
        System.out.println("Enumerating Vertices");
        SharedVertexSet sv = SharedVertexSet.fromModel(model, 1e-9, vertexCap);
        if (sv.complete) {
            System.out.println("Precomputed vertices: " + sv.vertexCount);
            this.sharedVertices = sv;
        } else {
            System.out.println("Vertex enumeration exceeded cap (" + vertexCap + "); using LP mode.");
            this.sharedVertices = null; // fall back to LP in the distributions
        }
    }

    public UMDPSimple<Double> getUMDP() {
        UMDPSimple<Double> convexUMDP = new UMDPSimple<>(mdpParam.getNumStates());
        for (int s = 0; s < mdpParam.getNumStates(); s++) {
            for (int a = 0; a < mdpParam.getNumChoices(s); a++) {
                Distribution<Function> pdist = mdpParam.getDistribution(s, a);
                Object action = mdpParam.getAction(s,a);

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
            System.out.println(ExpressionTranslator.formatGBRConstraint(model,con));
        }
        System.out.println("-------------");

    }
}
