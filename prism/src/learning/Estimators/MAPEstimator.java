package learning.Estimators;

import common.Interval;
import explicit.*;
import imdpcomp.Experiment;
import learning.Simulation.StateActionPair;
import learning.Simulation.TransitionTriple;
import org.apache.commons.lang3.NotImplementedException;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.*;
import simulator.ModulesFileModelGenerator;
import strat.MDStrategy;
import strat.Strategy;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;


public class MAPEstimator extends Estimator {
    protected HashMap<TransitionTriple, Integer> dirichletPriorsMap;
    protected HashMap<StateActionPair, HashSet<Integer>> successorStatesMap;

    // Cache for MAP mode denominator: sum of Dirichlet priors minus one per transition
    private final TransitionTriple keyTriple = new TransitionTriple(0, "", 0);


    public MAPEstimator(Prism prism, Experiment ex) {
        super(prism, ex);
        this.dirichletPriorsMap = new HashMap<>();
        this.successorStatesMap = new HashMap<>();
        this.setPriors(2);
        this.name = "MAP";
    }

    public void setIntervalsMap(HashMap<TransitionTriple, Interval<Double>> im) {
        this.intervalsMap = im;
    }

    public void setPriors(int alpha) {
        int numStates = mdp.getNumStates();
        for (int s = 0; s < numStates; s++) {
            final int state = s;
            int numChoices = mdp.getNumChoices(s);
            for (int i = 0; i < numChoices; i++) {
                final String action = getActionString(mdp, s, i);
                final StateActionPair sa = new StateActionPair(state, action);
                HashSet<Integer> successors = new HashSet<>();
                mdp.forEachDoubleTransition(s, i, (int sFrom, int sTo, double p) -> {
                    if (p != 0.0) {
                        final TransitionTriple t = new TransitionTriple(state, action, sTo);
                        successors.add(sTo);
                        this.dirichletPriorsMap.put(t, alpha);
                    }
                });
                this.successorStatesMap.put(sa, successors);
            }
        }
    }

    public double mode(TransitionTriple t) {
        int num = dirichletPriorsMap.get(t) - 1;
        int denum = 0;
        int count = 0;
        StateActionPair sa = t.getStateAction();
        HashSet<Integer> successors = successorStatesMap.get(sa);
        for (int successor : successors) {
            //System.out.println(alpha);
            keyTriple.setAll(sa.getState(), sa.getAction(), successor);
            denum += dirichletPriorsMap.get(keyTriple);
            count += 1;
        }
        denum -= count;


        //System.out.println("num = " + num);
        //System.out.println("denum = " + denum);
        return (double) num / (double) denum;
    }

    public Double modeTied(TransitionTriple t) {
        StateActionPair sa = t.getStateAction();
        return (double) this.samplesMap.get(t) / (double) this.sampleSizeMap.get(sa);
    }

    public int getTransitionCount(TransitionTriple t) {
        return dirichletPriorsMap.get(t);
    }

    public int getTotalTransitionCount() {
        int count = 0;
        for (TransitionTriple t : dirichletPriorsMap.keySet()) {
            count += getTransitionCount(t);
        }
        return count;
    }

    public int getStateActionCount(StateActionPair sa) {
        int count = 0;
        HashSet<Integer> successors = successorStatesMap.get(sa);
        for (int successor : successors) {
            keyTriple.setAll(sa.getState(), sa.getAction(), successor);
            count += dirichletPriorsMap.get(keyTriple);
        }
        return count;
    }


    public void updatePriors() {
        boolean needsNormalization = false;
        for (TransitionTriple t : this.dirichletPriorsMap.keySet()) {
            if (this.samplesMap.containsKey(t)) {
                this.dirichletPriorsMap.put(t, this.dirichletPriorsMap.get(t) + this.samplesMap.get(t));
            }
        }
    }

    public double[] getCurrentResults() throws PrismException {
        updatePriors();
        Result resultRobust;
        Result resultOptimistic;

        long startTime;
        long modelBuildingTime;
        long modelCheckingTimeRobust;
        long modelCheckingTimeOptimistic;
        long modelCheckingTimeDTMC;

        if (ex.factored) {
            startTime = System.nanoTime();
            buildMarginalUMDP(mdp);
            modelBuildingTime = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            resultRobust = modelCheckMarginalEstimate(true, true);
            modelCheckingTimeRobust = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            resultOptimistic = modelCheckMarginalEstimate(false, true);
            modelCheckingTimeOptimistic = System.nanoTime() - startTime;

            // Reset Marginal Estimate and request low priority garbage collection
            this.marginalEstimate = null;
            Executors.newSingleThreadExecutor().submit(System::gc);

        } else {
            startTime = System.nanoTime();
            buildPointIMDP(mdp);
            modelBuildingTime = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            resultRobust = modelCheckPointEstimate(true, true);
            modelCheckingTimeRobust = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            resultOptimistic = modelCheckPointEstimate(false, true);
            modelCheckingTimeOptimistic = System.nanoTime() - startTime;
        }

        double resultRobustMDP = round((Double) resultRobust.getResult());
        MDStrategy<Double> robustStrat = (MDStrategy<Double>) resultRobust.getStrategy();
        MDStrategy<Double> optimisticStrat = (MDStrategy<Double>) resultOptimistic.getStrategy();
        this.currentStrat = optimisticStrat;

        startTime = System.nanoTime();
        double resultRobustDTMC = round((Double) checkDTMC(robustStrat).getResult());
        modelCheckingTimeDTMC = System.nanoTime() - startTime;

        double resultOptimisticDTMC = round((Double) checkDTMC(optimisticStrat).getResult());

        return new double[]{resultRobustMDP, resultRobustDTMC, resultOptimisticDTMC, modelBuildingTime, modelCheckingTimeRobust, modelCheckingTimeOptimistic, modelCheckingTimeDTMC};
    }

    @Override
    public double averageDistanceToSUL() {
        double totalDist = 0.0;

        for (TransitionTriple t : super.trueProbabilitiesMap.keySet()) {
            double value = mode(t);
            double p = super.trueProbabilitiesMap.get(t);
            double dist = Math.abs(value - p);
            totalDist += dist;
        }
        return totalDist / super.trueProbabilitiesMap.size();
    }


    public Result checkDTMC(MDStrategy strat) throws PrismException {
        MDPExplicit<Double> mdp = (MDPExplicit<Double>) this.prism.getBuiltModelExplicit();
        DTMC<Double> dtmc = (DTMC<Double>) mdp.constructInducedModel(strat);
        DTMCModelChecker mc = new DTMCModelChecker(this.prism);
        //mc.setPrecomp(false); //TODO: here
        mc.setErrorOnNonConverge(false);
        mc.setMaxIters(ex.maxVIIters);
        mc.setTermCritParam(1e-4);
        mc.setGenStrat(true);
        PropertiesFile pf = prism.parsePropertiesString(ex.dtmcSpec);

        ModulesFile modulesFileDTMC = (ModulesFile) modulesFileIMDP.deepCopy();
        modulesFileDTMC.setModelType(ModelType.DTMC);
        ModulesFileModelGenerator<?> modelGen = ModulesFileModelGenerator.create(modulesFileDTMC, this.prism);
        modelGen.setSomeUndefinedConstants(mdp.getConstantValues());
        //RewardGeneratorMDStrat<?> rewGen = new RewardGeneratorMDStrat(modelGen, mdp, strat);

        mc.setModelCheckingInfo(modelGen, pf, modelGen);
        Result result = mc.check(dtmc, pf.getProperty(0));
        return result;
    }


    public Result getInitialResult(boolean verbose) throws PrismException {
        buildPointIMDP(mdp);
        return modelCheckPointEstimate(true, false);
    }

    public double[] getInitialResults() throws PrismException {
        Result resultRobust;
        Result resultOptimistic;

        long startTime;
        long modelBuildingTime;
        long modelCheckingTimeRobust;
        long modelCheckingTimeOptimistic;
        long modelCheckingTimeDTMC;

        if (ex.factored) {
            startTime = System.nanoTime();
            buildMarginalUMDP(mdp);
            modelBuildingTime = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            resultRobust = modelCheckMarginalEstimate(true, true);
            modelCheckingTimeRobust = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            resultOptimistic = modelCheckMarginalEstimate(false, true);
            modelCheckingTimeOptimistic = System.nanoTime() - startTime;
        } else {
            startTime = System.nanoTime();
            buildPointIMDP(mdp);
            modelBuildingTime = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            resultRobust = modelCheckPointEstimate(true, true);
            modelCheckingTimeRobust = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            resultOptimistic = modelCheckPointEstimate(false, true);
            modelCheckingTimeOptimistic = System.nanoTime() - startTime;
        }

        double resultRobustMDP = round((Double) resultRobust.getResult());
        MDStrategy<Double> robustStrat = (MDStrategy<Double>) resultRobust.getStrategy();
        MDStrategy<Double> optimisticStrat = (MDStrategy<Double>) resultOptimistic.getStrategy();
        this.currentStrat = optimisticStrat;

        startTime = System.nanoTime();
        double resultRobustDTMC = round((Double) checkDTMC(robustStrat).getResult());
        modelCheckingTimeDTMC = System.nanoTime() - startTime;

        double resultOptimisticDTMC = round((Double) checkDTMC(optimisticStrat).getResult());

        return new double[]{resultRobustMDP, resultRobustDTMC, resultOptimisticDTMC, modelBuildingTime, modelCheckingTimeRobust, modelCheckingTimeOptimistic, modelCheckingTimeDTMC};
    }

    /**
     * Builds a point estimate IMDP of point intervals with laplace smoothing for the parameter epsilon
     *
     * @param mdp MDP for the underlying state space
     * @return IMDP of point intervals
     */
    public UMDP<Double> buildPointIMDP(MDP<Double> mdp) {
        //System.out.println("Building IMDP");
        int numStates = mdp.getNumStates();
        IMDPSimple<Double> imdp = new IMDPSimple<>(numStates);
        imdp.addInitialState(mdp.getFirstInitialState());
        imdp.setStatesList(mdp.getStatesList());
        imdp.setConstantValues(mdp.getConstantValues());
        imdp.setIntervalEvaluator(Evaluator.forDoubleInterval());

        for (int s = 0; s < numStates; s++) {
            int numChoices = mdp.getNumChoices(s);
            final int state = s;
            for (int i = 0; i < numChoices; i++) {
                final String action = getActionString(mdp, s, i);

                Distribution<Interval<Double>> distrNew = new Distribution<>(Evaluator.forDoubleInterval());
                mdp.forEachDoubleTransition(s, i, (int sFrom, int sTo, double p) -> {
                    TransitionTriple t = new TransitionTriple(state, action, sTo);
                    Interval<Double> interval;
                    if (0 < p && p < 1.0) {
                        interval = getTransitionInterval(t);
                        //System.out.println("Triple: " + t + " Interval: " + interval);
                        distrNew.add(sTo, interval);
                        this.intervalsMap.put(t, interval);
                    } else if (p == 1.0) {
                        interval = new Interval<Double>(p, p);
                        distrNew.add(sTo, interval);
                        this.intervalsMap.put(t, interval);
                    }
                });
                imdp.addActionLabelledChoice(s, distrNew, getActionString(mdp, s, i));
            }
        }
        Map<String, BitSet> labels = mdp.getLabelToStatesMap();
        Iterator<Entry<String, BitSet>> it = labels.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, BitSet> entry = it.next();
            imdp.addLabel(entry.getKey(), entry.getValue());
        }
        this.estimate = imdp;

        return imdp;
    }

    protected Interval<Double> getTransitionInterval(TransitionTriple t) {
        double point = mode(t);
        return new Interval<>(point, point);
    }

    public UMDP<Double> buildMarginalUMDP(MDP<Double> mdp) {
        throw new NotImplementedException("Only implemented for PAC UMDP Learning");
    }

    /**
     * Model check the point estimate stored in the class
     *
     * @return Result
     * @throws PrismException
     */
    public Result modelCheckPointEstimate(boolean robust, boolean verbose) throws PrismException {
        UMDPModelChecker mc = new UMDPModelChecker(this.prism);
        mc.setGenStrat(true);
        mc.setPrecomp(true);
        mc.setMaxIters(ex.maxVIIters);
        mc.setTermCritParam(1e-4);
        mc.setErrorOnNonConverge(false);

        PropertiesFile pf;
        if (robust)
            pf = prism.parsePropertiesString(ex.robustSpec);
        else
            pf = prism.parsePropertiesString(ex.optimisticSpec);

        ModulesFileModelGenerator<?> modelGen = ModulesFileModelGenerator.create(modulesFileIMDP, this.prism);
        modelGen.setSomeUndefinedConstants(this.estimate.getConstantValues());
        mc.setModelCheckingInfo(modelGen, pf, modelGen);
        Result result = mc.check(this.estimate, pf.getProperty(0));
        if (verbose) {
            System.out.println("\nModel checking point estimate MDP:");
            System.out.println((robust ? ex.robustSpec : ex.optimisticSpec) + " : " + result.getResultAndAccuracy());
        }
        return result;
    }

    /**
     * Model check the point estimate stored in the class
     *
     * @return Result
     * @throws PrismException
     */
    public Result modelCheckPointEstimate(UMDP<Double> estimate, boolean robust, boolean verbose) throws PrismException {
        UMDPModelChecker mc = new UMDPModelChecker(this.prism);
        mc.setGenStrat(true);
        mc.setMaxIters(ex.maxVIIters);
        mc.setTermCritParam(1e-4);
        mc.setErrorOnNonConverge(false);

        PropertiesFile pf;
        if (robust)
            pf = prism.parsePropertiesString(ex.robustSpec);
        else
            pf = prism.parsePropertiesString(ex.optimisticSpec);

        ModulesFileModelGenerator<?> modelGen = ModulesFileModelGenerator.create(modulesFileIMDP, this.prism);
        modelGen.setSomeUndefinedConstants(estimate.getConstantValues());
        mc.setModelCheckingInfo(modelGen, pf, modelGen);
        Result result = mc.check(estimate, pf.getProperty(0));

        if (verbose) {
            System.out.println("\nModel checking point estimate MDP:");
            System.out.println(ex.robustSpec + " : " + result.getResultAndAccuracy());
        }

        return result;
    }

    /**
     * Model check the marginal estimate stored in the class
     */
    public Result modelCheckMarginalEstimate(boolean robust, boolean verbose) throws PrismException {
        UMDPModelChecker mc = new UMDPModelChecker(this.prism);
        mc.setGenStrat(true);
        mc.setPrecomp(true);
        mc.setMaxIters(ex.maxVIIters);
        mc.setErrorOnNonConverge(false);

        PropertiesFile pf;
        if (robust)
            pf = prism.parsePropertiesString(ex.robustSpec);
        else
            pf = prism.parsePropertiesString(ex.optimisticSpec);

        ModulesFileModelGenerator<?> modelGen = ModulesFileModelGenerator.create(modulesFileIMDP, this.prism);
        modelGen.setSomeUndefinedConstants(this.marginalEstimate.getConstantValues());
        mc.setModelCheckingInfo(modelGen, pf, modelGen);
        Result result = mc.check(this.marginalEstimate, pf.getProperty(0));
        if (verbose) {
            System.out.println("\nModel checking marginal estimate MDP:");
            System.out.println((robust ? ex.robustSpec : ex.optimisticSpec) + " : " + result.getResultAndAccuracy());
        }
        return result;
    }

    public Strategy buildStrategy() throws PrismException {
        return super.buildUniformStrat();
    }
}


class MAPEstimatorOptimistic extends MAPEstimator {
    public MAPEstimatorOptimistic(Prism prism, Experiment ex) {
        super(prism, ex);
    }

    public Strategy buildStrategy() throws PrismException {
        return this.buildWeightedOptimisticStrategy(this.getEstimate(), 0.9);
    }
}