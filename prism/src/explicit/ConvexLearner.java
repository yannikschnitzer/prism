package explicit;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import param.Function;
import param.FunctionFactory;
import parser.State;
import prism.*;
import common.Interval;

public class ConvexLearner {

    private Prism prism;

    public static void main(String[] args) throws PrismException {

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


        System.out.println("MDP: " + mdp);
        System.out.println("IMDP: " + umdp);

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        ExpressionTranslator trans = new ExpressionTranslator(model);
        Expression exp = trans.translateLinearExpression(onemp.asExpression());

        System.out.println("Expression: " + onemp.asExpression());

        exp.upper(0.2).lower(0.1);
        System.out.println("Translated: "+ExpressionTranslator.getConstraintEquation(exp, model.getVariables()));
        System.out.println("Model: " + model);


    }

}
