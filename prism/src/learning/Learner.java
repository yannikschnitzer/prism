package learning;

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBConstr;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBException;
import common.Interval;
import explicit.*;
import param.Function;
import param.FunctionFactory;
import parser.Values;
import parser.ast.ModulesFile;
import prism.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.BitSet;

public class Learner {

    Prism prism;

    public Learner(Prism prism) {
        this.prism = prism;
    }

    public static void main(String[] args) throws GRBException, PrismException {
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

        System.out.println("MDP: " + mdp);
        System.out.println("IMDP: " + umdp);

        GRBEnv env = new GRBEnv(true);
        env.set(GRB.IntParam.OutputFlag, 0);
        env.start();

        ConvexLearner cxl = new ConvexLearner(env);
        cxl.setParamModel(mdp);
        cxl.setConstraints(umdp);
        cxl.getModel().update();

        for (GRBConstr con : cxl.getModel().getConstrs()) {
            System.out.println(ExpressionTranslator.formatGBRConstraint(cxl.getModel(),con));
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


        Learner learner = new Learner(new Prism(new PrismDevNullLog()));
        learner.initializePrism();
        MDP<Function> pmdp = learner.buildParamModel(new Experiment(Experiment.Model.CHAIN2).config(30, 1_000_000, 1, true, true, 1, 1, 4));
        System.out.println(pmdp);
    }

    @SuppressWarnings("unchecked")
    public void initializePrism() throws PrismException {
        this.prism = new Prism(new PrismDevNullLog());
        this.prism.initialise();
        this.prism.setEngine(Prism.EXPLICIT);
        this.prism.setGenStrat(true);
    }

    public MDP<Function> buildParamModel(Experiment ex) {
        try {
            ModulesFile modulesFile = this.prism.parseModelFile(new File(ex.modelFile));
            prism.loadPRISMModel(modulesFile);

            // Temporarily get parametric model
            /*
             * SAV2: pL, pH
             * Aircraft: r, p
             * Drone Single: p
             * Betting Game: p
             * Chain Large: p, q
             */
            String[] paramNames = new String[]{"p"};
            String[] paramLowerBounds = new String[]{"0"};
            String[] paramUpperBounds = new String[]{"1"};
//            this.prism.setPRISMModelConstants(new Values(), true);
//            this.prism.setParametric(paramNames, paramLowerBounds, paramUpperBounds);
            this.prism.buildModel();
            MDP<Function> model = (MDP<Function>) this.prism.getBuiltModelExplicit();
//            System.out.println("Model states values" + model.getStatesList().getFirst());
//            System.out.println("Action 0:" + model.getAction(0,0));
//            System.out.println("Action 1:" + model.getAction(0,1));
//            System.out.println("Action 2:" + model.getAction(0,2));

            return model;

        } catch (PrismException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
