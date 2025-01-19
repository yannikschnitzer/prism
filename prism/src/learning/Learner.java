package learning;

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBConstr;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBException;
import common.Interval;
import explicit.*;
import learning.Estimators.Estimator;
import learning.Estimators.EstimatorConstructor;
import learning.Estimators.PACIntervalEstimatorOptimistic;
import param.Function;
import param.FunctionFactory;
import parser.Values;
import parser.ast.ModulesFile;
import prism.*;
import strat.Strategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Learner {

    Prism prism;

    private final boolean verbose = true;

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
        Experiment ex = new Experiment(Experiment.Model.CHAIN_CONVEX).config(30, 1_000_000, 1, false, false, 1, 1, 4);
        MDP<Function> pmdp = learner.buildParamModel(ex);
        System.out.println(pmdp);

        Values values = new Values();
        values.addValue("p", 0.15);
        values.addValue("q", 0.1);

        learner.learnIMDP("test", ex, PACIntervalEstimatorOptimistic::new, pmdp, values, true);
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

            // Get parametric model
            String[] paramNames = new String[]{"p","q"};
            String[] paramLowerBounds = new String[]{"0","0"};
            String[] paramUpperBounds = new String[]{"1","1"};
            this.prism.setPRISMModelConstants(new Values(), true);
            this.prism.setParametric(paramNames, paramLowerBounds, paramUpperBounds);
            this.prism.buildModel();
            MDP<Function> model = (MDP<Function>) this.prism.getBuiltModelExplicit();

            return model;

        } catch (PrismException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void resetAll(int seed) {
        try {
            initializePrism();
            this.prism.setSimulatorSeed(seed);
        } catch (PrismException e) {
            System.out.println("PrismException in resetAll(): " + e.getMessage());
            System.exit(1);
        }
    }

    public Pair<List<List<IMDP<Double>>>, List<MDP<Double>>> learnIMDP(String label, Experiment ex, EstimatorConstructor estimatorConstructor, MDP<Function> mdpParam, Values parameterValuation, boolean verification) {
        resetAll(ex.seed);

        System.out.println("\n\n\n\n%------\n%Learning IMDP\n%  Model: " + ex.model + "\n%  max_episode_length: "
                + ex.max_episode_length + "\n%  iterations: " + ex.iterations + "\n%------");
        if (verbose)
            System.out.printf("%s, seed %d\n", label, ex.seed);

        try {
            ModulesFile modulesFile = prism.parseModelFile(new File(ex.modelFile));
            prism.loadPRISMModel(modulesFile);

            ex.values = parameterValuation;
            Estimator estimator = estimatorConstructor.get(this.prism, ex);
            System.out.println("Constant Values:" + estimator.getSUL().getConstantValues());
            estimator.set_experiment(ex);

            // Iterate and run experiments for each of the sampled parameter vectors
            //ex.setTieParamters(verification);
            Pair<ArrayList<DataPoint>, ArrayList<IMDP<Double>>> resIMDP = runSampling(ex, estimator, verification);

        } catch (PrismException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public Pair<ArrayList<DataPoint>, ArrayList<IMDP<Double>>> runSampling(Experiment ex, Estimator estimator, boolean verifcation) {
        return runSampling(ex, estimator, 0, verifcation);
    }

    public Pair<ArrayList<DataPoint>, ArrayList<IMDP<Double>>> runSampling(Experiment ex, Estimator estimator, int past_iterations, boolean verficiation) {
        try {
            MDP<Double> SUL = estimator.getSUL();

            if (true/*this.modelStats == null*/) {
                //this.modelStats = estimator.getModelStats();
                System.out.println("======");
                System.out.println(ex.model);
                System.out.println("======");
            }

            ObservationSampler observationSampler = new ObservationSampler(this.prism, SUL, estimator.getTerminatingStates());
            observationSampler.setTransitionsOfInterest(estimator.getTransitionsOfInterest());
            observationSampler.setTiedParameters(ex.tieParameters);
            observationSampler.setMultiplier(ex.multiplier);

            double[] currentResults = estimator.getInitialResults();

            ArrayList<DataPoint> results = new ArrayList<>();
            ArrayList<IMDP<Double>> estimates = new ArrayList<>();
            if (past_iterations == 0) {
                results.add(new DataPoint(0, past_iterations, currentResults));
                estimates.add(estimator.getEstimate());
            }
            int samples = 0;
            Strategy samplingStrategy = estimator.buildStrategy();
            for (int i = past_iterations; i < ex.iterations + past_iterations; i++) {
                int sampled = observationSampler.simulateEpisode(ex.max_episode_length, samplingStrategy);
                samples += sampled;

                boolean last_iteration = i == ex.iterations + past_iterations - 1;
                if (observationSampler.collectedEnoughSamples() || last_iteration || ex.resultIteration(i)) {
                    estimator.setObservationMaps(observationSampler.getSamplesMap(), observationSampler.getSampleSizeMap());
                    samplingStrategy = estimator.buildStrategy();
                    currentResults = estimator.getCurrentResults();

                    if (!ex.tieParameters || (!verficiation && ex.isBayesian())) {
                        observationSampler.resetObservationSequence();
                    } else {
                        observationSampler.incrementAccumulatedSamples();
                    }

                    if (this.verbose) System.out.println("Episode " + i + ".");
                    if (this.verbose) System.out.println("Performance on MDPs (J): " + currentResults[1]);
                    if (this.verbose) System.out.println("Performance Guarantee on IMDPs (J̃): " + currentResults[0]);
                    if (this.verbose) System.out.println();

                    if (last_iteration || ex.resultIteration(i)) {
                        results.add(new DataPoint(samples, i + 1, currentResults));
                        estimates.add(estimator.getEstimate());
                    }
                }
            }

            return new Pair(results, estimates);
        } catch (PrismException e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
        prism.closeDown();
        return null;
    }
}
