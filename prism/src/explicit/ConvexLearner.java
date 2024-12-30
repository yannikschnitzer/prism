package explicit;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import param.Function;
import param.FunctionFactory;
import prism.*;
import common.Interval;

import com.gurobi.gurobi.*;

public class ConvexLearner {

    private ExpressionsBasedModel model;

    private MDPSimple<Function> mdpParam;

    private ExpressionTranslator trans;

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
        udist.add(1, new Interval<>(0.0,0.2));
        udist.add(2, new Interval<>(0.8,1.0));
        umdp.addActionLabelledChoice(4, new UDistributionIntervals<>(udist), "a");

        System.out.println("MDP: " + mdp);
        System.out.println("IMDP: " + umdp);

        String username = System.getProperty("user.name");
        System.out.println("User: " + username);

        GRBEnv env = new GRBEnv(true);
        env.set(GRB.IntParam.OutputFlag, 0);
        env.start();

        GRBModel grbmodel = new GRBModel(env);
        GRBVar x = grbmodel.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x");
        GRBVar y = grbmodel.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y");
        GRBVar z = grbmodel.addVar(0.0, 1.0, 0.0, GRB.BINARY, "z");
        GRBLinExpr gexpr = new GRBLinExpr();
        gexpr.addTerm(1.0, x);
        gexpr.addTerm(1.0, y);
        gexpr.addTerm(2.0, z);
        grbmodel.setObjective(gexpr, GRB.MAXIMIZE);
        grbmodel.optimize();
        System.out.println("Obj: " + grbmodel.get(GRB.DoubleAttr.ObjVal));

        GRBLinExpr gexpr2 = new GRBLinExpr();
        gexpr2.addTerm(2.0, x);
        gexpr2.addTerm(2.0, y);
        gexpr2.addTerm(2.0, z);
        grbmodel.setObjective(gexpr2, GRB.MAXIMIZE);
        grbmodel.optimize();
        System.out.println("Obj: " + grbmodel.get(GRB.DoubleAttr.ObjVal));


        ExpressionsBasedModel model = new ExpressionsBasedModel();

        ConvexLearner cxl = new ConvexLearner(model);
        cxl.setParamModel(mdp);
        cxl.setConstraints(umdp);

        for (Expression expr : model.getExpressions()) {
            System.out.println(ExpressionTranslator.getConstraintEquation(expr, model.getVariables()));
        }

        Expression obj = model.addExpression("Objective").add(model.getVariable(0),1.0).weight(1.0);
        System.out.println("Model: " + model);
        Optimisation.Result res = model.maximise();
        System.out.println("Res:" + res);

    }

    public ConvexLearner(ExpressionsBasedModel model) {
        this.model = model;
        this.trans = new ExpressionTranslator(model);
    }

    public ExpressionsBasedModel getModel() {
        return model;
    }

    public void setModel(ExpressionsBasedModel model) {
        this.model = model;
    }

    public MDP<Function> getParamModel() {
        return mdpParam;
    }

    public void setParamModel(MDPSimple<Function> mdpParam) {
        this.mdpParam = mdpParam;
    }

    public void setConstraints(UMDP<Double> imdp) throws PrismException {
        // Iterate over IMDP
        for (int s = 0; s < imdp.getNumStates(); s++) {
            for (int a = 0; a < imdp.getNumChoices(s); a++) {
                UDistribution<Double> udist = imdp.getUncertainDistribution(s,a);
                if (udist instanceof UDistributionIntervals<Double> dist) {
                    Distribution<Interval<Double>> idist = dist.getIntervals();
                    Distribution<Function> pdist = mdpParam.getDistribution(s, a);

                    for (int i : idist.getSupport()) {
                        Expression exp = trans.translateLinearExpression(pdist.get(i).asExpression());
                        exp.lower(idist.get(i).getLower());
                        exp.upper(idist.get(i).getUpper());
                    }
                } else {
                    throw new PrismException("Only Interval MDPs supported.");
                }
            }
        }

    }
}
