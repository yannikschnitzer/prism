package learning.ParametricConvex;

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBConstr;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBException;
import common.Interval;
import explicit.*;
import learning.Data.DataPoint;
import learning.Data.DataProcessor;
import learning.Estimators.*;
import learning.ParameterTyer;
import learning.Simulation.ObservationSampler;
import learning.Simulation.TransitionTriple;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import param.Function;
import param.FunctionFactory;
import parser.Values;
import parser.ast.Expression;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.*;
import strat.Strategy;
import imdpcomp.Experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static imdpcomp.Experiment.ParameterTying.NO_TYING;

public class ParametricConvexLearner {

    Prism prism;

    private final boolean verbose = true;

    public ParametricConvexLearner(Prism prism) {
        this.prism = prism;
    }

//    public static void main_old(String[] args) throws GRBException, PrismException {
//        PrismSettings settings = new PrismSettings();
//        FunctionFactory fact = FunctionFactory.create(new String[]{"p","q"}, new String[]{"0","0"}, new String[]{"1","1"}, settings);
//
//        //Function onemp = fact.getOne().multiply(1).subtract((fact.getVar("p").add(fact.getVar("q")).multiply(3)));
//        Function onemp = fact.getOne().subtract(fact.getVar("p"));
//        Function onemq = fact.getOne().subtract(fact.getVar("q"));
//
//        // Param MDP
//        MDPSimple<Function> mdp = new MDPSimple<>();
//        mdp.addStates(5);
//
//        Distribution<Function> dist = new Distribution<>(Evaluator.forRationalFunction(fact));
//        dist.add(1, fact.getVar("p"));
//        dist.add(4, onemp);
//        mdp.addActionLabelledChoice(0,dist,"a");
//
//        dist = new Distribution<>(Evaluator.forRationalFunction(fact));
//        dist.add(1, fact.getVar("q"));
//        dist.add(2, onemq);
//        mdp.addActionLabelledChoice(3,dist,"b");
//
//        dist = new Distribution<>(Evaluator.forRationalFunction(fact));
//        dist.add(1, fact.getVar("p").subtract(fact.getVar("q")));
//        dist.add(2, onemp.add(fact.getVar("q")));
//        mdp.addActionLabelledChoice(4,dist,"a");
//
//        dist = new Distribution<>(Evaluator.forRationalFunction(fact));
//        dist.add(1, fact.getOne());
//        mdp.addActionLabelledChoice(1, dist,"b");
//
//        dist = new Distribution<>(Evaluator.forRationalFunction(fact));
//        dist.add(2, fact.getOne());
//        mdp.addActionLabelledChoice(2, dist,"b");
//
//        // IMDP
//        UMDPSimple<Double> umdp = new UMDPSimple<>();
//        umdp.addStates(5);
//
//        Distribution<Interval<Double>> udist = new Distribution<>(Evaluator.forDoubleInterval());
//        udist.add(1, new Interval<>(0.5,0.9));
//        udist.add(4, new Interval<>(0.2,0.5));
//        umdp.addActionLabelledChoice(0, new UDistributionIntervals<>(udist), "a");
//
//        udist = new Distribution<>(Evaluator.forDoubleInterval());
//        udist.add(1, new Interval<>(1.0,1.0));
//        umdp.addActionLabelledChoice(1, new UDistributionIntervals<>(udist), "b");
//
//        udist = new Distribution<>(Evaluator.forDoubleInterval());
//        udist.add(2, new Interval<>(1.0,1.0));
//        umdp.addActionLabelledChoice(2, new UDistributionIntervals<>(udist), "b");
//
//        udist = new Distribution<>(Evaluator.forDoubleInterval());
//
//        udist.add(1, new Interval<>(0.2,0.5));
//        udist.add(2, new Interval<>(0.5,0.9));
//        umdp.addActionLabelledChoice(3, new UDistributionIntervals<>(udist), "a");
//
//        udist = new Distribution<>(Evaluator.forDoubleInterval());
//        udist.add(1, new Interval<>(0.0,0.25));
//        udist.add(2, new Interval<>(0.7,1.0));
//        umdp.addActionLabelledChoice(4, new UDistributionIntervals<>(udist), "a");
//
//        System.out.println("MDP: " + mdp);
//        System.out.println("IMDP: " + umdp);
//
//        GRBEnv env = new GRBEnv(true);
//        env.set(GRB.IntParam.OutputFlag, 0);
//        env.start();
//
//        ConvexLearner cxl = new ConvexLearner(env);
//        cxl.setParamModel(mdp);
//        cxl.setConstraints(umdp);
//        cxl.getModel().update();
//
//        for (GRBConstr con : cxl.getModel().getConstrs()) {
//            System.out.println(ExpressionTranslator.formatGBRConstraint(cxl.getModel(),con));
//        }
//
//        UMDPSimple<Double> convex_mdp = cxl.getUMDP();
//
//        UMDPModelChecker mc = new UMDPModelChecker(null);
//        mc.setPrecomp(true);
//
//        BitSet target = new BitSet();
//        target.set(1);
//        //target.set(5);
//        ModelCheckerResult res;
//        //convex_mdp.findDeadlocks(true);
//        res = mc.computeReachProbs(convex_mdp, target, MinMax.max().setMinUnc(false));
//        System.out.println("maxmax: " + res.soln[0]);
//
//
//        ParametricConvexLearner parametricConvexLearner = new ParametricConvexLearner(new Prism(new PrismDevNullLog()));
//        parametricConvexLearner.initializePrism();
//        Experiment ex = new Experiment(Experiment.Model.BETTING_GAME_CONVEX_ADAPTIVE);
//        MDPSimple<Function> pmdp = parametricConvexLearner.buildParamModel(ex);
//        System.out.println(pmdp);
//
//        parametricConvexLearner.learnIMDP(ex, PACConvexEstimatorOptimistic::new, pmdp, ex.parameterValues, true);
//    }

    public static void main(String[] args) throws GRBException, PrismException {
        ParametricConvexLearner parametricConvexLearner = new ParametricConvexLearner(new Prism(new PrismDevNullLog()));
        parametricConvexLearner.initializePrism();

        Experiment ex = new Experiment(Experiment.Model.ROUTING_BISIM).setParametricConvex(false);

        MDPSimple<Function> pmdp = parametricConvexLearner.buildParamModel(ex);
        System.out.println(pmdp);

        parametricConvexLearner.learnIMDP(ex,
                ex.useParametricConvex ? PACConvexEstimatorOptimistic::new : PACIntervalEstimatorOptimistic::new,
                pmdp,
                ex.parameterValues,
                true);
    }

    @SuppressWarnings("unchecked")
    public void initializePrism() throws PrismException {
        this.prism = new Prism(new PrismDevNullLog());
        this.prism.initialise();
        this.prism.setEngine(Prism.EXPLICIT);
        this.prism.setGenStrat(true);
    }

    public MDPSimple<Function> buildParamModel(Experiment experiment) {
        try {
            ModulesFile modulesFile = this.prism.parseModelFile(new File(experiment.certainModelFile));
            prism.loadPRISMModel(modulesFile);

            List<String> namesList = experiment.parameterValues.getNames();
            String[] paramNames = namesList.toArray(new String[0]);

            int n = paramNames.length;
            String[] paramLowerBounds = new String[n];
            String[] paramUpperBounds = new String[n];
            Arrays.fill(paramLowerBounds, "0");
            Arrays.fill(paramUpperBounds, "1");

            this.prism.setPRISMModelConstants(new Values(), true);
            this.prism.setParametric(paramNames, paramLowerBounds, paramUpperBounds);
            this.prism.buildModel();

            return (MDPSimple<Function>) this.prism.getBuiltModelExplicit();

        } catch (PrismException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Triple<MDPSimple<Function>,int[], Expression> constructParamBisimulation(MDPSimple<Function> pmdp, Experiment experiment) throws PrismException {
        // Parse specs to bisimulation specs
        PropertiesFile pf;
        Expression expr;
        ArrayList<String> propNames = new ArrayList<>();
        ArrayList<BitSet> propBSs = new ArrayList<>();
        StateModelChecker mc = new StateModelChecker(null);
        Expression exprNew;

        pf = prism.parsePropertiesString(experiment.dtmcSpec);
        expr = pf.getProperty(0);
        exprNew = mc.checkMaximalPropositionalFormulas(pmdp, expr.deepCopy(), propNames, propBSs);
        experiment.dtmcSpec_bisim = exprNew;

        pf = prism.parsePropertiesString(experiment.spec);
        expr = pf.getProperty(0);
        exprNew = mc.checkMaximalPropositionalFormulas(pmdp, expr.deepCopy(), propNames, propBSs);
        experiment.spec_bism = exprNew;

        pf = prism.parsePropertiesString(experiment.robustSpec);
        expr = pf.getProperty(0);
        exprNew = mc.checkMaximalPropositionalFormulas(pmdp, expr.deepCopy(), propNames, propBSs);
        experiment.robustSpec_bisim = exprNew;

        pf = prism.parsePropertiesString(experiment.optimisticSpec);
        expr = pf.getProperty(0);
        exprNew = mc.checkMaximalPropositionalFormulas(pmdp, expr.deepCopy(), propNames, propBSs);
        experiment.optimisticSpec_bisim = exprNew;


        Bisimulation<Function> bisim = new Bisimulation<>(mc);
        MDPSimple<Function> pmdpBisim = (MDPSimple<Function>) bisim.minimise(pmdp, propNames, propBSs);

        return new ImmutableTriple<>(pmdpBisim, bisim.getPartition(), exprNew);
    }

    // Resets the PRISM engine and sets the simulator seed
    public void resetAll(int seed) {
        try {
            initializePrism();
            this.prism.setSimulatorSeed(seed);
        } catch (PrismException e) {
            System.out.println("PrismException in resetAll(): " + e.getMessage());
            System.exit(1);
        }
    }

    public Pair<List<List<IMDP<Double>>>, List<MDP<Double>>> learnIMDP(Experiment ex, EstimatorConstructor estimatorConstructor, MDPSimple<Function> pmdp, Values parameterValuation, boolean verification) {
        resetAll(ex.seed);

        System.out.println("\n\n\n\n%------\n%  Learning UMDP\n%  Model: " + ex.model +
                "\n%  Episode Length: " + ex.max_episode_length +
                "\n%  Iterations: " + ex.iterations +
                "\n%" +  "  Label: " + makeLabel(ex) +
                "\n%" +  "  Composition Type: " + ex.compositionType +
                "\n%" +  "  Seed: " + ex.seed +
                "\n%------");

        try {
            ModulesFile modulesFile = prism.parseModelFile(new File(ex.certainModelFile));
            prism.loadPRISMModel(modulesFile);

            ex.parameterValues = parameterValuation;

            List<List<TransitionTriple>> similarTransitions = ParameterTyer.getSimilarTransitions(pmdp);
            Map<Function, List<TransitionTriple>> functionMap = ParameterTyer.getFunctionMap(pmdp);

            Estimator estimator = estimatorConstructor.get(this.prism, ex);
            estimator.setPmdp(pmdp);
            estimator.setFunctionMap(functionMap);
            estimator.setSimilarTransitions(similarTransitions);
            estimator.set_experiment(ex);

            // Do bisimulation if requested
            if(ex.doBisim) {
                estimator.setUseBisimAggregation(true);
                estimator.setTieAbstractExpressions(true);

                Triple<MDPSimple<Function>, int[], Expression> bisimRes = constructParamBisimulation(pmdp, ex);
                System.out.println(bisimRes.getLeft());
                System.out.println(Arrays.toString(bisimRes.getMiddle()));
                System.out.println(bisimRes.getRight());

                estimator.setBisimulationTopology(bisimRes.getLeft(), bisimRes.getMiddle());
            }

            long startTime = System.nanoTime();
            // Iterate and run experiments for each of the sampled parameter vectors
            //ex.setTieParamters(verification);
            Pair<ArrayList<DataPoint>, ArrayList<IMDP<Double>>> resIMDP = runSampling(ex, estimator, verification);
            double durationInSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;

            DataProcessor dp = new DataProcessor();
            dp.dumpExperimentMetaData(makeOutputDirectory(ex), makeLabel(ex), ex, durationInSeconds, estimator.getSulOpt(), pmdp.getNumStates(), pmdp.getNumTransitions(), resIMDP.first.size(), estimator.getNumLearnableComponents());
            dp.dumpDataRobustPolicies(makeOutputDirectory(ex), makeLabel(ex), resIMDP.first);

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
                System.out.println("======");
                System.out.println(ex.model);
                System.out.println("======");
                System.out.println(estimator.getModelStats());
            }

            ObservationSampler observationSampler = new ObservationSampler(this.prism, SUL, estimator.getTerminatingStates());
            observationSampler.setTransitionsOfInterest(estimator.getTransitionsOfInterest());
            observationSampler.setTiedParameters(ex.tieParameters);
            observationSampler.setMultiplier(ex.multiplier);

            double[] currentResults = estimator.getInitialResults();

            ArrayList<DataPoint> results = new ArrayList<>();
            ArrayList<UMDP<Double>> estimates = new ArrayList<>();
            if (past_iterations == 0) {
                results.add(new DataPoint(0, 0,0, currentResults));
                //estimates.add(estimator.getEstimate());
            }
            int samples = 0;
            Strategy samplingStrategy = estimator.buildStrategy();
            long startTime = System.nanoTime();

            for (int i = past_iterations; i < ex.iterations + past_iterations; i++) {
                int sampled = observationSampler.simulateEpisode(ex.max_episode_length, samplingStrategy);
                samples += sampled;

                boolean last_iteration = i == ex.iterations + past_iterations - 1;
                if (observationSampler.collectedEnoughSamples() || last_iteration || ex.resultIteration(i)) {
                    estimator.setObservationMaps(observationSampler.getSamplesMap(), observationSampler.getSampleSizeMap());

                    currentResults = estimator.getCurrentResults();
                    samplingStrategy = estimator.buildStrategy();


                    if (ex.tieParameters == NO_TYING) {
                        observationSampler.resetObservationSequence();
                    } else {
                        observationSampler.incrementAccumulatedSamples();
                    }

                    if (this.verbose) System.out.println("Episode " + i + ".");
                    if (this.verbose) System.out.println("Performance on unknown MDP (J): " + currentResults[1]);
                    if (this.verbose) System.out.println("Performance Guarantee on learned UMDP (J̃): " + currentResults[0]);
                    if (this.verbose) System.out.println();

                    results.add(new DataPoint(samples, i + 1, System.nanoTime() - startTime, currentResults));

                    if(false) {
                        results.add(new DataPoint(samples, i + 1, System.nanoTime() - startTime, currentResults));
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

    public String makeLabel(Experiment ex) {
        return String.format("%s_%s_%s_%s", ex.model.toString(), ex.useParametricConvex ? "PARCONVEX" : "IMDP", ex.tieParameters, ex.doBisim ? "BISIM": "NOBISIM");
    }

    // Creates the directory path for dumping experimental results
    public String makeOutputDirectory(Experiment ex) {
        String outputPath = String.format("plotting/results/parametric_convex/%s/%s/%s/", ex.model.toString(), ex.parameterValues, ex.seed);
        try {
            Files.createDirectories(Paths.get(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputPath;
    }
}
