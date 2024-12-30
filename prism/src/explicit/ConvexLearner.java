package explicit;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import param.Function;
import param.FunctionFactory;
import prism.*;
import common.Interval;

import com.gurobi.gurobi.*;

import java.util.Arrays;
import java.util.BitSet;

public class ConvexLearner {

    private final GRBEnv env;
    private GRBModel model;

    private MDPSimple<Function> mdpParam;

    private final ExpressionTranslator trans;

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
        dist.add(2, onemp);
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
        udist.add(2, new Interval<>(0.2,0.5));
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

        System.out.println("MDP: " + mdp);
        System.out.println("IMDP: " + umdp);


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

        GRBLinExpr obj = cxl.trans.translateLinearExpression(fact.getVar("p").asExpression());
        cxl.model.setObjective(obj, GRB.MAXIMIZE);
        cxl.model.optimize();
        System.out.println("Obj: " + cxl.model.get(GRB.DoubleAttr.ObjVal));

        obj = cxl.trans.translateLinearExpression(fact.getVar("q").asExpression());
        cxl.model.setObjective(obj, GRB.MAXIMIZE);
        cxl.model.optimize();
        System.out.println("Obj: " + cxl.model.get(GRB.DoubleAttr.ObjVal));

        for (GRBVar var : cxl.model.getVars()) {
            System.out.println(var.get(GRB.StringAttr.VarName) + " LB: " + var.get(GRB.DoubleAttr.LB) + " UB: "  + var.get(GRB.DoubleAttr.UB));
        }

        UMDPSimple<Double> convex_mdp = cxl.getUMDP();
        System.out.println("UMDP: " + convex_mdp);

        UMDPModelChecker mc = new UMDPModelChecker(null);
        mc.setPrecomp(true);

        BitSet target = new BitSet();
        target.set(1);
        //target.set(5);
        ModelCheckerResult res;
        //umdp.findDeadlocks(true);
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
    }

    public void setParamModel(MDPSimple<Function> mdpParam) {
        this.mdpParam = mdpParam;
    }

    public void setConstraints(UMDP<Double> imdp) throws PrismException, GRBException {
        // Iterate over IMDP
        for (int s = 0; s < imdp.getNumStates(); s++) {
            for (int a = 0; a < imdp.getNumChoices(s); a++) {
                UDistribution<Double> udist = imdp.getUncertainDistribution(s,a);
                if (udist instanceof UDistributionIntervals<Double> dist) {
                    Distribution<Interval<Double>> idist = dist.getIntervals();
                    Distribution<Function> pdist = mdpParam.getDistribution(s, a);

                    for (int i : idist.getSupport()) {
                        GRBLinExpr exp = trans.translateLinearExpression(pdist.get(i).asExpression());
                        model.addConstr(exp, GRB.GREATER_EQUAL, idist.get(i).getLower(), null);
                        model.addConstr(exp, GRB.LESS_EQUAL, idist.get(i).getUpper(), null);
                    }
                } else {
                    throw new PrismException("Only Interval MDPs supported.");
                }
            }
        }
    }

    public UMDPSimple<Double> getUMDP() {
        UMDPSimple<Double> convexUMDP = new UMDPSimple<>(mdpParam.getNumStates());
        for (int s = 0; s < mdpParam.getNumStates(); s++) {
            for (int a = 0; a < mdpParam.getNumChoices(s); a++) {
                Distribution<Function> pdist = mdpParam.getDistribution(s, a);
                Object action = mdpParam.getAction(s,a);

                UDistributionLinearProgram<Double> convex_dist = new UDistributionLinearProgram<>(pdist, this.model, this.trans);
                convexUMDP.addActionLabelledChoice(s, convex_dist, action);
            }
        }
        return convexUMDP;
    }
}
