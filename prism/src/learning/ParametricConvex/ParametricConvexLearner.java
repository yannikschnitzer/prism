package learning.ParametricConvex;

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBConstr;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBException;
import common.Interval;
import explicit.*;
import imdpcomp.Experiment.IntervalAbstractionMode;
import imdpcomp.Experiment.Model;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static imdpcomp.Experiment.IntervalAbstractionMode.*;
import static imdpcomp.Experiment.ParameterTying.FULL_TYING;
import static imdpcomp.Experiment.ParameterTying.NO_TYING;

public class ParametricConvexLearner {

    Prism prism;

    private final boolean verbose = true;
    private static final String DEFAULT_OUTPUT_ROOT = "plotting_paper_with_ellipsoids/results_learning_new/parametric_convex";
    private static final Model DEFAULT_MODEL = Model.GLIDER;
    private String outputRoot = DEFAULT_OUTPUT_ROOT;
    private static final EnumSet<RunConfiguration> DEFAULT_RUN_CONFIGURATIONS = EnumSet.of(
            RunConfiguration.PARAMETER_TYING,
            RunConfiguration.PARAMETRIC_CONVEX,
            RunConfiguration.LP_TO_INTERVAL_EXACT,
            RunConfiguration.LP_TO_INTERVAL_FAST
    );

    private enum RunConfiguration {
        PLAIN_NAIVE {
            @Override
            void applyTo(Experiment experiment) {
                experiment.setParametricConvex(false).useLPToIMDP(false).setTieParameters(NO_TYING);
            }
        },
        PARAMETER_TYING {
            @Override
            void applyTo(Experiment experiment) {
                experiment.setParametricConvex(false).useLPToIMDP(false).setTieParameters(FULL_TYING);
            }
        },
        PARAMETRIC_CONVEX {
            @Override
            void applyTo(Experiment experiment) {
                experiment.setParametricConvex(true).useLPToIMDP(false).setTieParameters(FULL_TYING).useOBBT(10);
            }
        },
        LP_TO_INTERVAL_EXACT {
            @Override
            void applyTo(Experiment experiment) {
                experiment.setParametricConvex(true).useLPToIMDP(true).setIntervalAbstractionMode(EXACT).useOBBT(10);
            }
        },
        LP_TO_INTERVAL_FAST {
            @Override
            void applyTo(Experiment experiment) {
                experiment.setParametricConvex(true).useLPToIMDP(true).setIntervalAbstractionMode(FAST).useOBBT(10);
            }
        },
        ELLIPSOID {
            @Override
            void applyTo(Experiment experiment) {
                experiment.setParametricConvex(true).useAPSEllipsoid(true).setTieParameters(FULL_TYING).useOBBT(10).useLPToIMDP(false);
            }
        },
        ELLIPSOID_TO_INTERVAL_EXACT {
            @Override
            void applyTo(Experiment experiment) {
                experiment.setParametricConvex(true).useAPSEllipsoid(true).useLPToIMDP(true).setIntervalAbstractionMode(EXACT).setTieParameters(FULL_TYING).useOBBT(10);
            }
        },
        ELLIPSOID_TO_INTERVAL_FAST {
            @Override
            void applyTo(Experiment experiment) {
                experiment.setParametricConvex(true).useAPSEllipsoid(true).useLPToIMDP(true).setIntervalAbstractionMode(FAST).setTieParameters(FULL_TYING).useOBBT(10);
            }
        };

        abstract void applyTo(Experiment experiment);

        Experiment createExperiment(Model model) {
            Experiment experiment = new Experiment(model);
            applyTo(experiment);
            return experiment;
        }

        static String availableNames() {
            StringJoiner joiner = new StringJoiner(", ");
            for (RunConfiguration runConfiguration : values()) {
                joiner.add(runConfiguration.name());
            }
            return joiner.toString();
        }
    }

    private static final class CliOptions {
        Model model = DEFAULT_MODEL;
        EnumSet<RunConfiguration> runConfigurations = EnumSet.copyOf(DEFAULT_RUN_CONFIGURATIONS);
        String outputRoot = DEFAULT_OUTPUT_ROOT;
        Integer iterationsOverride = null;
        Integer maxEpisodeLengthOverride = null;
        Integer multiplierOverride = null;
        Integer seedOverride = null;
        final Map<String, Object> parameterOverrides = new LinkedHashMap<>();
    }

    public ParametricConvexLearner(Prism prism) {
        this.prism = prism;
    }

    public static void main(String[] args) throws GRBException, PrismException {
        if (isHelpInvocation(args)) {
            printUsage();
            return;
        }

        CliOptions options = parseCliOptions(args);

        ParametricConvexLearner parametricConvexLearner = new ParametricConvexLearner(new Prism(new PrismDevNullLog()));
        parametricConvexLearner.outputRoot = options.outputRoot;
        parametricConvexLearner.initializePrism();

        Experiment baseExperiment = new Experiment(options.model);
        applyExperimentOverrides(baseExperiment, options);
        MDPSimple<Function> pmdp = parametricConvexLearner.buildParamModel(baseExperiment);

        System.out.println("Output root: " + options.outputRoot);
        System.out.println("Model: " + options.model);
        System.out.println("Run configurations: " + options.runConfigurations);
        if (options.iterationsOverride != null) {
            System.out.println("Overriding iterations: " + options.iterationsOverride);
        }
        if (options.maxEpisodeLengthOverride != null) {
            System.out.println("Overriding max episode length: " + options.maxEpisodeLengthOverride);
        }
        if (options.multiplierOverride != null) {
            System.out.println("Overriding multiplier: " + options.multiplierOverride);
        }
        if (options.seedOverride != null) {
            System.out.println("Overriding seed: " + options.seedOverride);
        }
        if (!options.parameterOverrides.isEmpty()) {
            System.out.println("Overriding parameters: " + options.parameterOverrides);
        }

        for (RunConfiguration runConfiguration : options.runConfigurations) {
            Experiment ex = runConfiguration.createExperiment(options.model);
            applyExperimentOverrides(ex, options);
            parametricConvexLearner.learnIMDP(ex,
                    ex.useParametricConvex ? PACConvexEstimatorOptimistic::new : PACIntervalEstimatorOptimistic::new,
                    pmdp,
                    ex.parameterValues,
                    true);
        }
    }

    private static boolean isHelpInvocation(String[] args) {
        for (String arg : args) {
            if ("--help".equals(arg) || "-h".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static void printUsage() {
        System.out.println("ParametricConvexLearner");
        System.out.println("Usage:");
        System.out.println("  java -cp \"classes:lib/*\" learning.ParametricConvex.ParametricConvexLearner [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --model=<MODEL>                 Model enum value (default: " + DEFAULT_MODEL + ")");
        System.out.println("  --runs=<R1,R2,...|all>          Run configurations. Can also be passed positionally.");
        System.out.println("  --output-root=<PATH>            Output root directory (default: " + DEFAULT_OUTPUT_ROOT + ")");
        System.out.println("  --iterations=<N>                Override number of iterations");
        System.out.println("  --max-episode-length=<N>        Override max episode length");
        System.out.println("  --multiplier=<N>                Override learning multiplier");
        System.out.println("  --seed=<N>                      Override random seed");
        System.out.println("  --param=<k=v[,k=v...]>          Override one or more model parameters");
        System.out.println("  --help, -h                      Show this message");
        System.out.println();
        System.out.println("Available run configurations:");
        System.out.println("  " + RunConfiguration.availableNames());
    }

    private static CliOptions parseCliOptions(String[] args) {
        CliOptions options = new CliOptions();
        List<String> runTokens = new ArrayList<>();

        for (String arg : args) {
            if (arg == null || arg.isBlank()) {
                continue;
            }

            if (arg.startsWith("--model=")) {
                String modelName = arg.substring("--model=".length()).trim();
                try {
                    options.model = Model.valueOf(normalizeRunConfigurationToken(modelName));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Unknown model '" + modelName + "'.");
                }
                continue;
            }

            if (arg.startsWith("--output-root=")) {
                String outputRoot = arg.substring("--output-root=".length()).trim();
                if (!outputRoot.isBlank()) {
                    options.outputRoot = outputRoot;
                }
                continue;
            }

            if (arg.startsWith("--iterations=")) {
                options.iterationsOverride = parsePositiveIntegerOrThrow("iterations", arg.substring("--iterations=".length()));
                continue;
            }

            if (arg.startsWith("--max-episode-length=")) {
                options.maxEpisodeLengthOverride = parsePositiveIntegerOrThrow("max episode length", arg.substring("--max-episode-length=".length()));
                continue;
            }

            if (arg.startsWith("--multiplier=")) {
                options.multiplierOverride = parsePositiveIntegerOrThrow("multiplier", arg.substring("--multiplier=".length()));
                continue;
            }

            if (arg.startsWith("--seed=")) {
                options.seedOverride = parsePositiveIntegerOrThrow("seed", arg.substring("--seed=".length()));
                continue;
            }

            if (arg.startsWith("--param=")) {
                parseParameterOverrides(options.parameterOverrides, arg.substring("--param=".length()));
                continue;
            }

            if (arg.startsWith("--params=")) {
                parseParameterOverrides(options.parameterOverrides, arg.substring("--params=".length()));
                continue;
            }

            runTokens.add(arg);
        }

        options.runConfigurations = resolveRunConfigurations(runTokens);
        return options;
    }

    private static EnumSet<RunConfiguration> resolveRunConfigurations(List<String> rawArgs) {
        if (rawArgs.isEmpty()) {
            return EnumSet.copyOf(DEFAULT_RUN_CONFIGURATIONS);
        }

        EnumSet<RunConfiguration> selectedRunConfigurations = EnumSet.noneOf(RunConfiguration.class);
        for (String arg : rawArgs) {
            String rawToken = arg.startsWith("--runs=") ? arg.substring("--runs=".length()) : arg;
            for (String token : rawToken.split(",")) {
                String runName = token.trim();
                if (runName.isEmpty()) {
                    continue;
                }
                if ("all".equalsIgnoreCase(runName)) {
                    return EnumSet.allOf(RunConfiguration.class);
                }
                try {
                    selectedRunConfigurations.add(RunConfiguration.valueOf(normalizeRunConfigurationToken(runName)));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Unknown run configuration '" + runName + "'. Available: " + RunConfiguration.availableNames()
                                    + ". Usage: --runs=PARAMETRIC_CONVEX,ELLIPSOID or pass names as positional args."
                    );
                }
            }
        }

        return selectedRunConfigurations.isEmpty()
                ? EnumSet.copyOf(DEFAULT_RUN_CONFIGURATIONS)
                : selectedRunConfigurations;
    }

    private static String normalizeRunConfigurationToken(String runName) {
        return runName.toUpperCase(Locale.ROOT).replace('-', '_');
    }

    private static Integer parsePositiveIntegerOrThrow(String label, String token) {
        String trimmed = token == null ? "" : token.trim();
        Integer value = parseInteger(trimmed);
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Invalid " + label + " '" + token + "'. Use a positive integer.");
        }
        return value;
    }

    private static Integer parseInteger(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(rawValue.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void parseParameterOverrides(Map<String, Object> parameterOverrides, String rawOverrides) {
        if (rawOverrides == null || rawOverrides.isBlank()) {
            throw new IllegalArgumentException("Empty --param override.");
        }

        String[] entries = rawOverrides.split(",");
        for (String entry : entries) {
            String trimmedEntry = entry.trim();
            if (trimmedEntry.isEmpty()) {
                continue;
            }

            int separatorIndex = trimmedEntry.indexOf('=');
            if (separatorIndex <= 0 || separatorIndex >= trimmedEntry.length() - 1) {
                throw new IllegalArgumentException("Invalid parameter override '" + trimmedEntry + "'. Expected key=value.");
            }

            String parameterName = trimmedEntry.substring(0, separatorIndex).trim();
            String rawValue = trimmedEntry.substring(separatorIndex + 1).trim();
            parameterOverrides.put(parameterName, parseParameterValue(rawValue));
        }
    }

    private static Object parseParameterValue(String rawValue) {
        if ("true".equalsIgnoreCase(rawValue)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(rawValue)) {
            return Boolean.FALSE;
        }

        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException ignored) {
            // try double below
        }

        try {
            return Double.parseDouble(rawValue);
        } catch (NumberFormatException ignored) {
            // keep string below
        }

        return rawValue;
    }

    private static void applyExperimentOverrides(Experiment experiment, CliOptions options) {
        if (options.iterationsOverride != null) {
            experiment.iterations = options.iterationsOverride;
        }
        if (options.maxEpisodeLengthOverride != null) {
            experiment.max_episode_length = options.maxEpisodeLengthOverride;
        }
        if (options.multiplierOverride != null) {
            experiment.multiplier = options.multiplierOverride;
        }
        if (options.seedOverride != null) {
            experiment.seed = options.seedOverride;
        }

        for (Map.Entry<String, Object> entry : options.parameterOverrides.entrySet()) {
            String parameterName = entry.getKey();
            Object parameterValue = entry.getValue();
            experiment.parameterValues.setValue(parameterName, parameterValue);
            if (experiment.identParameters.contains(parameterName)) {
                experiment.identParameters.setValue(parameterName, parameterValue);
            }
        }
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
        explicit.StateModelChecker mc = new explicit.StateModelChecker(null);
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
//            if (past_iterations == 0) {
//                results.add(new DataPoint(0, 0,0, currentResults));
//                //estimates.add(estimator.getEstimate());
//            }
            int samples = 0;
            Strategy samplingStrategy = estimator.buildStrategy();
            long startTime = System.nanoTime();

            for (int i = past_iterations; i < ex.iterations + past_iterations; i++) {
                int sampled = observationSampler.simulateEpisode(ex.max_episode_length, samplingStrategy);
                samples += sampled;

                boolean last_iteration = i == ex.iterations + past_iterations - 1;
                if (observationSampler.collectedEnoughSamples() || last_iteration || ex.resultIteration(i) || i == 1) {
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
        String name = ex.model.toString();
        if (ex.useParametricConvex) {
            if (ex.useApsEllipsoid) {
                name += "_ELLIPSOID";
            } else {
                name += "_PARCONVEX";
            }
        } else {
            name += "_IMDP";
        }

        if (ex.useLPToIntervals) {
            name += "_CPINTERVAL_" + ((ex.intervalAbstractionMode == EXACT) ? "EXACT" : "FAST");
        }

        name += "_" + ex.tieParameters;
        //return String.format("%s_%s_%s_%s", ex.model.toString(), ex.useParametricConvex ? (ex.useLPToIntervals ? ("LPINTERVAL_" + ((ex.intervalAbstractionMode == EXACT) ? "EXACT" : "FAST")) : (ex.useApsEllipsoid ? "ELLIPSOID" : "PARCONVEX")) : "IMDP", ex.tieParameters, ex.doBisim ? "BISIM": "NOBISIM");
        return name;
    }

    // Creates the directory path for dumping experimental results
    public String makeOutputDirectory(Experiment ex) {
        String outputPath = String.format("%s/%s/%s/%s/", this.outputRoot, ex.model.toString(), ex.parameterValues.getNumValues() > 10 ? ex.identParameters : ex.parameterValues, ex.seed);
        try {
            Files.createDirectories(Paths.get(outputPath));
            copyExperimentPrismFile(ex, outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputPath;
    }

    private void copyExperimentPrismFile(Experiment ex, String outputPath) throws IOException {
        String modelPath = (ex.modelFile != null && !ex.modelFile.isBlank()) ? ex.modelFile : ex.certainModelFile;
        if (modelPath == null || modelPath.isBlank()) {
            return;
        }

        Path source = Paths.get(modelPath).normalize();
        if (!Files.exists(source)) {
            return;
        }

        String fileName = source.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!fileName.endsWith(".prism") && !fileName.endsWith(".pm")) {
            return;
        }

        Path target = Paths.get(outputPath).resolve(source.getFileName());
        if (Files.exists(target)) {
            return;
        }

        Files.copy(source, target);
    }
}
