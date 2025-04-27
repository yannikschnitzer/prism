package learning;

import explicit.*;
import imdpcomp.Experiment;
import learning.Data.DataPoint;
import learning.Data.DataProcessor;
import learning.Estimators.Estimator;
import learning.Estimators.EstimatorConstructor;
import learning.Estimators.PACIntervalEstimatorOptimistic;
import learning.Simulation.ObservationSampler;
import learning.Simulation.TransitionTriple;
import param.Function;
import parser.Values;
import parser.ast.ModulesFile;
import prism.*;
import strat.Strategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates sampling-based learning of IMDPs.
 * Builds the parametric MDP, samples execution traces, and dumps robust policy data.
 */
public class CompositionLearner {
    Prism prism;

    private final boolean verbose = true;

    int seed = 5;
    int iterations = 1_000_000;
    int max_episode_length = 20;
    int multiplier = 5;

    public CompositionLearner(Prism prism) {
        this.prism = prism;
    }

    public static void main(String[] args) throws PrismException {
        CompositionLearner learner = new CompositionLearner(new Prism(new PrismDevNullLog()));
        learner.initializePrism();

        Experiment ex = new Experiment(Experiment.Model.AIRCRAFT);

        // Build the parametric MDP to infer parametric structure
        MDPSimple<Function> pmdp = learner.buildParamModel(ex);

       //System.out.println(pmdp);
        for (int i = 0; i < pmdp.getNumStates(); i++) {
            //System.out.println("State: " + i);
            for (int j = 0; j < pmdp.getNumChoices(i); j++) {
                //System.out.println("Choice: " + pmdp.getAction(i,j));
                Distribution<Function> c = pmdp.getChoice(i,j);
                List<List<Function>> marginals = c.getMarginals();
                //System.out.println("Marginal:" + marginals);
                //System.out.println("Multiplied out:" + c.multiplyMarginals());
                //System.out.println("Support: " + Arrays.toString(c.getSupportArray()) + " Size: " + c.getSupportArray().length);
                //System.out.println("Support (no dup): " + Arrays.toString(c.supportArrayUnique));
                c.calculateSupportMarginalMap();
                //System.out.println("Distribution: " + c + " Size: " + c.getSupport().size());
            }
            //System.out.println("");
        }
        learner.learnIMDP("learning", ex, PACIntervalEstimatorOptimistic::new, pmdp, ex.parameterValues, true);

        System.out.println("Done");
    }

    // Builds the parametric MDP model from the PRISM definition
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

    // Builds the hidden true MDP from which sample trajectories are generated
    public MDPSimple<Double> buildSamplingModel(Experiment experiment) {
        try {
            ModulesFile modulesFile = this.prism.parseModelFile(new File(experiment.certainModelFile));
            prism.loadPRISMModel(modulesFile);
            if (experiment.parameterValues != null) {
                prism.setPRISMModelConstants(experiment.parameterValues, true);
            }
            this.prism.buildModel();
            return (MDPSimple<Double>) this.prism.getBuiltModelExplicit();

        } catch (PrismException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Learns an IMDP by running sampling-based experiments with the specified estimator
    public Pair<List<List<IMDP<Double>>>, List<MDP<Double>>> learnIMDP(String label, Experiment ex, EstimatorConstructor estimatorConstructor, MDPSimple<Function> pmdp, Values parameterValuation, boolean verification) {
        resetAll(seed);

        System.out.println("\n\n\n\n%------\n% Learning IMDP\n%  Model: " + ex.model + "\n%  max_episode_length: "
                + max_episode_length + "\n%  iterations: " + iterations + "\n%------");
        if (verbose)
            System.out.printf("%s, seed %d\n", label, seed);

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

            // Iterate and run experiments for each of the sampled parameter vectors
            Pair<ArrayList<DataPoint>, ArrayList<IMDP<Double>>> resIMDP = runSampling(ex, estimator, verification);

            DataProcessor dp = new DataProcessor();
            dp.dumpDataRobustPolicies(makeOutputDirectory(ex), label, resIMDP.first);

        } catch (PrismException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    // Samples episodes until enough data is collected or max iterations reached
    public Pair<ArrayList<DataPoint>, ArrayList<IMDP<Double>>> runSampling(Experiment ex, Estimator estimator, boolean verification) {
        return runSampling(ex, estimator, 0, verification);
    }

    // Samples episodes until enough data is collected or max iterations reached
    public Pair<ArrayList<DataPoint>, ArrayList<IMDP<Double>>> runSampling(Experiment ex, Estimator estimator, int past_iterations, boolean verification) {
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
            observationSampler.setMultiplier(multiplier);

            double[] currentResults = estimator.getInitialResults();


            ArrayList<DataPoint> results = new ArrayList<>();
            ArrayList<UMDP<Double>> estimates = new ArrayList<>();
            if (past_iterations == 0) {
                results.add(new DataPoint(0, past_iterations, currentResults));
                //estimates.add(estimator.getEstimate());
            }
            int samples = 0;
            Strategy samplingStrategy = estimator.buildStrategy();
            for (int i = past_iterations; i < iterations + past_iterations; i++) {
                // Simulate one episode and collect samples
                int sampled = observationSampler.simulateEpisode(max_episode_length, samplingStrategy);
                samples += sampled;

                // Check if enough samples have been collected or if this is the last iteration
                boolean last_iteration = i == iterations + past_iterations - 1;
                if (observationSampler.collectedEnoughSamples() || last_iteration) { // || resultIteration(i)
                    estimator.setObservationMaps(observationSampler.getSamplesMap(), observationSampler.getSampleSizeMap());

                    currentResults = estimator.getCurrentResults();
                    samplingStrategy = estimator.buildStrategy();

                    if (!ex.tieParameters) { // || (!verification && ex.isBayesian())
                        observationSampler.resetObservationSequence();
                    } else {
                        observationSampler.incrementAccumulatedSamples();
                    }

                    if (this.verbose) System.out.println("Episode " + i + ".");
                    if (this.verbose) System.out.println("Performance on MDPs (J): " + currentResults[1]);
                    if (this.verbose) System.out.println("Performance Guarantee on IMDPs (J̃): " + currentResults[0]);
                    if (this.verbose) System.out.println();

                    results.add(new DataPoint(samples, i + 1, currentResults));

                    //if (last_iteration || ex.resultIteration(i)) {
                    if(false) {
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

    // Initializes the PRISM engine for explicit model checking and strategy generation
    @SuppressWarnings("unchecked")
    public void initializePrism() throws PrismException {
        this.prism = new Prism(new PrismDevNullLog());
        this.prism.initialise();
        this.prism.setEngine(Prism.EXPLICIT);
        this.prism.setGenStrat(true);
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

    // Creates the directory path for dumping experimental results
    public String makeOutputDirectory(Experiment ex) {
        String outputPath = String.format("plotting/results/%s/%s/%s/", ex.model.toString(),ex.parameterValues, seed);
        try {
            Files.createDirectories(Paths.get(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputPath;
    }

}
