package learning.Estimators;

import common.Interval;
import explicit.*;
import explicit.Model;
import imdpcomp.Experiment;
import learning.Simulation.StateActionPair;
import learning.Simulation.TransitionTriple;
import org.apache.commons.lang3.NotImplementedException;
import param.Function;
import parser.ast.Expression;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.*;
import simulator.ModulesFileModelGenerator;
import strat.MDStrategy;
import strat.MDStrategyArray;
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

        } else if (ex.doBisim) {
            startTime = System.nanoTime();
            buildPointIMDP_Bisim(mdp);
            modelBuildingTime = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            resultRobust = modelCheckPointEstimateBisim(true, true);
            modelCheckingTimeRobust = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            resultOptimistic = modelCheckPointEstimateBisim(false, true);
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
        double resultOptimisticMDP = round((Double) resultOptimistic.getResult());
        MDStrategy<Double> robustStrat = ex.doBisim ? liftStrategy((MDStrategyArray<Double>) resultRobust.getStrategy(), mdp) : (MDStrategy<Double>) resultRobust.getStrategy();
        MDStrategy<Double> optimisticStrat = ex.doBisim ? liftStrategy((MDStrategyArray<Double>) resultOptimistic.getStrategy(), mdp) : (MDStrategy<Double>) resultOptimistic.getStrategy();
        this.currentStrat = optimisticStrat;

        startTime = System.nanoTime();
        double resultRobustDTMC = round((Double) checkDTMC(robustStrat).getResult());
        modelCheckingTimeDTMC = System.nanoTime() - startTime;

        double resultOptimisticDTMC = round((Double) checkDTMC(optimisticStrat).getResult());

        return new double[]{resultRobustMDP, resultRobustDTMC, resultOptimisticDTMC, modelBuildingTime, modelCheckingTimeRobust, modelCheckingTimeOptimistic, modelCheckingTimeDTMC, resultOptimisticMDP};
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

        //System.out.println("MDP: " + mdp + " Strat: " + strat );
        DTMC<Double> dtmc = (DTMC<Double>) mdp.constructInducedModel(strat);
        DTMCModelChecker mc = new DTMCModelChecker(this.prism);
        mc.setPrecomp(false); //TODO: here
        mc.setErrorOnNonConverge(ex.errorOnNonConvergence);
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

    public Result checkUncDTMC(MDStrategy strat, UMDP<Double> umdp) throws PrismException {

        //System.out.println("MDP: " + mdp + " Strat: " + strat );
        System.out.println(umdp.constructInducedModel(strat));
//        DTMCModelChecker mc = new DTMCModelChecker(this.prism);
//        mc.setPrecomp(false); //TODO: here
//        mc.setErrorOnNonConverge(ex.errorOnNonConvergence);
//        mc.setMaxIters(ex.maxVIIters);
//        mc.setTermCritParam(1e-4);
//        mc.setGenStrat(true);
//        PropertiesFile pf = prism.parsePropertiesString(ex.dtmcSpec);
//
//        ModulesFile modulesFileDTMC = (ModulesFile) modulesFileIMDP.deepCopy();
//        modulesFileDTMC.setModelType(ModelType.DTMC);
//        ModulesFileModelGenerator<?> modelGen = ModulesFileModelGenerator.create(modulesFileDTMC, this.prism);
//        modelGen.setSomeUndefinedConstants(mdp.getConstantValues());
//        //RewardGeneratorMDStrat<?> rewGen = new RewardGeneratorMDStrat(modelGen, mdp, strat);
//
//        mc.setModelCheckingInfo(modelGen, pf, modelGen);
//        Result result = mc.check(dtmc, pf.getProperty(0));
//        return result;
        return null;
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
        } else if (ex.doBisim) {
            startTime = System.nanoTime();
            buildPointIMDP_Bisim(mdp);
            modelBuildingTime = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            resultRobust = modelCheckPointEstimateBisim(true, true);
            modelCheckingTimeRobust = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            resultOptimistic = modelCheckPointEstimateBisim(false, true);
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
        MDStrategy<Double> robustStrat = ex.doBisim ? liftStrategy((MDStrategyArray<Double>) resultRobust.getStrategy(), mdp) : (MDStrategy<Double>) resultRobust.getStrategy();
        MDStrategy<Double> optimisticStrat = ex.doBisim ? liftStrategy((MDStrategyArray<Double>) resultOptimistic.getStrategy(), mdp) : (MDStrategy<Double>) resultOptimistic.getStrategy();
        this.currentStrat = optimisticStrat;

        startTime = System.nanoTime();
        double resultRobustDTMC = round((Double) checkDTMC(robustStrat).getResult());
        modelCheckingTimeDTMC = System.nanoTime() - startTime;

        double resultOptimisticDTMC = round((Double) checkDTMC(optimisticStrat).getResult());

        return new double[]{resultRobustMDP, resultRobustDTMC, resultOptimisticDTMC, modelBuildingTime, modelCheckingTimeRobust, modelCheckingTimeOptimistic, modelCheckingTimeDTMC};
    }

    public MDStrategy<Double> liftStrategy(MDStrategyArray<Double> abstractStrategy, NondetModel<Double> model) throws PrismException {
        int[] choices = new int[model.getNumStates()];

        for (int i = 0; i < choices.length; i++) {
            choices[i] = abstractStrategy.getChoiceIndex(bisimPartition[i]);
        }

        return new MDStrategyArray<>(model, choices);
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
                IntervalUtils.delimit(distrNew, Evaluator.forDouble());
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
        mc.setErrorOnNonConverge(ex.errorOnNonConvergence);

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

    public Result modelCheckPointEstimateBisim(boolean robust, boolean verbose) throws PrismException {
        UMDPModelChecker mc = new UMDPModelChecker(this.prism);
        mc.setGenStrat(true);
        mc.setPrecomp(true);
        mc.setMaxIters(ex.maxVIIters);
        mc.setTermCritParam(1e-4);
        mc.setErrorOnNonConverge(ex.errorOnNonConvergence);

        Result result = mc.check(this.bisimEstimate, robust ? ex.robustSpec_bisim : ex.optimisticSpec_bisim);
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
        mc.setErrorOnNonConverge(ex.errorOnNonConvergence);

        PropertiesFile pf;
        if (robust) {
            pf = prism.parsePropertiesString(ex.robustSpec);
        } else {
            pf = prism.parsePropertiesString(ex.optimisticSpec);
        }

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

    public Result modelCheckPointEstimateBisim(UMDP<Double> estimate ,boolean robust, boolean verbose) throws PrismException {
        UMDPModelChecker mc = new UMDPModelChecker(this.prism);
        mc.setGenStrat(true);
        mc.setPrecomp(true);
        mc.setMaxIters(ex.maxVIIters);
        mc.setTermCritParam(1e-4);
        mc.setErrorOnNonConverge(ex.errorOnNonConvergence);

        Result result = mc.check(estimate, robust ? ex.robustSpec_bisim : ex.optimisticSpec_bisim);
        if (verbose) {
            System.out.println("\nModel checking point estimate MDP:");
            System.out.println((robust ? ex.robustSpec : ex.optimisticSpec) + " : " + result.getResultAndAccuracy());
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
        mc.setErrorOnNonConverge(ex.errorOnNonConvergence);

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

     protected UMDP<Double> buildPointIMDP_Bisim(MDP<Double> groundMdp){
        return null;
     }

      /*
      Bisimulation Aggregations
     */

    /**
     * Aggregate trials for the abstract (sAbs, action) by summing ground state-action counts
     * over all ground states that map to sAbs and share the same action label.
     */
    protected int aggregateAbstractEdgeTrials(int sAbs, String action) {
        int N = 0;
        // iterate ground states that are in this abstract block
        for (int s = 0; s < mdp.getNumStates(); s++) {
            if (bisimPartition[s] != sAbs) continue;
            StateActionPair sa = new StateActionPair(s, action);
            N += sampleSizeMap.getOrDefault(sa, 0);
        }
        return N;
    }

    /**
     * Aggregate successes for the abstract edge (sAbs, action, tAbs) by summing ground
     * transition counts from all ground states in sAbs with 'action' to any ground state in tAbs.
     */
    protected int aggregateAbstractEdgeSuccesses(int sAbs, String action, int tAbs) {
        int K = 0;
        for (int s = 0; s < mdp.getNumStates(); s++) {
            if (bisimPartition[s] != sAbs) continue;
            // for this ground state/action, sum successes to all ground successors that map to tAbs
            HashSet<Integer> succs = successorStatesMap.get(new StateActionPair(s, action));
            if (succs == null) continue;
            for (int t : succs) {
                if (bisimPartition[t] != tAbs) continue;
                K += samplesMap.getOrDefault(new learning.Simulation.TransitionTriple(s, action, t), 0);
            }
        }
        return K;
    }

    // robust key for Functions (fallback to string if equals/hashCode not well-defined)
    protected static final class FuncKey {
        final param.Function f;
        final String s;
        FuncKey(param.Function f) { this.f = f; this.s = (f == null) ? "<null>" : f.toString(); }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FuncKey k)) return false;
            // prefer equals if present; otherwise fallback to string
            if (f != null && k.f != null && f.equals(k.f)) return true;
            return Objects.equals(s, k.s);
        }
        @Override public int hashCode() { return (f != null) ? f.hashCode() : s.hashCode(); }

        @Override public String toString() { return "(" + s + ")"; }
    }

    // pack (sAbs, actionLabel, tAbs) into one long
    protected long packEdgeKey(int sAbs, String action, int tAbs) {
        int ah24 = (action == null ? 0 : action.hashCode()) & 0x00FFFFFF;
        return (((long)(sAbs & 0xFFFFF)) << 44) | (((long)(tAbs & 0xFFFFF)) << 24) | (long)ah24;
    }

    // ===================== DEBUG DUMPERS =====================

    /** Pretty print the ground-level counts (N per (s,a) and K per (s,a,t))
     *  and show the param expression for each ground transition from pmdp. */
    public void debugDumpGroundCounts(boolean includeZeroEdges) {
        System.out.println("\n========== GROUND COUNTS ==========");
        if (mdp == null || pmdp == null) {
            System.out.println("(mdp/pmdp not set)");
            return;
        }

        for (int s = 0; s < mdp.getNumStates(); s++) {
            int numChoices = mdp.getNumChoices(s);
            if (numChoices == 0) continue;
            System.out.printf("State s=%d%n", s);
            for (int i = 0; i < numChoices; i++) {
                final String action = getActionString(mdp, s, i);
                StateActionPair sa = new StateActionPair(s, action);
                int N = sampleSizeMap.getOrDefault(sa, 0);

                // Build a map succ->Function (expression) from param model
                Map<Integer, Function> exprBySucc = new HashMap<>();
                for (Iterator<Map.Entry<Integer, Function>> it = pmdp.getTransitionsIterator(s, i); it.hasNext();) {
                    Map.Entry<Integer, Function> e = it.next();
                    exprBySucc.put(e.getKey(), e.getValue());
                }

                // Get successors we know about at the ground level
                HashSet<Integer> succs = successorStatesMap.get(sa);
                if (succs == null || succs.isEmpty()) {
                    if (includeZeroEdges) {
                        System.out.printf("  action=%s  N=%d  (no successors)%n", action, N);
                    }
                    continue;
                }

                System.out.printf("  action=%s  N=%d%n", action, N);
                // Header
                System.out.printf("    %-8s  %-8s  %s%n", "t", "K", "expr");
                System.out.printf("    %-8s  %-8s  %s%n", "--------", "--------", "----------------");

                // Rows
                for (int t : succs) {
                    int K = samplesMap.getOrDefault(new TransitionTriple(s, action, t), 0);
                    if (!includeZeroEdges && K == 0) continue;
                    Function f = exprBySucc.get(t);
                    String fStr = (f == null) ? "-" : f.toString();
                    System.out.printf("    %-8d  %-8d  %s%n", t, K, fStr);
                }
            }
        }
        System.out.println("===================================\n");
    }

    /** Pretty print the abstract (bisim) counts: N per (block,action), K per (block,action,block).
     *  Also prints the pooled tying table across identical expressions if enabled and available. */
    public void debugDumpAbstractCountsAndTies(boolean includeZeroEdges) {
        System.out.println("\n========== ABSTRACT (BISIM) COUNTS ==========");
        if (pmdpBisim == null || bisimPartition == null) {
            System.out.println("(bisim topology not set; call setBisimulationTopology(...))");
            return;
        }
        if (mdp == null) {
            System.out.println("(mdp not set)");
            return;
        }

        // ----- recompute edgeCounts and exprCounts identically to buildPointIMDP_Bisim -----
        final int numAbsStates = pmdpBisim.getNumStates();
        Map<Long, int[]> edgeCounts = new HashMap<>();           // (sAbs,action,tAbs) -> [K,N]
        Map<FuncKey, int[]> exprCounts = new HashMap<>();         // expr -> [Ksum, Nsum]
        Map<FuncKey, List<String>> exprEdgeList = new HashMap<>();// expr -> list of "sAbs --a--> tAbs [K/N]"

        for (int sAbs = 0; sAbs < numAbsStates; sAbs++) {
            int numChoices = pmdpBisim.getNumChoices(sAbs);
            for (int iAbs = 0; iAbs < numChoices; iAbs++) {
                final String action = getActionString(pmdpBisim, sAbs, iAbs);

                // N for the group (sAbs, action)
                final int N_group = aggregateAbstractEdgeTrials(sAbs, action);

                for (Iterator<Map.Entry<Integer, param.Function>> it = pmdpBisim.getTransitionsIterator(sAbs, iAbs); it.hasNext();) {
                    Map.Entry<Integer, param.Function> e = it.next();
                    int tAbs = e.getKey();
                    param.Function f = e.getValue();

                    final int K_edge = aggregateAbstractEdgeSuccesses(sAbs, action, tAbs);

                    long ek = packEdgeKey(sAbs, action, tAbs);
                    edgeCounts.put(ek, new int[]{K_edge, N_group});

                    FuncKey key = new FuncKey(f);
                    int[] kn = exprCounts.computeIfAbsent(key, k -> new int[]{0, 0});
                    kn[0] += K_edge;
                    kn[1] += N_group;

                    exprEdgeList
                            .computeIfAbsent(key, k -> new ArrayList<>())
                            .add(String.format("(%d) --%s--> (%d)  [%d/%d]", sAbs, action, tAbs, K_edge, N_group));
                }
            }
        }

        // ----- print abstract per-(sAbs,action,tAbs) table -----
        for (int sAbs = 0; sAbs < numAbsStates; sAbs++) {
            int numChoices = pmdpBisim.getNumChoices(sAbs);
            if (numChoices == 0) continue;
            System.out.printf("Block B=%d%n", sAbs);
            for (int iAbs = 0; iAbs < numChoices; iAbs++) {
                final String action = getActionString(pmdpBisim, sAbs, iAbs);
                // read one N_group by peeking any successor (or recompute)
                int N_group = aggregateAbstractEdgeTrials(sAbs, action);
                System.out.printf("  action=%s  N=%d%n", action, N_group);

                System.out.printf("    %-8s  %-8s  %-12s  %s%n", "toBlk", "K", "expr", "edgeKey");
                System.out.printf("    %-8s  %-8s  %-12s  %s%n", "--------", "--------", "------------", "----------------");

                for (Iterator<Map.Entry<Integer, param.Function>> it = pmdpBisim.getTransitionsIterator(sAbs, iAbs); it.hasNext();) {
                    Map.Entry<Integer, param.Function> e = it.next();
                    int tAbs = e.getKey();
                    param.Function f = e.getValue();
                    long ek = packEdgeKey(sAbs, action, tAbs);
                    int[] kn = edgeCounts.get(ek);
                    if (kn == null) continue;
                    int K = kn[0], N = kn[1];
                    if (!includeZeroEdges && K == 0) continue;
                    System.out.printf("    %-8d  %-8d  %-12s  %d%n", tAbs, K, (f == null ? "-" : f.toString()), ek);
                }
            }
        }

        // ----- print tying summary (pooled by expression) -----
        System.out.println("\n----- Abstract tying by identical expressions -----");
        if (exprCounts.isEmpty()) {
            System.out.println("(no abstract edges found)");
        } else {
            System.out.printf("  %-16s  %-10s  %-10s  %s%n", "expression", "K_total", "N_total", "contributing edges [K/N]");
            System.out.printf("  %-16s  %-10s  %-10s  %s%n", "----------------", "----------", "----------", "-------------------------");
            for (Map.Entry<FuncKey,int[]> e : exprCounts.entrySet()) {
                FuncKey fk = e.getKey();
                int[] kn = e.getValue();
                List<String> edges = exprEdgeList.getOrDefault(fk, Collections.emptyList());
                System.out.printf("  %-16s  %-10d  %-10d  %s%n", fk.s, kn[0], kn[1], edges);
            }
        }
        System.out.println("===============================================\n");
    }

    /** Convenience: dump both ground and abstract tables together. */
    public void debugDumpCounts(boolean includeZeroEdges) {
        debugDumpGroundCounts(includeZeroEdges);
        debugDumpAbstractCountsAndTies(includeZeroEdges);
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