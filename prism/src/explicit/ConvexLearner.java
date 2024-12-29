package explicit;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import param.Function;
import param.FunctionFactory;
import prism.*;

public class ConvexLearner {

    private Prism prism;

    public static void main(String[] args) throws PrismException {
        UMDPModelChecker umdp = new UMDPModelChecker(null);

        MDPSimple<Function> mdp = new MDPSimple<>();
        mdp.addStates(5);

        PrismSettings settings = new PrismSettings();
        FunctionFactory fact = FunctionFactory.create(new String[]{"p","q"}, new String[]{"0","0"}, new String[]{"1","1"}, settings);

        Function onemp = fact.getOne().subtract((fact.getVar("p").add(fact.getVar("q")).multiply(3)));
        Function onemq = fact.getOne().multiply(-1).subtract(fact.getVar("q"));

        System.out.println("Expression: " + onemp.asExpression());

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Expression exp = model.addExpression("Constraint 1");

        ExpressionTranslator trans = new ExpressionTranslator(model);

        trans.translateLinearExpression(onemp.asExpression(), exp);

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

        System.out.println("MDP: " + mdp);



    }

}
