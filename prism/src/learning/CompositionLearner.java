package learning;

import explicit.IMDP;
import explicit.MDP;
import explicit.MDPSimple;
import explicit.UMDP;
import imdpcomp.Experiment;
import learning.Data.DataPoint;
import learning.Data.DataProcessor;
import learning.Estimators.Estimator;
import learning.Estimators.EstimatorConstructor;
import learning.Estimators.PACIntervalEstimatorOptimistic;
import learning.Simulation.ObservationSampler;
import param.Function;
import parser.Values;
import parser.ast.ModulesFile;
import prism.Pair;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import strat.Strategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CompositionLearner {
    Prism prism;

    private final boolean verbose = true;

    int seed = 5;
    int iterations = 1_000_000;
    int max_episode_length = 100;
    int multiplier = 2;

    public CompositionLearner(Prism prism) {
        this.prism = prism;
    }

    public static void main(String[] args) throws PrismException {
        CompositionLearner learner = new CompositionLearner(new Prism(new PrismDevNullLog()));
        learner.initializePrism();

        Experiment ex = new Experiment(Experiment.Model.CHAIN);
        MDPSimple<Function> pmdp = learner.buildParamModel(ex);
        System.out.println(pmdp);

        learner.learnIMDP("test", ex, PACIntervalEstimatorOptimistic::new, pmdp, ex.parameterValues, true);

        System.out.println("Done");
    }

    public MDPSimple<Function> buildParamModel(Experiment experiment) {
        try {
            ModulesFile modulesFile = this.prism.parseModelFile(new File(experiment.certainModelFile));
            prism.loadPRISMModel(modulesFile);
            if (experiment.parameterValues != null) {
                prism.setPRISMModelConstants(experiment.parameterValues);
            }

            //Get parametric model
//            String[] paramNames = new String[]{"p","q"};
//            String[] paramLowerBounds = new String[]{"0","0"};
//            String[] paramUpperBounds = new String[]{"1","1"};
//            //this.prism.setPRISMModelConstants(new Values(), true);
//            this.prism.setParametric(paramNames, paramLowerBounds, paramUpperBounds);
            this.prism.buildModel();
            MDPSimple<Function> model = (MDPSimple<Function>) this.prism.getBuiltModelExplicit();

            return model;

        } catch (PrismException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Pair<List<List<IMDP<Double>>>, List<MDP<Double>>> learnIMDP(String label, Experiment ex, EstimatorConstructor estimatorConstructor, MDPSimple<Function> mdpParam, Values parameterValuation, boolean verification) {
        resetAll(seed);

        System.out.println("\n\n\n\n%------\n% Learning IMDP\n%  Model: " + ex.model + "\n%  max_episode_length: "
                + max_episode_length + "\n%  iterations: " + iterations + "\n%------");
        if (verbose)
            System.out.printf("%s, seed %d\n", label, seed);

        try {
            ModulesFile modulesFile = prism.parseModelFile(new File(ex.certainModelFile));
            prism.loadPRISMModel(modulesFile);

            ex.parameterValues = parameterValuation;
            Estimator estimator = estimatorConstructor.get(this.prism, ex);
            System.out.println("Constant Values:" + estimator.getSUL().getConstantValues());
            estimator.set_experiment(ex);
            estimator.setPmdp(mdpParam);

            // Iterate and run experiments for each of the sampled parameter vectors
            //ex.setTieParamters(verification);
            Pair<ArrayList<DataPoint>, ArrayList<IMDP<Double>>> resIMDP = runSampling(ex, estimator, verification);

            DataProcessor dp = new DataProcessor();
            dp.dumpDataRobustPolicies(makeOutputDirectory(ex), label, resIMDP.first);

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
            observationSampler.setMultiplier(multiplier);

            double[] currentResults = estimator.getInitialResults();


            ArrayList<DataPoint> results = new ArrayList<>();
            ArrayList<UMDP<Double>> estimates = new ArrayList<>();
            if (past_iterations == 0) {
                results.add(new DataPoint(0, past_iterations, currentResults));
                estimates.add(estimator.getEstimate());
            }
            int samples = 0;
            Strategy samplingStrategy = estimator.buildStrategy();
            for (int i = past_iterations; i < iterations + past_iterations; i++) {
                int sampled = observationSampler.simulateEpisode(max_episode_length, samplingStrategy);
                samples += sampled;

                boolean last_iteration = i == iterations + past_iterations - 1;
                if (observationSampler.collectedEnoughSamples() || last_iteration) { // || resultIteration(i)
                    estimator.setObservationMaps(observationSampler.getSamplesMap(), observationSampler.getSampleSizeMap());
                    samplingStrategy = estimator.buildStrategy();
                    currentResults = estimator.getCurrentResults();

                    if (!ex.tieParameters) { // || (!verficiation && ex.isBayesian())
                        observationSampler.resetObservationSequence();
                    } else {
                        observationSampler.incrementAccumulatedSamples();
                    }

                    if (this.verbose) System.out.println("Episode " + i + ".");
                    if (this.verbose) System.out.println("Performance on MDPs (J): " + currentResults[1]);
                    if (this.verbose) System.out.println("Performance Guarantee on IMDPs (J̃): " + currentResults[0]);
                    if (this.verbose) System.out.println();

                    //if (last_iteration || ex.resultIteration(i)) {
                    if(true) {
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

    @SuppressWarnings("unchecked")
    public void initializePrism() throws PrismException {
        this.prism = new Prism(new PrismDevNullLog());
        this.prism.initialise();
        this.prism.setEngine(Prism.EXPLICIT);
        this.prism.setGenStrat(true);
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

    public String makeOutputDirectory(Experiment ex) {
        String outputPath = String.format("plotting/results/%s/%s/Robust_Policies_WCC/%s/", ex.parameterValues, ex.model.toString(), seed);
        try {
            Files.createDirectories(Paths.get(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputPath;
    }

}
