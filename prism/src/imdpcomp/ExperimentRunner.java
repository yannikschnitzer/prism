package imdpcomp;

import explicit.Model;
import explicit.*;
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
import java.util.BitSet;
import java.util.List;

import static explicit.ConstructModel.CompositionType.*;

public class ExperimentRunner {

    Prism prism = new Prism(new PrismPrintStreamLog(System.out));

    public ExperimentRunner() {
        try {
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

            ModulesFile modulesFile = prism.parseModelFile(new File("../models/aircraft_collision/aircraft_10x20_resolution_3.prism"));
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

    public static void main(String[] args) {
        ExperimentRunner experimentRunner = new ExperimentRunner();
        Experiment experiment = new Experiment(Experiment.Model.CHAIN_MULTI);

        try {
            experimentRunner.runExperimentAllTypes(experiment);
        } catch (PrismException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void runExperimentAllValues(Experiment experiment, List<Values> valueList) throws PrismException, FileNotFoundException {
        for (Values value : valueList) {
            runExperimentAllTypes(experiment.setValues(value));
        }
    }

    public void runExperimentAllTypes(Experiment experiment) throws PrismException, FileNotFoundException {
        runExperiment(experiment.setCompositonType(INTERVAL_PRODUCT));
        runExperiment(experiment.setCompositonType(SMART));
        runExperiment(experiment.setCompositonType(VERTEX));
    }

    public void runExperiment(Experiment experiment) throws PrismException, FileNotFoundException {
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

        // Set Objective
        PropertiesFile pf = prism.parsePropertiesString(experiment.robustSpec);
        ModulesFileModelGenerator<?> modelGen = ModulesFileModelGenerator.create(modulesFile, prism);
        mc.setModelCheckingInfo(modelGen, pf, modelGen);
        double timer = System.currentTimeMillis();
        Result resultUMDP = mc.check(umdp, pf.getProperty(0));
        timer = System.currentTimeMillis() - timer;
        //System.out.println("Strategy:" + result.getStrategy());

        Result resultDTMC = null;//checkInducedDTMC(experiment, (MDStrategy<Double>) resultUMDP.getStrategy());
        dumpExperiment(experiment, umdp, resultUMDP, resultDTMC, timer);
    }

    public Result checkInducedDTMC(Experiment experiment, MDStrategy<Double> strat) throws PrismException, FileNotFoundException {
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
        DTMCModelChecker mc = new DTMCModelChecker(this.prism);
        mc.setPrecomp(false);

        PropertiesFile pf = prism.parsePropertiesString(experiment.dtmcSpec);

        ModulesFile modulesFileDTMC = (ModulesFile) modulesFile.deepCopy();
        modulesFileDTMC.setModelType(ModelType.DTMC);
        ModulesFileModelGenerator<?> modelGen = ModulesFileModelGenerator.create(modulesFileDTMC, this.prism);
        mc.setModelCheckingInfo(modelGen, pf, modelGen);

        Result result = mc.check(dtmc, pf.getProperty(0));

        return result;
    }

    public void dumpExperiment(Experiment experiment, Model<Double> model, Result resultUMDP, Result resultDTMC, double timer) {
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
            writer.write("Composition Type: " + experiment.compositionType + "\n");
            writer.write("Robust Goal: " + experiment.robustSpec + "\n");
            writer.write("Robust Result: " + resultUMDP.getResult() + "\n");
            writer.write("VI Iterations: " + resultUMDP.getNumIters() + "\n");
            writer.write("DTMC Goal: " + experiment.dtmcSpec + "\n");
            writer.write("DTMC Result: " + ((resultDTMC != null) ?  resultDTMC.getResult() : "n/a") + "\n");
            writer.write("Runtime: " + timer / 1000 + "s \n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Dump experiment setting to " + outputPath);
    }
}
