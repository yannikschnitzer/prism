package learning.ParametricConvex;

import com.gurobi.gurobi.GRBException;
import explicit.*;
import imdpcomp.Experiment;
import imdpcomp.Experiment.Model;
import learning.Data.DataPoint;
import learning.Data.DataProcessor;
import learning.Estimators.Estimator;
import learning.Estimators.EstimatorConstructor;
import learning.Estimators.PACConvexEstimatorOptimistic;
import learning.Estimators.PACIntervalEstimatorOptimistic;
import learning.ParameterTyer;
import learning.Simulation.ObservationSampler;
import learning.Simulation.StateActionPair;
import learning.Simulation.TransitionTriple;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import param.Function;
import parser.State;
import parser.Values;
import parser.ast.Expression;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.Pair;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import strat.Strategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static imdpcomp.Experiment.IntervalAbstractionMode.EXACT;
import static imdpcomp.Experiment.IntervalAbstractionMode.FAST;
import static imdpcomp.Experiment.ParameterTying.FULL_TYING;
import static imdpcomp.Experiment.ParameterTying.NO_TYING;

public class ParametricConvexSolver {

    Prism prism;

    private final boolean verbose = true;

    private HashMap<TransitionTriple, Integer> cachedSamplesMap;
    private HashMap<StateActionPair, Integer> cachedSampleSizeMap;

    private static final String DEFAULT_OUTPUT_ROOT = "plotting_paper_with_ellipsoids/results_uniform_solving_new/parametric_convex";
    private static final String DEFAULT_BENCHMARK_INPUT_ROOT = DEFAULT_OUTPUT_ROOT;
    private static final String DEFAULT_BENCHMARK_OUTPUT_BASE = "plotting_paper_with_ellipsoids/benchmark_results";
    private static final DateTimeFormatter BENCHMARK_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static final Model DEFAULT_MODEL = Model.BETTING_GAME_CONVEX_ADAPTIVE;

    // Keep this set small and explicit; pass CLI args to override without editing code.
    private static final EnumSet<RunConfiguration> DEFAULT_RUN_CONFIGURATIONS = EnumSet.of(
            RunConfiguration.PARAMETER_TYING,
            RunConfiguration.PARAMETRIC_CONVEX,
            RunConfiguration.LP_TO_INTERVAL_EXACT,
            RunConfiguration.LP_TO_INTERVAL_FAST,
//            RunConfiguration.ELLIPSOID,
            RunConfiguration.ELLIPSOID_TO_INTERVAL_EXACT,
            RunConfiguration.ELLIPSOID_TO_INTERVAL_FAST
    );

    // IntelliJ convenience: when no CLI args are passed, this controls what main() does.
    private enum IdeExecutionMode {
        DEFAULT_MODEL,
        REPRODUCE_BENCHMARKS
    }

    private static final IdeExecutionMode IDE_EXECUTION_MODE = IdeExecutionMode.REPRODUCE_BENCHMARKS;
    private static final Model IDE_MODEL = DEFAULT_MODEL;
    private static final EnumSet<RunConfiguration> IDE_RUN_CONFIGURATIONS = EnumSet.copyOf(DEFAULT_RUN_CONFIGURATIONS);
    private static final String IDE_BENCHMARK_INPUT_ROOT = DEFAULT_BENCHMARK_INPUT_ROOT;
    // null means auto-create timestamped output directory.
    private static final String IDE_BENCHMARK_OUTPUT_ROOT = null;
    // null means no timeout.
    private static final Integer IDE_BENCHMARK_TIMEOUT_SECONDS = null;

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
        boolean reproduceBenchmarks = false;
        String benchmarkInputRoot = DEFAULT_BENCHMARK_INPUT_ROOT;
        String benchmarkOutputRoot = null;
        Integer benchmarkTimeoutSeconds = null;
    }

    private static final class BenchmarkInstance {
        final Model model;
        final String parameterDirectoryName;
        final Values parameterValues;
        final Values identParameters;
        final int seed;
        final Path modelFile;
        final String spec;
        final String robustSpec;
        final String optimisticSpec;
        final String dtmcSpec;
        final Integer iterations;
        final Integer maxEpisodeLength;
        final Integer multiplier;
        final Double errorTolerance;
        final Boolean useVertexPrecomp;
        final Boolean verboseBisim;

        BenchmarkInstance(
                Model model,
                String parameterDirectoryName,
                Values parameterValues,
                Values identParameters,
                int seed,
                Path modelFile,
                String spec,
                String robustSpec,
                String optimisticSpec,
                String dtmcSpec,
                Integer iterations,
                Integer maxEpisodeLength,
                Integer multiplier,
                Double errorTolerance,
                Boolean useVertexPrecomp,
                Boolean verboseBisim
        ) {
            this.model = model;
            this.parameterDirectoryName = parameterDirectoryName;
            this.parameterValues = parameterValues;
            this.identParameters = identParameters;
            this.seed = seed;
            this.modelFile = modelFile;
            this.spec = spec;
            this.robustSpec = robustSpec;
            this.optimisticSpec = optimisticSpec;
            this.dtmcSpec = dtmcSpec;
            this.iterations = iterations;
            this.maxEpisodeLength = maxEpisodeLength;
            this.multiplier = multiplier;
            this.errorTolerance = errorTolerance;
            this.useVertexPrecomp = useVertexPrecomp;
            this.verboseBisim = verboseBisim;
        }
    }

    private String outputRoot = DEFAULT_OUTPUT_ROOT;
    private boolean forceIdentParameterDirectory = false;


    public ParametricConvexSolver(Prism prism) {
        this.prism = prism;
    }

    public static void main(String[] args) throws GRBException, PrismException {
        ParametricConvexSolver parametricConvexLearner = new ParametricConvexSolver(new Prism(new PrismDevNullLog()));
        parametricConvexLearner.initializePrism();

        CliOptions options = args.length == 0 ? buildIdeOptions() : parseCliOptions(args);
        if (options.reproduceBenchmarks) {
            parametricConvexLearner.runBenchmarkReproduction(options);
        } else {
            parametricConvexLearner.runDefaultModel(options.model, options.runConfigurations);
        }
    }

    private static CliOptions buildIdeOptions() {
        CliOptions options = new CliOptions();
        options.model = IDE_MODEL;
        options.runConfigurations = EnumSet.copyOf(IDE_RUN_CONFIGURATIONS);

        if (IDE_EXECUTION_MODE == IdeExecutionMode.REPRODUCE_BENCHMARKS) {
            options.reproduceBenchmarks = true;
            options.benchmarkInputRoot = IDE_BENCHMARK_INPUT_ROOT;
            options.benchmarkOutputRoot = IDE_BENCHMARK_OUTPUT_ROOT == null || IDE_BENCHMARK_OUTPUT_ROOT.isBlank()
                    ? createFreshBenchmarkOutputRoot()
                    : IDE_BENCHMARK_OUTPUT_ROOT;
            options.benchmarkTimeoutSeconds = IDE_BENCHMARK_TIMEOUT_SECONDS;
        }

        return options;
    }

    private static CliOptions parseCliOptions(String[] args) {
        CliOptions options = new CliOptions();
        List<String> runTokens = new ArrayList<>();

        for (String arg : args) {
            if (arg == null || arg.isBlank()) {
                continue;
            }

            if ("--reproduce-benchmarks".equals(arg)) {
                options.reproduceBenchmarks = true;
                continue;
            }

            if (arg.startsWith("--benchmark-input-root=")) {
                options.benchmarkInputRoot = arg.substring("--benchmark-input-root=".length()).trim();
                continue;
            }

            if (arg.startsWith("--benchmark-root=")) {
                options.benchmarkInputRoot = arg.substring("--benchmark-root=".length()).trim();
                continue;
            }

            if (arg.startsWith("--benchmark-output-root=")) {
                options.benchmarkOutputRoot = arg.substring("--benchmark-output-root=".length()).trim();
                continue;
            }

            if (arg.startsWith("--benchmark-timeout-seconds=")) {
                String timeoutToken = arg.substring("--benchmark-timeout-seconds=".length()).trim();
                Integer timeoutSeconds = parseInteger(timeoutToken);
                if (timeoutSeconds == null || timeoutSeconds <= 0) {
                    throw new IllegalArgumentException("Invalid benchmark timeout '" + timeoutToken + "'. Use a positive integer number of seconds.");
                }
                options.benchmarkTimeoutSeconds = timeoutSeconds;
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

            runTokens.add(arg);
        }

        options.runConfigurations = resolveRunConfigurations(runTokens);
        if (options.reproduceBenchmarks && (options.benchmarkOutputRoot == null || options.benchmarkOutputRoot.isBlank())) {
            options.benchmarkOutputRoot = createFreshBenchmarkOutputRoot();
        }

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

    private void runDefaultModel(Model model, EnumSet<RunConfiguration> selectedRunConfigurations) {
        this.outputRoot = DEFAULT_OUTPUT_ROOT;
        this.forceIdentParameterDirectory = false;
        clearCachedSamples();

        MDPSimple<Function> pmdp = buildParamModel(new Experiment(model));
        for (RunConfiguration runConfiguration : selectedRunConfigurations) {
            Experiment ex = runConfiguration.createExperiment(model);
            solveIMDPUniform(ex,
                    ex.useParametricConvex ? PACConvexEstimatorOptimistic::new : PACIntervalEstimatorOptimistic::new,
                    pmdp,
                    ex.parameterValues,
                    true);
        }
    }

    private void runBenchmarkReproduction(CliOptions options) {
        List<BenchmarkInstance> benchmarkInstances = discoverBenchmarkInstances(Paths.get(options.benchmarkInputRoot));
        if (benchmarkInstances.isEmpty()) {
            throw new IllegalArgumentException("No benchmark instances found in '" + options.benchmarkInputRoot + "'.");
        }

        this.outputRoot = options.benchmarkOutputRoot;
        this.forceIdentParameterDirectory = true;

        System.out.println("Reproducing " + benchmarkInstances.size() + " benchmark instances from " + options.benchmarkInputRoot);
        System.out.println("Output root: " + options.benchmarkOutputRoot);
        System.out.println("Run configurations: " + options.runConfigurations);
        if (options.benchmarkTimeoutSeconds != null) {
            System.out.println("Per-run timeout: " + options.benchmarkTimeoutSeconds + "s");
        }

        for (BenchmarkInstance benchmarkInstance : benchmarkInstances) {
            clearCachedSamples();
            System.out.println("Benchmark instance: " + benchmarkInstance.model + " / " + benchmarkInstance.parameterDirectoryName + " / seed " + benchmarkInstance.seed);

            Experiment baselineExperiment = applyBenchmarkInstance(runConfigurationIndependentExperiment(benchmarkInstance.model), benchmarkInstance);
            MDPSimple<Function> pmdp = buildParamModel(baselineExperiment);

            for (RunConfiguration runConfiguration : options.runConfigurations) {
                Experiment ex = applyBenchmarkInstance(runConfiguration.createExperiment(benchmarkInstance.model), benchmarkInstance);
                solveIMDPUniform(ex,
                        ex.useParametricConvex ? PACConvexEstimatorOptimistic::new : PACIntervalEstimatorOptimistic::new,
                        pmdp,
                        ex.parameterValues,
                        true,
                        options.benchmarkTimeoutSeconds);
            }
        }
    }

    private static Experiment runConfigurationIndependentExperiment(Model model) {
        return new Experiment(model);
    }

    private static String createFreshBenchmarkOutputRoot() {
        String timestamp = LocalDateTime.now().format(BENCHMARK_TIMESTAMP_FORMAT);
        return Paths.get(DEFAULT_BENCHMARK_OUTPUT_BASE, "benchmark_results_" + timestamp, "parametric_convex").toString();
    }

    private static String normalizeRunConfigurationToken(String runName) {
        return runName.toUpperCase(Locale.ROOT).replace('-', '_');
    }

    private static List<BenchmarkInstance> discoverBenchmarkInstances(Path benchmarkRoot) {
        if (!Files.exists(benchmarkRoot) || !Files.isDirectory(benchmarkRoot)) {
            throw new IllegalArgumentException("Benchmark root does not exist or is not a directory: " + benchmarkRoot);
        }

        Map<Path, Path> metadataBySeedDirectory = new TreeMap<>();
        try (Stream<Path> files = Files.walk(benchmarkRoot)) {
            files.filter(Files::isRegularFile)
                    .filter(ParametricConvexSolver::isYamlFile)
                    .sorted()
                    .forEach(path -> {
                        Path seedDirectory = path.getParent();
                        Path relativeSeedPath = benchmarkRoot.relativize(seedDirectory);
                        if (relativeSeedPath.getNameCount() >= 3) {
                            metadataBySeedDirectory.putIfAbsent(seedDirectory, path);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to discover benchmark metadata files in " + benchmarkRoot, e);
        }

        List<BenchmarkInstance> instances = new ArrayList<>();
        for (Map.Entry<Path, Path> entry : metadataBySeedDirectory.entrySet()) {
            Path seedDirectory = entry.getKey();
            Path metadataFile = entry.getValue();
            Path modelFile = findBenchmarkModelFile(seedDirectory);
            if (modelFile == null) {
                throw new IllegalStateException("No benchmark .prism/.pm model file found in " + seedDirectory);
            }

            Map<String, String> metadata = parseSimpleYaml(metadataFile);
            Path relativeSeedPath = benchmarkRoot.relativize(seedDirectory);
            Model model = parseBenchmarkModel(metadata, relativeSeedPath);
            if (model == null) {
                throw new IllegalStateException("Could not resolve benchmark model for seed directory " + seedDirectory + " from metadata " + metadataFile);
            }

            String parameterDirectoryName = readParameterDirectoryName(metadata, relativeSeedPath);
            Values parameterValues = parseParameterValues(parameterDirectoryName, true);
            Values identParameters = parseParameterValues(parameterDirectoryName, false);
            int seed = parseSeed(metadata, relativeSeedPath);

            instances.add(new BenchmarkInstance(
                    model,
                    parameterDirectoryName,
                    parameterValues,
                    identParameters,
                    seed,
                    modelFile,
                    metadata.get("Specification"),
                    metadata.get("RobustSpecification"),
                    metadata.get("OptimisticSpecification"),
                    metadata.get("DTMCSpecification"),
                    parseInteger(metadata.get("NumEpisodes")),
                    parseInteger(metadata.get("MaxTrajectoryLength")),
                    parseInteger(metadata.get("Multiplier")),
                    parseDouble(metadata.get("ErrorTolerance")),
                    parseBoolean(metadata.get("UseVertexPrecomp")),
                    parseBoolean(metadata.get("VerboseBisim"))
            ));
        }

        instances.sort(Comparator
                .comparing((BenchmarkInstance instance) -> instance.model.name())
                .thenComparing(instance -> instance.parameterDirectoryName)
                .thenComparingInt(instance -> instance.seed));
        return instances;
    }

    private static boolean isYamlFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return fileName.endsWith(".yaml") || fileName.endsWith(".yml");
    }

    private static Path findBenchmarkModelFile(Path seedDirectory) {
        try (Stream<Path> files = Files.list(seedDirectory)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.endsWith(".prism") || name.endsWith(".pm");
                    })
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to inspect benchmark seed directory " + seedDirectory, e);
        }
    }

    private static Map<String, String> parseSimpleYaml(Path yamlFile) {
        Map<String, String> metadata = new LinkedHashMap<>();
        List<String> lines;
        try {
            lines = Files.readAllLines(yamlFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read benchmark metadata file " + yamlFile, e);
        }

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || "ExperimentInfo:".equals(trimmed)) {
                continue;
            }

            int separatorIndex = trimmed.indexOf(':');
            if (separatorIndex < 0) {
                continue;
            }

            String key = trimmed.substring(0, separatorIndex).trim();
            String value = trimmed.substring(separatorIndex + 1).trim();
            metadata.put(key, value);
        }

        return metadata;
    }

    private static Model parseBenchmarkModel(Map<String, String> metadata, Path relativeSeedPath) {
        String modelName = metadata.get("Model");
        if ((modelName == null || modelName.isBlank()) && relativeSeedPath.getNameCount() >= 1) {
            modelName = relativeSeedPath.getName(0).toString();
        }

        if (modelName == null || modelName.isBlank()) {
            return null;
        }

        try {
            return Model.valueOf(modelName.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String readParameterDirectoryName(Map<String, String> metadata, Path relativeSeedPath) {
        String parameterValues = metadata.get("ParameterValues");
        if (parameterValues != null && !parameterValues.isBlank()) {
            return parameterValues.trim();
        }

        if (relativeSeedPath.getNameCount() >= 2) {
            return relativeSeedPath.getName(1).toString();
        }

        return "";
    }

    private static int parseSeed(Map<String, String> metadata, Path relativeSeedPath) {
        Integer seed = parseInteger(metadata.get("Seed"));
        if (seed != null) {
            return seed;
        }

        if (relativeSeedPath.getNameCount() >= 3) {
            Integer parsedSeed = parseInteger(relativeSeedPath.getName(2).toString());
            if (parsedSeed != null) {
                return parsedSeed;
            }
        }

        return 5;
    }

    private static Values parseParameterValues(String parameterString, boolean parseTypedValues) {
        Values values = new Values();
        if (parameterString == null || parameterString.isBlank()) {
            return values;
        }

        for (String entry : parameterString.split(",")) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            int separatorIndex = trimmed.indexOf('=');
            if (separatorIndex <= 0 || separatorIndex >= trimmed.length() - 1) {
                continue;
            }

            String parameterName = trimmed.substring(0, separatorIndex).trim();
            String rawValue = trimmed.substring(separatorIndex + 1).trim();
            Object value = parseTypedValues ? parseParameterValue(rawValue) : rawValue;
            values.addValue(parameterName, value);
        }

        return values;
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
            // Try double below.
        }

        try {
            return Double.parseDouble(rawValue);
        } catch (NumberFormatException ignored) {
            // Fall back to raw string below.
        }

        return rawValue;
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

    private static Double parseDouble(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        try {
            return Double.parseDouble(rawValue.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Boolean parseBoolean(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalizedValue = rawValue.trim();
        if ("true".equalsIgnoreCase(normalizedValue)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(normalizedValue)) {
            return Boolean.FALSE;
        }

        return null;
    }

    private static Experiment applyBenchmarkInstance(Experiment experiment, BenchmarkInstance benchmarkInstance) {
        experiment.modelFile = benchmarkInstance.modelFile.toString();
        experiment.certainModelFile = benchmarkInstance.modelFile.toString();
        experiment.parameterValues = new Values(benchmarkInstance.parameterValues);
        experiment.identParameters = new Values(benchmarkInstance.identParameters);
        experiment.seed = benchmarkInstance.seed;

        if (benchmarkInstance.spec != null && !benchmarkInstance.spec.isBlank()) {
            experiment.spec = benchmarkInstance.spec;
        }
        if (benchmarkInstance.robustSpec != null && !benchmarkInstance.robustSpec.isBlank()) {
            experiment.robustSpec = benchmarkInstance.robustSpec;
        }
        if (benchmarkInstance.optimisticSpec != null && !benchmarkInstance.optimisticSpec.isBlank()) {
            experiment.optimisticSpec = benchmarkInstance.optimisticSpec;
        }
        if (benchmarkInstance.dtmcSpec != null && !benchmarkInstance.dtmcSpec.isBlank()) {
            experiment.dtmcSpec = benchmarkInstance.dtmcSpec;
        }
        if (benchmarkInstance.iterations != null) {
            experiment.iterations = benchmarkInstance.iterations;
        }
        if (benchmarkInstance.maxEpisodeLength != null) {
            experiment.max_episode_length = benchmarkInstance.maxEpisodeLength;
        }
        if (benchmarkInstance.multiplier != null) {
            experiment.multiplier = benchmarkInstance.multiplier;
        }
        if (benchmarkInstance.errorTolerance != null) {
            experiment.error_tolerance = benchmarkInstance.errorTolerance;
            experiment.apsDelta = 1 - benchmarkInstance.errorTolerance;
        }
        if (benchmarkInstance.useVertexPrecomp != null) {
            experiment.useVertexPrecomp = benchmarkInstance.useVertexPrecomp;
        }
        if (benchmarkInstance.verboseBisim != null) {
            experiment.verboseBisim = benchmarkInstance.verboseBisim;
        }

        return experiment;
    }

    private void clearCachedSamples() {
        this.cachedSamplesMap = null;
        this.cachedSampleSizeMap = null;
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

    public Pair<List<List<IMDP<Double>>>, List<MDP<Double>>> solveIMDPUniform(Experiment ex, EstimatorConstructor estimatorConstructor, MDPSimple<Function> pmdp, Values parameterValuation, boolean verification) {
        return solveIMDPUniform(ex, estimatorConstructor, pmdp, parameterValuation, verification, null);
    }

    public Pair<List<List<IMDP<Double>>>, List<MDP<Double>>> solveIMDPUniform(Experiment ex, EstimatorConstructor estimatorConstructor, MDPSimple<Function> pmdp, Values parameterValuation, boolean verification, Integer timeoutSeconds) {
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
            Pair<ArrayList<DataPoint>, ArrayList<IMDP<Double>>> resIMDP = runSolvingUniform(ex, estimator, 0, verification, timeoutSeconds);
            double durationInSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;

            DataProcessor dp = new DataProcessor();

            dp.dumpExperimentMetaData(makeOutputDirectory(ex), makeLabel(ex), ex, durationInSeconds, estimator.getSulOpt(), pmdp.getNumStates(), pmdp.getNumTransitions(), resIMDP.first.size(), estimator.getNumLearnableComponents());
            dp.dumpDataRobustPolicies(makeOutputDirectory(ex), makeLabel(ex), resIMDP.first);

        } catch (PrismException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public Pair<ArrayList<DataPoint>, ArrayList<IMDP<Double>>> runSolvingUniform(Experiment ex, Estimator estimator, boolean verifcation) {
        return runSolvingUniform(ex, estimator, 0, verifcation, null);
    }

    public Pair<ArrayList<DataPoint>, ArrayList<IMDP<Double>>> runSolvingUniform(Experiment ex, Estimator estimator, int past_iterations, boolean verficiation) {
        return runSolvingUniform(ex, estimator, past_iterations, verficiation, null);
    }

    public Pair<ArrayList<DataPoint>, ArrayList<IMDP<Double>>> runSolvingUniform(Experiment ex, Estimator estimator, int past_iterations, boolean verficiation, Integer timeoutSeconds) {
        try {
            MDP<Double> SUL = estimator.getSUL();
            long timeoutDeadlineNanos = computeTimeoutDeadline(timeoutSeconds);
            boolean timeoutEnabled = timeoutDeadlineNanos != Long.MAX_VALUE;

            if (true/*this.modelStats == null*/) {
                System.out.println("======");
                System.out.println(ex.model);
                System.out.println("======");
                System.out.println(estimator.getModelStats());
            }

            if (this.cachedSamplesMap != null) {
                if (timeoutEnabled && hasTimedOut(timeoutDeadlineNanos)) {
                    System.out.println("Skipping solve because benchmark timeout reached before solving with cached samples (" + timeoutSeconds + "s).");
                    return new Pair<>(new ArrayList<>(), new ArrayList<>());
                }

                double[] currentResults;

                ArrayList<DataPoint> results = new ArrayList<>();
                ArrayList<UMDP<Double>> estimates = new ArrayList<>();

                estimator.setObservationMaps(cachedSamplesMap, cachedSampleSizeMap);

                long startTime = System.nanoTime();
                currentResults = estimator.getCurrentResults();
                long solvingTime = System.nanoTime() - startTime;

                if (this.verbose) System.out.println("Uniform Sampling and Solving");
                if (this.verbose) System.out.println("Episode " + ex.iterations + ".");
                if (this.verbose) System.out.println("Performance on unknown MDP (J): " + currentResults[1]);
                if (this.verbose) System.out.println("Performance Guarantee on learned UMDP (J̃): " + currentResults[0]);
                if (this.verbose) System.out.println("Solving Time: " + solvingTime / 1_000_000_000.0 + "s");
                if (this.verbose) System.out.println();

                results.add(new DataPoint(ex.iterations, ex.iterations, solvingTime, currentResults));

                return new Pair(results, estimates);
            }

            ObservationSampler observationSampler = new ObservationSampler(this.prism, SUL, estimator.getTerminatingStates());
            observationSampler.setTransitionsOfInterest(estimator.getTransitionsOfInterest());
            observationSampler.setTiedParameters(ex.tieParameters);
            observationSampler.setMultiplier(ex.multiplier);

            double[] currentResults;

            ArrayList<DataPoint> results = new ArrayList<>();
            ArrayList<UMDP<Double>> estimates = new ArrayList<>();

            int samples = 0;
            Strategy samplingStrategy = estimator.buildUniformStrat();
            boolean timeoutReported = false;

            for (int i = past_iterations; i < ex.iterations + past_iterations; i++) {
                if (timeoutEnabled && hasTimedOut(timeoutDeadlineNanos)) {
                    if (!timeoutReported) {
                        System.out.println("Timeout reached after " + (i - past_iterations) + " sampled episodes (limit " + timeoutSeconds + "s).");
                        timeoutReported = true;
                    }
                    break;
                }

                int sampled = observationSampler.simulateEpisode(ex.max_episode_length, samplingStrategy);
                samples += sampled;

                if (i % 1000 == 0) {
                    System.out.println("Sampled episodes: " + i + " of " + ex.iterations);
                }

                boolean timeoutReached = timeoutEnabled && hasTimedOut(timeoutDeadlineNanos);
                boolean last_iteration = i == ex.iterations + past_iterations - 1 || timeoutReached;
                if (last_iteration || ex.resultIteration(i)) {
                    estimator.setObservationMaps(observationSampler.getSamplesMap(), observationSampler.getSampleSizeMap());

                    this.cachedSamplesMap = observationSampler.getSamplesMap();
                    this.cachedSampleSizeMap = observationSampler.getSampleSizeMap();

                    long startTime = System.nanoTime();
                    currentResults = estimator.getCurrentResults();
                    long solvingTime = System.nanoTime() - startTime;

                    if (this.verbose) System.out.println("Uniform Sampling and Solving");
                    if (this.verbose) System.out.println("Episode " + i + ".");
                    if (this.verbose) System.out.println("Performance on unknown MDP (J): " + currentResults[1]);
                    if (this.verbose) System.out.println("Performance Guarantee on learned UMDP (J̃): " + currentResults[0]);
                    if (this.verbose) System.out.println("Solving Time: " + solvingTime / 1_000_000_000.0 + "s");
                    if (this.verbose) System.out.println();

                    results.add(new DataPoint(samples, i + 1, solvingTime, currentResults));

                    if (timeoutReached) {
                        if (!timeoutReported) {
                            System.out.println("Timeout reached after " + (i - past_iterations + 1) + " sampled episodes (limit " + timeoutSeconds + "s).");
                        }
                        break;
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

    private static long computeTimeoutDeadline(Integer timeoutSeconds) {
        if (timeoutSeconds == null || timeoutSeconds <= 0) {
            return Long.MAX_VALUE;
        }

        long timeoutNanos = TimeUnit.SECONDS.toNanos(timeoutSeconds.longValue());
        long now = System.nanoTime();
        if (Long.MAX_VALUE - now < timeoutNanos) {
            return Long.MAX_VALUE;
        }
        return now + timeoutNanos;
    }

    private static boolean hasTimedOut(long timeoutDeadlineNanos) {
        return timeoutDeadlineNanos != Long.MAX_VALUE && System.nanoTime() >= timeoutDeadlineNanos;
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
        boolean useIdentParameters = this.forceIdentParameterDirectory && ex.identParameters != null && ex.identParameters.getNumValues() > 0;
        String parameterDirectory = (useIdentParameters || ex.parameterValues.getNumValues() > 10)
                ? ex.identParameters.toString()
                : ex.parameterValues.toString();
        String outputPath = Paths.get(this.outputRoot, ex.model.toString(), parameterDirectory, String.valueOf(ex.seed)).toString() + "/";
        try {
            Files.createDirectories(Paths.get(outputPath));
            copyExperimentPrismFile(ex, outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputPath;
    }

    private void copyExperimentPrismFile(Experiment ex, String outputPath) throws IOException {
        if (ex.modelFile == null || ex.modelFile.isBlank()) {
            return;
        }

        Path source = Paths.get(ex.modelFile).normalize();
        if (!Files.exists(source)) {
            return;
        }

        Path target = Paths.get(outputPath).resolve(source.getFileName());
        if (Files.exists(target)) {
            return;
        }

        Files.copy(source, target);
    }
}
