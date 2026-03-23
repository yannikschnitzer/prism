package learning.Data;

import imdpcomp.Experiment;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DataProcessor {

    public void dumpDataRobustPolicies(String directoryPath, String name, ArrayList<DataPoint> dataPoints) {
        try {
            String path = directoryPath + name + ".csv";

            FileWriter writer = new FileWriter(path, false);

            writer.write("Episode,Robust Guarantee UMDP,Performance UMDP Robust Policy,Performance UMDP Optimistic Policy,Optimistic Guarantee UMDP,Total Runtime,Model Building Time,Model Checking Time Robust,Model Checking Time Optimistic,Model Checking Time DTMC,UDTMC Result Optimal Policy Robust,UDTMC Result Optimal Polciy Optimistic");
            writer.write(System.lineSeparator());

            for (DataPoint entry : dataPoints) {
                String row = entry.getEpisode() + ","
                        + entry.getEstimated_robust_value_umdp() + ","
                        + entry.getValue_umdp_robust_policy() + ","
                        + entry.getValue_umdp_optimistic_policy() + ","
                        + entry.getEstimated_optimistic_value_umdp() + ","
                        + entry.getTimeSeconds() + ","
                        + entry.getModelBuildingTime() + ","
                        + entry.getModelCheckingTimeRobust() + ","
                        + entry.getModelCheckingTimeOptimistic() + ","
                        + entry.getModelCheckingTimeDTMC() + "," +
                        + entry.getUncOptimalPolicyresultRobust() + ","
                        + entry.getUncOptimalPolicyresultOptimistic();
                writer.write(row + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void dumpExperimentMetaData(String directoryPath, String name, Experiment ex, double durationInSeconds, double sulOpt, int numStates, int numTransitions, int recomputations, int numLearnableComponents) {
        try {
            String path = directoryPath + name + ".yaml";

            FileWriter writer = new FileWriter(path, false);
            writer.write("ExperimentInfo:" + System.lineSeparator());

            writer.write("Model: " + ex.model + System.lineSeparator());
            writer.write("NumStates: " + numStates + System.lineSeparator());
            writer.write("NumTransitions: " + numTransitions + System.lineSeparator());
            writer.write("NumLearnableComponents: " + numLearnableComponents + System.lineSeparator());
            writer.write("NumParameters: " + ex.parameterValues.getNames().size() + System.lineSeparator());
            writer.write("Seed: " + ex.seed + System.lineSeparator());
            writer.write("Type: " + ex.type + System.lineSeparator());
            writer.write("Factored: " + ex.factored + System.lineSeparator());
            writer.write("CompositionType: " + ex.compositionType + System.lineSeparator());
            writer.write("ParameterTying: " + ex.tieParameters + System.lineSeparator());
            writer.write("ParameterValues: " + ex.parameterValues + System.lineSeparator());
            writer.write("ErrorTolerance: " + ex.error_tolerance + System.lineSeparator());
            writer.write("UseVertexPrecomp: " + ex.useVertexPrecomp + System.lineSeparator());
            writer.write("VerboseBisim: " + ex.verboseBisim + System.lineSeparator());
            writer.write("Specification: " + ex.spec + System.lineSeparator());
            writer.write("RobustSpecification: " + ex.robustSpec + System.lineSeparator());
            writer.write("OptimisticSpecification: " + ex.optimisticSpec + System.lineSeparator());
            writer.write("DTMCSpecification: " + ex.dtmcSpec + System.lineSeparator());
            writer.write("TrueOptimum: " + sulOpt + System.lineSeparator());
            writer.write("Multiplier: " + ex.multiplier + System.lineSeparator());
            writer.write("NumEpisodes: " + ex.iterations + System.lineSeparator());
            writer.write("MaxTrajectoryLength: " + ex.max_episode_length + System.lineSeparator());
            writer.write("ModelFile: " + ex.certainModelFile + System.lineSeparator());
            writer.write("StrategyWeight: " + ex.strategyWeight + System.lineSeparator());
            writer.write("Recomputations: " + recomputations + System.lineSeparator());
            writer.write("Runtime: " + durationInSeconds + System.lineSeparator());


            writer.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }


}
