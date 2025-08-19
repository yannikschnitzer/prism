package imdpcomp;

import explicit.Model;
import explicit.*;
import learning.CommandLine;
import param.Function;
import parser.Values;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.*;
import simulator.ModulesFileModelGenerator;
import strat.MDStrategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import static explicit.ConstructModel.CompositionType.*;
import static imdpcomp.SolvingExperiment.Model.*;

@CommandLine.Command(mixinStandardHelpOptions = true, version = "AAAI V-0.0.1", description = "Compositional Solver for AAAI")
public class ExperimentRunner implements Callable<Integer> {

    Prism prism;

    @CommandLine.Option(names = {"-c", "--casestudy"}, description = "Run a specific case study - \"aircraft\", \"betting\", \"sav\", \"chain\", \"drone\", \"lake\"")
    private String casestudy = "aircraft";

    @CommandLine.Option(names = {"-o", "--composition"}, description = "Run a specific IMDP learning algorhtm - \"smart\", \"vertex\", \"interval\", \"all\"")
    private String composition = "smart";

    @CommandLine.Option(names = {"-e", "--eps"}, description = "Set epsilon")
    private Double epsilon = 0.02;

    @CommandLine.Option(names = {"-nd", "--nodtmc"}, description = "Do not compute DTMC value")
    private boolean nodtmc = false;

    @CommandLine.Option(names = {"-no", "--nooptimistic"}, description = "Do not compute optimistic value")
    private boolean noopt = false;

    public ExperimentRunner() {
        try {
            this.prism = new Prism(new PrismPrintStreamLog(System.out));
            prism.setVerbose(true);
            prism.initialise();
            prism.setEngine(Prism.EXPLICIT);
            prism.setGenStrat(true);

        } catch (PrismException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main2(String[] args) {
        Prism prism = new Prism(new PrismPrintStreamLog(System.out));
        try {
            prism.setVerbose(true);
            prism.initialise();
            prism.setEngine(Prism.EXPLICIT);
            prism.setGenStrat(true);

            ModulesFile modulesFile = prism.parseModelFile(new File("../models/aircraft_collision/aircraft_3.prism"));
            //ModulesFile modulesFile = prism.parseModelFile(new File("../models/aircraft_collision/aircraft_4_overshoot.prism"));
            //ModulesFile modulesFile = prism.parseModelFile(new File("../models/grid_world_robot/grid_robot_1.prism"));
            //ModulesFile modulesFile = prism.parseModelFile(new File("../models/blocks_world/block_epistemic.prism"));
            //ModulesFile modulesFile = prism.parseModelFile(new File("../models/imdp_comp_test.prism"));
            prism.loadPRISMModel(modulesFile);
            prism.buildModel(INTERVAL_PRODUCT);


            UMDPSimple<Double> umdp = (UMDPSimple<Double>) prism.getBuiltModelExplicit();
            //System.out.println(umdp);

            UMDPModelChecker mc = new UMDPModelChecker(null);
            mc.setPrecomp(true);
            mc.setGenStrat(true);

            BitSet target = new BitSet();
            target.set(1);
            target.set(2);
            target.set(3);
            target.set(4);
            target.set(5);
            target.set(12);
            ModelCheckerResult res;
            //umdp.findDeadlocks(true);

            boolean min = true;

//			res = mc.computeReachProbs(umdp, target, MinMax.max().setMinUnc(min));
//			System.out.println((min ? "maxmin: " : "maxmax: ") + res.soln[0]);

            //String robustSpec = "Pmaxmin=? [!\"collision\" U \"goal\"]";
            String robustSpec = "Pmaxmin=? [!\"collision\" U \"goal\"]";
            //String robustSpec = "Rminmax=?[F \"goal\"];";
            PropertiesFile pf = prism.parsePropertiesString(robustSpec);
            ModulesFileModelGenerator<?> modelGen = ModulesFileModelGenerator.create(modulesFile, prism);
            mc.setModelCheckingInfo(modelGen, pf, modelGen);
            Result result = mc.check(umdp, pf.getProperty(0));
            System.out.println((min ? "maxmin: " : "maxmax: ") + result.getResultString());
            System.out.println("Strategy:" + result.getStrategy());

        } catch (PrismException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public static void ma3in(String[] args) {
        if (args.length > 0) {
            int exitCode = new CommandLine(new ExperimentRunner()).execute(args);
            System.exit(exitCode);
        } else {
            System.out.println("No Arguments Provided");
        }
    }

    @Override
    public Integer call() throws Exception {
        SolvingExperiment ex;
        switch (this.casestudy) {
            case "aircraft" -> {
                ex = new SolvingExperiment(AIRCRAFT_MULTI_SLIP);
            }
            case "lake" -> {
                ex = new SolvingExperiment(LAKE_SWARM);
            }
            case "lakemulti" -> {
                ex = new SolvingExperiment(LAKE_SWARM_MULTI_SLIP);
            }
            case "drone" -> {
                ex = new SolvingExperiment(DRONE_MULTI);
            }
            case "drone2" -> {
                ex = new SolvingExperiment(DRONE_MULTI_2);
            }
            case "chain" -> {
                ex = new SolvingExperiment(CHAIN_MULTI_SINGLE);
            }
            case "herman" -> {
                ex = new SolvingExperiment(HERMAN_3);
            }
            case "sysadmin" -> {
                ex = new SolvingExperiment(SYSADMIN);
            }
            case "stocktrading" -> {
                ex = new SolvingExperiment(STOCK_TRADING);
            }
            default -> {
                ex = new SolvingExperiment(AIRCRAFT);
            }
        }

        ex.setSingleValue("eps", epsilon);

        switch (this.composition) {
            case "smart" -> {
                ex.compositionType = SMART;
                this.runExperiment(ex);
            }
            case "vertex" -> {
                ex.compositionType = VERTEX;
                this.runExperiment(ex);
            }
            case "interval" -> {
                ex.compositionType = INTERVAL_PRODUCT;
                this.runExperiment(ex);
            }
            case "all" -> {
                this.runExperimentAllTypes(ex);
            }
            default -> {}
        }


        System.out.println("Done");
        return 0;
    }


    public static void main(String[] args) {
        ExperimentRunner experimentRunner = new ExperimentRunner();
        SolvingExperiment experiment = new SolvingExperiment(LAKE_SWARM_MULTI_SLIP);

        try {
            experimentRunner.runExperimentAllTypes(experiment);
        } catch (PrismException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void runExperimentAllValues(SolvingExperiment experiment, List<Values> valueList) throws PrismException, FileNotFoundException {
        for (Values value : valueList) {
            runExperimentAllTypes(experiment.setValues(value));
        }
    }

    public void runExperimentAllTypes(SolvingExperiment experiment) throws PrismException, FileNotFoundException {
        runExperiment(experiment.setCompositonType(INTERVAL_PRODUCT));
        //runExperiment(experiment.setCompositonType(LINFINITY));
       // runExperiment(experiment.setCompositonType(SMART));
        //runExperiment(experiment.setCompositonType(VERTEX));
    }

    public void runExperiment(SolvingExperiment experiment) throws PrismException, FileNotFoundException {
        // Build model
        ModulesFile modulesFile = prism.parseModelFile(new File(experiment.modelFile));
        prism.loadPRISMModel(modulesFile);
        if (experiment.parameterValues != null) {
            prism.setPRISMModelConstants(experiment.parameterValues);
        }
        prism.buildModel(experiment.compositionType);
        UMDPSimple<Double> umdp = (UMDPSimple<Double>) prism.getBuiltModelExplicit();

        // Init UMDP Model Checker
        UMDPModelChecker mc = new UMDPModelChecker(null);
        mc.setPrecomp(true);
        mc.setGenStrat(true);
        mc.setErrorOnNonConverge(true);

        // Set Objective for robust check
        PropertiesFile pf = prism.parsePropertiesString(experiment.robustSpec);
        ModulesFileModelGenerator<?> modelGen = ModulesFileModelGenerator.create(modulesFile, prism);
        mc.setModelCheckingInfo(modelGen, pf, modelGen);
        double timer = System.currentTimeMillis();
        Result resultUMDProbust = mc.check(umdp, pf.getProperty(0));
        double timerrobust = System.currentTimeMillis() - timer;
        //System.out.println("Strategy:" + result.getStrategy());

        // Set Objective for optimistic check
        pf = prism.parsePropertiesString(experiment.optimisticSpec);
        modelGen = ModulesFileModelGenerator.create(modulesFile, prism);
        mc.setModelCheckingInfo(modelGen, pf, modelGen);
        timer = System.currentTimeMillis();
        Result resultUMDPoptimistic = noopt ? null : mc.check(umdp, pf.getProperty(0));
        double timeroptimistic = System.currentTimeMillis() - timer;

        Result resultDTMC = nodtmc ? null : checkInducedDTMC(experiment, (MDStrategy<Double>) resultUMDProbust.getStrategy());
        dumpExperiment(experiment, umdp, resultUMDProbust, resultUMDPoptimistic, resultDTMC, timerrobust, timeroptimistic);
    }

    public Result checkInducedDTMC(SolvingExperiment experiment, MDStrategy<Double> strat) throws PrismException, FileNotFoundException {
        // Build model
        ModulesFile modulesFile = prism.parseModelFile(new File(experiment.certainModelFile));
        prism.loadPRISMModel(modulesFile);
        if (experiment.parameterValues != null) {
            prism.setPRISMModelConstants(experiment.parameterValues);
        }

        // Build induced DTMC
        prism.buildModel(experiment.compositionType);

        MDPExplicit<Double> mdp = (MDPExplicit<Double>) prism.getBuiltModelExplicit();
        DTMCExplicit<Double> dtmc = (DTMCExplicit<Double>) mdp.constructInducedModel(strat);

        // Model check DTMC to get true performance of robust policy
        System.out.println("Building DTMC");
        DTMCModelChecker mc = new DTMCModelChecker(this.prism);
        mc.setPrecomp(false);
        mc.setErrorOnNonConverge(false);
        mc.setTermCritParam(1e-5);

        PropertiesFile pf = prism.parsePropertiesString(experiment.dtmcSpec);

        ModulesFile modulesFileDTMC = (ModulesFile) modulesFile.deepCopy();
        modulesFileDTMC.setModelType(ModelType.DTMC);
        ModulesFileModelGenerator<?> modelGen = ModulesFileModelGenerator.create(modulesFileDTMC, this.prism);
        mc.setModelCheckingInfo(modelGen, pf, modelGen);

        Result result = mc.check(dtmc, pf.getProperty(0));

        return result;
    }

    public void dumpExperiment(SolvingExperiment experiment, Model<Double> model, Result resultUMDProbust, Result resultUMDPoptimistic, Result resultDTMC, double timerRobust, double timeroptimistic) {
        String outputPath = String.format("results/%s/%s/", experiment.model, experiment.parameterValues);
        try {
            Files.createDirectories(Paths.get(outputPath));

            String file_name = experiment.model + "_" + experiment.compositionType + "_" + experiment.parameterValues;

            FileWriter writer = new FileWriter(outputPath + file_name + ".yaml");
            writer.write("Model: " + experiment.model + "\n");
            writer.write("Model File: " + experiment.modelFile + "\n");
            writer.write("State Space: " + model.getNumStates() + "\n");
            writer.write("Transitions: " + model.getNumTransitions() + "\n");
            writer.write("Constant Values: " + experiment.parameterValues + "\n");
            try {
                writer.write("Epsilon: " + experiment.parameterValues.getValueOf("eps") + "\n");
            } catch (PrismLangException e) {
                throw new RuntimeException(e);
            }
            writer.write("Composition Type: " + experiment.compositionType + "\n");
            writer.write("Robust Goal: " + experiment.robustSpec + "\n");
            writer.write("Robust Result: " + resultUMDProbust.getResult() + "\n");
            writer.write("VI Iterations: " + resultUMDProbust.getNumIters() + "\n");
            writer.write("Optimistic Goal: " + experiment.optimisticSpec + "\n");
            writer.write("Optimistic Result: " + ((resultUMDPoptimistic != null) ?  resultUMDPoptimistic.getResult() : "n/a") + "\n");
            writer.write("VI Iterations Optimistic: " + ((resultUMDPoptimistic != null) ?  resultUMDPoptimistic.getNumIters() : "n/a") + "\n");
            writer.write("DTMC Goal: " + experiment.dtmcSpec + "\n");
            writer.write("DTMC Result: " + ((resultDTMC != null) ?  resultDTMC.getResult() : "n/a") + "\n");
            writer.write("Runtime Robust: " + timerRobust / 1000 + "s \n");
            writer.write("Runtime Optimistic: " + timeroptimistic / 1000 + "s \n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Dump experiment setting to " + outputPath);
    }

}
