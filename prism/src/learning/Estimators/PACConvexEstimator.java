package learning.Estimators;

import com.gurobi.gurobi.*;
import common.Interval;
import common.GurobiEnvManager;
import explicit.*;
import imdpcomp.Experiment;
import imdpcomp.Experiment.IntervalAbstractionMode;
import learning.ParametricConvex.ConvexLearner;
import learning.ParametricConvex.ExpressionTranslator;
import learning.ParametricConvex.RobustDTMCOneShotLP;
import learning.Simulation.StateActionPair;
import learning.Simulation.TransitionTriple;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.special.Beta;
import param.Function;
import param.Point;
import prism.Evaluator;
import prism.Prism;
import prism.PrismException;
import prism.Result;
import strat.MDStrategy;
import strat.MDStrategyArray;

import java.util.*;
import java.util.concurrent.*;

import static imdpcomp.Experiment.IntervalAbstractionMode.*;
import static imdpcomp.Experiment.ParameterTying.NO_TYING;

public class PACConvexEstimator extends MAPEstimator {

    protected double error_tolerance;
    double precision = 1e-8;
    private static final double PROB_EPS = 1e-12;
    boolean useVertexPrecomp = true;
    boolean verbose_bisim = false;

    // For parameter-tying in IMDP
    protected HashMap<TransitionTriple, Double> tiedModes = new HashMap<>();
    protected HashMap<TransitionTriple, Integer> tiedTransitionCounts = new HashMap<>();
    protected HashMap<TransitionTriple, Integer> tiedStateActionCounts = new HashMap<>();

    public PACConvexEstimator(Prism prism, Experiment ex) {
        super(prism, ex);
        error_tolerance = ex.error_tolerance;
        useVertexPrecomp = ex.useVertexPrecomp;
        verbose_bisim = ex.verboseBisim;
        this.name = "PAC";
    }

    /**
     * Combine transition-triple and state-action pair counts for similar transitions, i.e., tie the parameters.
     */
    public void tieParameters() {
        List<List<TransitionTriple>> similarTransitions = this.getSimilarTransitions();

        for (List<TransitionTriple> transitions : similarTransitions) {
            // Compute mode and count over all similar transitions
            int num = 0;
            int denum = 0;
            //System.out.println("Sample size map:" + samplesMap);
            for (TransitionTriple t : transitions) {
                StateActionPair sa = t.getStateAction();
                num += samplesMap.getOrDefault(t, 0);
                denum += sampleSizeMap.getOrDefault(sa, 0);
            }

            for (TransitionTriple t : transitions) {
                double mode = (double) num / (double) denum;
                tiedModes.put(t, mode);
                tiedTransitionCounts.put(t, num);
                tiedStateActionCounts.put(t, denum);
            }
        }

    }

    @Override
    protected Interval<Double> getTransitionInterval(TransitionTriple t) {
        int n, k;

        if (this.ex.tieParameters == NO_TYING) {
            n = getStateActionCount(t.getStateAction());
            k = getTransitionCount(t);
        }  else { // tying
            Integer nTied = tiedStateActionCounts.get(t);
            Integer kTied = tiedTransitionCounts.get(t);
            if (nTied == null || nTied == 0 || kTied == null) {
                // No observations for this tied component: vacuous interval.
                return new Interval<>(0.0, 1.0);
            }
            n = nTied;
            k = kTied;
        }

        int m = this.getNumLearnableTransitions();

        return computeClopperPearson(n, k, (1.0 - error_tolerance) / (double) m);

    }

    @Override
    public double[] getCurrentResults() throws PrismException {
        updatePriors();
        UMDP<Double> imdp = buildPointIMDP(mdp);
        //System.out.println("IMDP: " + imdp);

        Result resultRobustConvex;
        Result resultOptimisticConvex;

        long startTime;
        long modelBuildingTime;
        long modelCheckingTimeRobust;
        long modelCheckingTimeOptimistic;
        long modelCheckingTimeDTMC;

        // Solve Convex Parametric MDP
        try {
            if (ex.doBisim) {
                UMDP<Double> imdpBisim = buildPointIMDP_Bisim(mdp);

                startTime = System.nanoTime();
                UMDP<Double> convex_mdp = buildConvexUMDPCombinedBisim(imdp, pmdp, imdpBisim, pmdpBisim);
                modelBuildingTime = System.nanoTime() - startTime;

                startTime = System.nanoTime();
                resultRobustConvex = modelCheckPointEstimateBisim(convex_mdp, true, false);
                modelCheckingTimeRobust = System.nanoTime() - startTime;

                startTime = System.nanoTime();
                resultOptimisticConvex = modelCheckPointEstimateBisim(convex_mdp, false, false);
                modelCheckingTimeOptimistic = System.nanoTime() - startTime;

            } else {
                startTime = System.nanoTime();
                UMDP<Double> convex_mdp = ex.useApsEllipsoid ? buildApsEllipsoidUMDP(pmdp) : buildConvexUMDP(imdp, this.pmdp);
                modelBuildingTime = System.nanoTime() - startTime;

                startTime = System.nanoTime();
                resultRobustConvex = modelCheckPointEstimate(convex_mdp, true, false);
                modelCheckingTimeRobust = System.nanoTime() - startTime;

                startTime = System.nanoTime();
                resultOptimisticConvex = modelCheckPointEstimate(convex_mdp, false, false);
                modelCheckingTimeOptimistic = System.nanoTime() - startTime;
            }
        } catch (GRBException e) {
            throw asPrismException("Convex model construction failed", e);
        }
        double resconvexMDP = round((Double) resultRobustConvex.getResult());
        double resconvexMDPOptimistic = round((Double) resultOptimisticConvex.getResult());
        MDStrategy<Double> robustStrat = ex.doBisim ? liftStrategy((MDStrategyArray<Double>) resultRobustConvex.getStrategy(), mdp) : (MDStrategy<Double>) resultRobustConvex.getStrategy();
        MDStrategy<Double> optimisticStrat = ex.doBisim ? liftStrategy((MDStrategyArray<Double>) resultOptimisticConvex.getStrategy(), mdp) : (MDStrategy<Double>) resultOptimisticConvex.getStrategy();
        this.currentStrat = optimisticStrat;

        Result uncDTMCres = checkUncDTMC(robustStrat, convex_estimate, false);
        double resUDTMC = round((Double) uncDTMCres.getResult());

        // Check optimal strategy
        double uncDTMCresOptRob = round((double) checkUncDTMC(optimalStrat, convex_estimate, true).getResult());
        double uncDTMCresOptOpt = round((double) checkUncDTMC(optimalStrat, convex_estimate, false).getResult());

        startTime = System.nanoTime();
        double resconvexDTMC = round((Double) checkDTMC(robustStrat).getResult());
        modelCheckingTimeDTMC = System.nanoTime() - startTime;

        double resultConvexOptimisticDTMC = round((Double) checkDTMC(optimisticStrat).getResult());

        System.out.println("Convex Guarantee: " + resconvexMDP + ", Convex Performance: " + resconvexDTMC);
        System.out.println("Optimistic Guarantee (from optimistic optimal policy): " + resultOptimisticConvex.getResult());
        System.out.println("UDTMC Opimistic Guarantee for Robust: " + uncDTMCres.getResult());
        System.out.println("UDTMC Optimal Policy Robust: " + uncDTMCresOptRob);
        System.out.println("UDTMC Optimal Policy Optimistic " + uncDTMCresOptOpt);

        return new double[]{resconvexMDP, resconvexDTMC, resultConvexOptimisticDTMC, modelBuildingTime, modelCheckingTimeRobust, modelCheckingTimeOptimistic, modelCheckingTimeDTMC, resUDTMC, uncDTMCresOptRob, uncDTMCresOptOpt};
    }

    // TODO : Update
    @Override
    public double[] getInitialResults() throws PrismException {
        if (ex.doBisim) {
            Result resultRobust;
            Result resultOptimistic;

            long startTime;
            long modelBuildingTime;
            long modelCheckingTimeRobust;
            long modelCheckingTimeOptimistic;
            long modelCheckingTimeDTMC;

            // Build and Model Check IMDP
            UMDP<Double> imdpGround = buildPointIMDP(mdp);
            resultRobust = modelCheckPointEstimate(true, true);
            resultOptimistic = modelCheckPointEstimate(false, true);

            startTime = System.nanoTime();
            UMDP<Double> imdpBisim = buildPointIMDP_Bisim(mdp);
            modelBuildingTime = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            resultRobust = modelCheckPointEstimateBisim(true, true);
            modelCheckingTimeRobust = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            resultOptimistic = modelCheckPointEstimateBisim(false, true);
            modelCheckingTimeOptimistic = System.nanoTime() - startTime;

            double resultRobustMDP = round((Double) resultRobust.getResult());
            MDStrategy<Double> robustStrat = ex.doBisim ? liftStrategy((MDStrategyArray<Double>) resultRobust.getStrategy(), mdp) : (MDStrategy<Double>) resultRobust.getStrategy();
            MDStrategy<Double> optimisticStrat = ex.doBisim ? liftStrategy((MDStrategyArray<Double>) resultOptimistic.getStrategy(), mdp) : (MDStrategy<Double>) resultOptimistic.getStrategy();
            this.currentStrat = optimisticStrat;

            startTime = System.nanoTime();
            double resultRobustDTMC = round((Double) checkDTMC(robustStrat).getResult());
            modelCheckingTimeDTMC = System.nanoTime() - startTime;

            double resultOptimisticDTMC = round((Double) checkDTMC(optimisticStrat).getResult());

//            System.out.println("IMDP Ground: " + imdpGround);
//            System.out.println("IMDP Bisim: " + imdpBisim);
            try {
                buildConvexUMDP(imdpGround, this.pmdp);
            } catch (GRBException e) {
                throw asPrismException("Convex model warm-up failed", e);
            }
            return new double[]{resultRobustMDP, resultRobustDTMC, resultOptimisticDTMC, modelBuildingTime, modelCheckingTimeRobust, modelCheckingTimeOptimistic, modelCheckingTimeDTMC};
        } else {
            double[] res = super.getInitialResults();
            try {
                buildConvexUMDP(this.estimate, this.pmdp);
            } catch (GRBException e) {
                throw asPrismException("Convex model warm-up failed", e);
            }
            return res;
        }
    }

    @Override
    public UMDPSimple<Double> buildPointIMDP(MDP<Double> mdp) {
        int numStates = mdp.getNumStates();
        UMDPSimple<Double> imdp = new UMDPSimple<>(numStates);
        imdp.addInitialState(mdp.getFirstInitialState());
        imdp.setStatesList(mdp.getStatesList());
        imdp.setConstantValues(mdp.getConstantValues());

        if (ex.tieParameters != NO_TYING) tieParameters();

        for (int s = 0; s < numStates; s++) {
            int numChoices = mdp.getNumChoices(s);
            final int state = s;
            for (int i = 0; i < numChoices; i++) {
                final String action = getActionString(mdp, s, i);

                Distribution<Interval<Double>> distrNew = new Distribution<>(Evaluator.forDoubleInterval());
                mdp.forEachDoubleTransition(s, i, (int sFrom, int sTo, double p) -> {
                    TransitionTriple t = new TransitionTriple(state, action, sTo);
                    Interval<Double> interval;
                    if (p <= PROB_EPS) {
                        return;
                    } else if (p >= 1.0 - PROB_EPS) {
                        interval = new Interval<>(1.0, 1.0);
                        distrNew.add(sTo, interval);
                        this.intervalsMap.put(t, interval);
                    } else {
                        interval = getTransitionInterval(t);
                        distrNew.add(sTo, interval);
                        this.intervalsMap.put(t, interval);
                    }
                });
                IntervalUtils.delimit(distrNew, Evaluator.forDouble());
                UDistributionIntervals<Double> udist = new UDistributionIntervals<>(distrNew);
                imdp.addActionLabelledChoice(s, udist, getActionString(mdp, s, i));
            }
        }
        Map<String, BitSet> labels = mdp.getLabelToStatesMap();
        for (Map.Entry<String, BitSet> entry : labels.entrySet()) {
            imdp.addLabel(entry.getKey(), entry.getValue());
        }
        this.estimate = imdp;

        return imdp;
    }

    /**
     * Build an IMDP on the *abstract* (bisim) topology by:
     *  1) lumping ground counts onto abstract edges;
     *  2) optionally tying identical expressions (Functions) across all abstract edges.
     */
    protected UMDP<Double> buildPointIMDP_Bisim(MDP<Double> groundMdp) {
        if (pmdpBisim == null || bisimPartition == null) {
            throw new IllegalStateException("Bisim topology/partition not set. Call setBisimulationTopology(...) first.");
        }

        final int numAbsStates = pmdpBisim.getNumStates();
        UMDPSimple<Double> imdp = new UMDPSimple<>(numAbsStates);
        // map the concrete initial state to its abstract block
        final int s0 = groundMdp.getFirstInitialState();
        final int b0 = bisimPartition[s0];
        imdp.addInitialState(b0);

        imdp.setStatesList(pmdpBisim.getStatesList());
        imdp.setConstantValues(pmdpBisim.getConstantValues());

        // ----- PASS 1: collect counts -----
        // per abstract edge: (sAbs, action, tAbs) -> [K, N]
        Map<Long, int[]> edgeCounts = new HashMap<>();
        // pooled over identical expressions (if enabled): FuncKey -> [K_total, N_total]
        Map<FuncKey, int[]> exprCounts = new HashMap<>();

        for (int sAbs = 0; sAbs < numAbsStates; sAbs++) {
            int numChoices = pmdpBisim.getNumChoices(sAbs);
            for (int iAbs = 0; iAbs < numChoices; iAbs++) {
                final String action = getActionString(pmdpBisim, sAbs, iAbs);

                // trials for all edges from (sAbs, action)
                final int N_group = aggregateAbstractEdgeTrials(sAbs, action);

                for (Iterator<Map.Entry<Integer, param.Function>> it = pmdpBisim.getTransitionsIterator(sAbs, iAbs); it.hasNext();) {
                    Map.Entry<Integer, param.Function> e = it.next();
                    int tAbs = e.getKey();
                    param.Function f = e.getValue();

                    // successes for this abstract edge
                    final int K_edge = aggregateAbstractEdgeSuccesses(sAbs, action, tAbs);

                    long ek = packEdgeKey(sAbs, action, tAbs);
                    edgeCounts.put(ek, new int[]{K_edge, N_group});

                    if (tieAbstractExpressions) {
                        FuncKey key = new FuncKey(f);
                        int[] kn = exprCounts.computeIfAbsent(key, k -> new int[]{0, 0});
                        kn[0] += K_edge;  // sum successes
                        kn[1] += N_group; // sum trials
                    }
                }
            }
        }

        int learnable;
        if (tieAbstractExpressions) {
            // mark expressions that ever occur in a learnable context
            HashSet<FuncKey> learnableExprs = new HashSet<>();
            for (int sAbs = 0; sAbs < numAbsStates; sAbs++) {
                int numChoices = pmdpBisim.getNumChoices(sAbs);
                for (int iAbs = 0; iAbs < numChoices; iAbs++) {
                    final String action = getActionString(pmdpBisim, sAbs, iAbs);
                    var dist = pmdpBisim.getDistribution(sAbs, iAbs);
                    boolean multi = dist.getSupport().size() > 1;
                    for (Iterator<Map.Entry<Integer, param.Function>> it = pmdpBisim.getTransitionsIterator(sAbs, iAbs); it.hasNext();) {
                        Map.Entry<Integer, param.Function> e = it.next();
                        var f = e.getValue();
                        if (multi && !f.isOne()) {
                            learnableExprs.add(new FuncKey(f));
                        }
                    }
                }
            }
            learnable = Math.max(1, learnableExprs.size());
        } else {
            HashSet<Long> learnableEdges = new HashSet<>();
            for (int sAbs = 0; sAbs < numAbsStates; sAbs++) {
                int numChoices = pmdpBisim.getNumChoices(sAbs);
                for (int iAbs = 0; iAbs < numChoices; iAbs++) {
                    final String action = getActionString(pmdpBisim, sAbs, iAbs);
                    var dist = pmdpBisim.getDistribution(sAbs, iAbs);
                    boolean multi = dist.getSupport().size() > 1;
                    for (Iterator<Map.Entry<Integer, param.Function>> it = pmdpBisim.getTransitionsIterator(sAbs, iAbs); it.hasNext();) {
                        Map.Entry<Integer, param.Function> e = it.next();
                        int tAbs = e.getKey();
                        var f = e.getValue();
                        if (multi && !f.isOne()) {
                            learnableEdges.add(packEdgeKey(sAbs, action, tAbs));
                        }
                    }
                }
            }
            learnable = Math.max(1, learnableEdges.size());
        }

        //System.out.println("Tied expressions in abstract model: " + exprCounts.keySet().stream().toList());

        // alpha split: per unique CI constructed
        final double alphaPerCI = (1.0 - error_tolerance) / (double) learnable;

        // ----- PASS 2: build distributions with chosen tying policy -----
        for (int sAbs = 0; sAbs < numAbsStates; sAbs++) {
            int numChoices = pmdpBisim.getNumChoices(sAbs);
            for (int iAbs = 0; iAbs < numChoices; iAbs++) {
                final String action = getActionString(pmdpBisim, sAbs, iAbs);
                Distribution<Interval<Double>> distrNew = new Distribution<>(Evaluator.forDoubleInterval());

                boolean multi = pmdpBisim.getDistribution(sAbs, iAbs).getSupport().size() > 1;

                for (Iterator<Map.Entry<Integer, param.Function>> it = pmdpBisim.getTransitionsIterator(sAbs, iAbs); it.hasNext();) {
                    Map.Entry<Integer, param.Function> e = it.next();
                    int tAbs = e.getKey();
                    param.Function f = e.getValue();

                    Interval<Double> interval;
                    if (!multi || f.isOne()) {
                        interval = new Interval<>(1.0, 1.0);
                    } else if (tieAbstractExpressions) {
                        int[] kn = exprCounts.get(new FuncKey(f));
                        int K = kn[0], N = kn[1];
                        if (f.isOne() || pmdpBisim.getDistribution(sAbs, iAbs).getSupport().size() == 1) {
                            interval = new Interval<>(1.0, 1.0);
                        } else {
                            interval = (N == 0)
                                    ? new Interval<>(0.0, 1.0)
                                    : computeClopperPearson(N, K, alphaPerCI);
                        }
                    } else {
                        int[] kn = edgeCounts.get(packEdgeKey(sAbs, action, tAbs));
                        int K = kn[0], N = kn[1];
                        if (f.isOne() || pmdpBisim.getDistribution(sAbs, iAbs).getSupport().size() == 1) {
                            interval = new Interval<>(1.0, 1.0);
                        } else {
                            interval = (N == 0)
                                    ? new Interval<>(0.0, 1.0)
                                    : computeClopperPearson(N, K, alphaPerCI);
                        }
                    }

                    distrNew.add(tAbs, interval);

                    // (optional) stash for debugging on abstract edge IDs:
                    // intervalsMap.put(new TransitionTriple(-1, action, (sAbs<<16) ^ tAbs), interval);
                }

                IntervalUtils.delimit(distrNew, Evaluator.forDouble());
                UDistributionIntervals<Double> udist = new UDistributionIntervals<>(distrNew);
                imdp.addActionLabelledChoice(sAbs, udist, action);
            }
        }

        // labels (carried over by pmdpBisim already)
        for (Map.Entry<String, BitSet> e : pmdpBisim.getLabelToStatesMap().entrySet())
            imdp.addLabel(e.getKey(), e.getValue());

        if (verbose_bisim) {
            this.debugDumpCounts(false);
        }

        this.bisimEstimate = imdp;
        return imdp;
    }


    public UMDP<Double> buildConvexUMDP(UMDP<Double> imdp, MDPSimple<Function> pmdp) throws GRBException, PrismException {
        GRBEnv env = GurobiEnvManager.getSharedEnv();

//        double[] thetaHat = estimateThetaHatCountsLS(mdp, pmdp, ex.apsLambda > 0 ? ex.apsLambda : 1e-2);
//        System.out.println("Theta Hat: " + Arrays.toString(thetaHat));
//        System.out.println("With new function:");
//        buildApsEllipsoidUMDP(pmdp);

        ConvexLearner cxl = new ConvexLearner(env);
        cxl.enableOBBT(ex.obbtMaxIters, ex.obbtEps);
        cxl.setParamModel(pmdp);
        cxl.setConstraints(imdp);
        if ((ex.obbtMaxIters > 0)) {
            cxl.runObbtLoopAndRebuild();
        } else {
            cxl.commitConstraints();
        }
        cxl.getModel().update();

//        if (RobustDTMCOneShotLP.isDTMC(pmdp) && ex.useDTMCLP) {
//            BitSet goal = pmdp.getLabelToStatesMap().getOrDefault("goal", new BitSet());
//
//            RobustDTMCOneShotLP.Result rReach = RobustDTMCOneShotLP.solve(cxl, pmdp, goal, RobustDTMCOneShotLP.Objective.REACH_PROB_MIN);
//            System.out.println("One-shot robust reach (init): " + rReach.x0);
//        }

        // Printing Model
        //ConvexLearner.printModel(cxl.getModel());

        // 2) Build intervals by optimizing each unique function once
        if (ex.useLPToIntervals) {
            UMDPSimple<Double> LPimdp = (ex.intervalAbstractionMode == EXACT)
                    ? buildIntervalizedUMDPFromLP(cxl, pmdp)
                    : buildIntervalizedUMDPFromLPIntervalArithmetic(cxl, pmdp);
            LPimdp.addInitialState(pmdp.getFirstInitialState());
            LPimdp.setStatesList(pmdp.getStatesList());
            LPimdp.setConstantValues(pmdp.getConstantValues());

            for (Map.Entry<String, BitSet> entry : pmdp.getLabelToStatesMap().entrySet()) {
                LPimdp.addLabel(entry.getKey(), entry.getValue());
            }
            this.convex_estimate = LPimdp;

            return LPimdp;
        } else {
            if (useVertexPrecomp) {
                cxl.setVertexCap(10000);
                cxl.precomputeVertices();
            }

            UMDPSimple<Double> convex_mdp = cxl.getUMDP();
            convex_mdp.addInitialState(pmdp.getFirstInitialState());
            convex_mdp.setStatesList(pmdp.getStatesList());
            convex_mdp.setConstantValues(pmdp.getConstantValues());

            Map<String, BitSet> labels = pmdp.getLabelToStatesMap();
            for (Map.Entry<String, BitSet> entry : labels.entrySet()) {
                convex_mdp.addLabel(entry.getKey(), entry.getValue());
            }

            this.convex_estimate = convex_mdp;

            return convex_mdp;
        }
    }

    public UMDP<Double> buildConvexUMDPCombinedBisim(UMDP<Double> imdpGround, MDPSimple<Function> pmdpGround, UMDP<Double> imdpBisim, MDPSimple<Function> pmdpBisim) throws GRBException, PrismException {
        GRBEnv env = GurobiEnvManager.getSharedEnv();

        ConvexLearner cxl = new ConvexLearner(env);
        // Set both constraints, from ground and abstract model, setConstraints() only keeps tighter constraints
        cxl.resetModel();
        cxl.enableOBBT(ex.obbtMaxIters, ex.obbtEps);
        cxl.setConstraints(pmdpGround, imdpGround);
        cxl.setConstraints(pmdpBisim, imdpBisim);
        cxl.setParamModel(pmdpBisim);
        if (ex.obbtMaxIters > 0) {
            cxl.runObbtLoopAndRebuild();
        } else {
            cxl.commitConstraints();
        }

        cxl.getModel().update();

        if (useVertexPrecomp) {
            cxl.setVertexCap(10000);
            cxl.precomputeVertices();
        }

        // Printing Model
        //ConvexLearner.printModel(cxl.getModel());

        UMDPSimple<Double> convex_mdp = cxl.getUMDP();
        convex_mdp.addInitialState(imdpBisim.getFirstInitialState());
        convex_mdp.setStatesList(imdpBisim.getStatesList());
        convex_mdp.setConstantValues(imdpBisim.getConstantValues());

        Map<String, BitSet> labels = imdpBisim.getLabelToStatesMap();
        for (Map.Entry<String, BitSet> entry : labels.entrySet()) {
            convex_mdp.addLabel(entry.getKey(), entry.getValue());
        }

        return convex_mdp;
    }

    /**
     * Build an IMDP by solving min/max for each unique Function over the committed LP.
     * Parallelism is controlled by ex.parallelizeBounds(): when true, create one GRBEnv+GRBModel copy per worker (no files).
     */
    private UMDPSimple<Double> buildIntervalizedUMDPFromLP(ConvexLearner cxl,
                                                           MDPSimple<Function> pmdp)
            throws GRBException, PrismException {

        final GRBModel baseModel = cxl.getModel();
        final ExpressionTranslator trans = cxl.getTranslator();

        // If you tightened bounds (OBBT), reflect them in the translator for McCormick
        refreshTranslatorBoundsFromModel(trans, baseModel);

        final int n = pmdp.getNumStates();
        UMDPSimple<Double> out = new UMDPSimple<>(n);

        // Cache numeric bounds [lo, hi] per unique expression (use f.toString()).
        final Map<String, double[]> exprCache = new ConcurrentHashMap<>();

        // ---- 1) Collect & translate all unique non-constant Functions ONCE (serial) ----
        final LinkedHashMap<String, ObjSpec> todoSpecs = new LinkedHashMap<>();
        for (int s = 0; s < n; s++) {
            int numChoices = pmdp.getNumChoices(s);
            for (int i = 0; i < numChoices; i++) {
                boolean singleSucc = pmdp.getDistribution(s, i).getSupport().size() == 1;
                for (Iterator<Map.Entry<Integer, Function>> it = pmdp.getTransitionsIterator(s, i); it.hasNext();) {
                    Map.Entry<Integer, Function> e = it.next();
                    Function f = e.getValue();

                    if (singleSucc || f.isOne()) continue; // fixed 1.0

                    final String key = f.toString();
                    if (exprCache.containsKey(key) || todoSpecs.containsKey(key)) continue;

                    if (f.isConstant()) {
                        double v = f.asBigRational().doubleValue();
                        exprCache.put(key, new double[]{v, v});
                        continue;
                    }

                    // Translate now (may add aux vars/cons); no worker mutates the model.
                    GRBLinExpr lin = trans.translateLinearExpression(f.asExpression());
                    baseModel.update();

                    // Extract objective spec (names + coeffs + constant) for rebuild in worker copies
                    final int terms = lin.size();
                    final String[] names = new String[terms];
                    final double[] coeffs = new double[terms];
                    for (int k = 0; k < terms; k++) {
                        names[k]  = lin.getVar(k).get(GRB.StringAttr.VarName);
                        coeffs[k] = lin.getCoeff(k);
                    }
                    final double constant = lin.getConstant();
                    todoSpecs.put(key, new ObjSpec(names, coeffs, constant));
                }
            }
        }

        if (!todoSpecs.isEmpty()) {
            baseModel.update(); // freeze before copying to other envs

            if (ex.exprBoundWorkers > 1) {
                // ---- 2a) PARALLEL:
                final int workerCount = ex.exprBoundWorkers;
                final int threadsPerWorker = 1;

                final ExecutorService pool = Executors.newFixedThreadPool(workerCount);

                // Precreate worker envs+model copies on the main thread
                final List<WorkerHandle> workers = new ArrayList<>(workerCount);
                for (int w = 0; w < workerCount; w++) {
                    // 1) Empty env -> set silence -> start
                    GRBEnv env = new GRBEnv(true);                 // empty env (nothing printed yet)
                    env.set(GRB.IntParam.LogToConsole, 0);         // silence console
                    env.set(GRB.IntParam.OutputFlag, 0);           // belt & braces
                    env.start();                                   // now the license banner won't print

                    GRBModel m = new GRBModel(baseModel, env);
                    m.set(GRB.IntParam.LogToConsole, 0);
                    m.set(GRB.IntParam.OutputFlag, 0);

                    m.set(GRB.IntParam.Method, 1);
                    m.set(GRB.IntParam.Threads, threadsPerWorker);

                    workers.add(new WorkerHandle(env, m));
                }

                final List<Map.Entry<String, ObjSpec>> all = new ArrayList<>(todoSpecs.entrySet());
                final List<List<Map.Entry<String, ObjSpec>>> chunks = partition(all, workerCount);

                final List<Future<?>> futures = new ArrayList<>();
                for (int w = 0; w < chunks.size(); w++) {
                    final List<Map.Entry<String, ObjSpec>> chunk = chunks.get(w);
                    final WorkerHandle wh = workers.get(w);
                    futures.add(pool.submit(() -> {
                        try {
                            for (Map.Entry<String, ObjSpec> entry : chunk) {
                                final String key = entry.getKey();
                                final ObjSpec spec = entry.getValue();
                                final double[] b = solveMinMaxOn(wh.model, spec);
                                double lo = clamp(b[0], precision, 1.0 - precision);
                                double hi = clamp(b[1], precision, 1.0 - precision);
                                if (hi < lo) { double t = lo; lo = hi; hi = t; }
                                exprCache.put(key, new double[]{lo, hi});
                            }
                        } finally {
                            // Dispose this worker's resources
                            try { wh.model.dispose(); } catch (Throwable ignore) {}
                            try { wh.env.dispose(); }   catch (Throwable ignore) {}
                        }
                        return null;
                    }));
                }

                // Join
                for (Future<?> f : futures) {
                    try { f.get(); }
                    catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw new RuntimeException("Interrupted", ie); }
                    catch (ExecutionException ee) { throw new RuntimeException("Worker failed", ee.getCause()); }
                }
                pool.shutdown();

            } else {
                // ---- 2b) SERIAL: reuse ONE model; change objective; warm re-solve ----
                final int oldMethod  = baseModel.get(GRB.IntParam.Method);
                final int oldThreads = baseModel.get(GRB.IntParam.Threads);
                final int oldOut     = baseModel.get(GRB.IntParam.OutputFlag);
                try {
                    baseModel.set(GRB.IntParam.Method, 1);        // dual simplex
                    baseModel.set(GRB.IntParam.OutputFlag, 0);

                    for (Map.Entry<String, ObjSpec> e : todoSpecs.entrySet()) {
                        final String key = e.getKey();
                        final ObjSpec spec = e.getValue();

                        GRBLinExpr expr = new GRBLinExpr();
                        for (int k = 0; k < spec.names.length; k++) {
                            expr.addTerm(spec.coeffs[k], baseModel.getVarByName(spec.names[k]));
                        }
                        expr.addConstant(spec.constant);

                        double lo = optimize(baseModel, expr, GRB.MINIMIZE);
                        double hi = optimize(baseModel, expr, GRB.MAXIMIZE);

                        lo = clamp(lo, precision, 1.0 - precision);
                        hi = clamp(hi, precision, 1.0 - precision);
                        if (hi < lo) { double t = lo; lo = hi; hi = t; }

                        exprCache.put(key, new double[]{lo, hi});
                    }
                } finally {
                    baseModel.set(GRB.IntParam.Method, oldMethod);
                    baseModel.set(GRB.IntParam.Threads, oldThreads);
                    baseModel.set(GRB.IntParam.OutputFlag, oldOut);
                }
            }
        }

        // ---- 3) Build the UMDP using cached bounds ----
        for (int s = 0; s < n; s++) {
            int numChoices = pmdp.getNumChoices(s);
            for (int i = 0; i < numChoices; i++) {
                final String action = getActionString(pmdp, s, i);
                Distribution<Interval<Double>> distrNew = new Distribution<>(Evaluator.forDoubleInterval());
                boolean singleSucc = pmdp.getDistribution(s, i).getSupport().size() == 1;

                for (Iterator<Map.Entry<Integer, Function>> it = pmdp.getTransitionsIterator(s, i); it.hasNext();) {
                    Map.Entry<Integer, Function> e = it.next();
                    int t = e.getKey();
                    Function f = e.getValue();

                    final Interval<Double> interval;
                    if (singleSucc || f.isOne()) {
                        interval = new Interval<>(1.0, 1.0);
                    } else {
                        final String key = f.toString();
                        double[] b = exprCache.get(key);
                        if (b == null) {
                            double v = f.isConstant() ? f.asBigRational().doubleValue() : 0.0;
                            b = new double[]{v, v};
                        }
                        interval = new Interval<>(b[0], b[1]); // fresh per-choice
                    }
                    distrNew.add(t, interval);
                }

                IntervalUtils.delimit(distrNew, Evaluator.forDouble());
                out.addActionLabelledChoice(s, new UDistributionIntervals<>(distrNew), action);
            }
        }

        return out;
    }

    /* ===== Helpers ===== */

    private static final class WorkerHandle {
        final GRBEnv env;
        final GRBModel model;
        WorkerHandle(GRBEnv env, GRBModel model) { this.env = env; this.model = model; }
    }

    private static final class ObjSpec {
        final String[] names;
        final double[] coeffs;
        final double constant;
        ObjSpec(String[] names, double[] coeffs, double constant) {
            this.names = names;
            this.coeffs = coeffs;
            this.constant = constant;
        }
    }

    private static double[] solveMinMaxOn(GRBModel m, ObjSpec spec) throws GRBException {
        GRBLinExpr expr = new GRBLinExpr();
        for (int k = 0; k < spec.names.length; k++) {
            expr.addTerm(spec.coeffs[k], m.getVarByName(spec.names[k]));
        }
        expr.addConstant(spec.constant);

        double lo = optimize(m, expr, GRB.MINIMIZE);
        double hi = optimize(m, expr, GRB.MAXIMIZE);

        return new double[]{lo, hi};
    }

    private static double clamp(double x, double lo, double hi) {
        return Math.max(lo, Math.min(hi, x));
    }

    private static <T> List<List<T>> partition(List<T> items, int parts) {
        final int n = items.size();
        final int k = Math.max(1, Math.min(parts, n));
        final List<List<T>> chunks = new ArrayList<>(k);
        int base = n / k, rem = n % k, idx = 0;
        for (int i = 0; i < k; i++) {
            int sz = base + (i < rem ? 1 : 0);
            chunks.add(items.subList(idx, idx + sz));
            idx += sz;
        }
        return chunks;
    }

    /** Build an IMDP by solving min/max for each unique Function over the committed LP. */
    private UMDPSimple<Double> buildIntervalizedUMDPFromLPIntervalArithmetic(ConvexLearner cxl, MDPSimple<Function> pmdp)
            throws GRBException, PrismException {
        final GRBModel model = cxl.getModel();
        final ExpressionTranslator trans = cxl.getTranslator();

        // If you tightened bounds (OBBT), reflect them in the translator for McCormick
        // (uncomment if you have the helper in this class)
        refreshTranslatorBoundsFromModel(trans, model);

        final int n = pmdp.getNumStates();
        UMDPSimple<Double> out = new UMDPSimple<>(n);

        // IMPORTANT: cache raw bounds only, NOT Interval objects (they are mutated by delimit()).
        Map<String, double[]> exprCache = new HashMap<>();

        // TODO: testing parametric precomp / interval arithmetic
        Map<GRBVar, double[]> varBounds = new HashMap<>();
        GRBVar[] vars = model.getVars();
        int freeVarCount = 0;
        System.out.println("Pre-computing bounds for " + vars.length + " LP variables...");
        for (GRBVar v : vars) {
            double lb_attr = v.get(GRB.DoubleAttr.LB);
            double ub_attr = v.get(GRB.DoubleAttr.UB);

            // Optimization: If the variable's attributes show it's fixed, don't call the solver.
            if (Math.abs(ub_attr - lb_attr) < 1e-9) {
                varBounds.put(v, new double[]{lb_attr, lb_attr});
            } else {
                freeVarCount++;
                GRBLinExpr v_obj = new GRBLinExpr();
                v_obj.addTerm(1.0, v);

                double lb_opt = optimize(model, v_obj, GRB.MINIMIZE);
                double ub_opt = optimize(model, v_obj, GRB.MAXIMIZE);

                varBounds.put(v, new double[]{lb_opt, ub_opt});
            }
        }
        System.out.println("Pre-computed bounds for " + freeVarCount + " free variables...");

        //------------

        for (int s = 0; s < n; s++) {
            int numChoices = pmdp.getNumChoices(s);
            for (int i = 0; i < numChoices; i++) {
                final String action = getActionString(pmdp, s, i);
                Distribution<Interval<Double>> distrNew = new Distribution<>(Evaluator.forDoubleInterval());

                boolean singleSucc = pmdp.getDistribution(s, i).getSupport().size() == 1;

                for (Iterator<Map.Entry<Integer, Function>> it = pmdp.getTransitionsIterator(s, i); it.hasNext();) {
                    Map.Entry<Integer, Function> e = it.next();
                    int t = e.getKey();
                    Function f = e.getValue();

                    Interval<Double> interval;
                    if (singleSucc || f.isOne()) {
                        interval = new Interval<>(1.0, 1.0);
                    } else {
                        String key = f.toString(); // same expression -> same bounds
                        double[] bounds = exprCache.get(key);
                        if (bounds == null) {
                            if (f.isConstant()) {
                                double val = f.asBigRational().doubleValue();
                                bounds = new double[]{val, val};
                            } else {
                                // Translate f into current model (adds aux for bilinear/quadratic via McCormick)
                                GRBLinExpr lin = trans.translateLinearExpression(f.asExpression());
                                model.update();

                                double lower = lin.getConstant();
                                double upper = lin.getConstant();

                                for (int j = 0; j < lin.size(); j++) {
                                    double coeff = lin.getCoeff(j);
                                    double[] bounds_var = varBounds.get(lin.getVar(j));
                                    if (bounds_var == null) {
                                        // This should no longer happen after the bug fix.
                                        System.err.println("Warning: Could not find pre-computed bounds for variable. Skipping term.");
                                        continue;
                                    }

                                    if (coeff > 0) {
                                        lower += coeff * bounds_var[0];
                                        upper += coeff * bounds_var[1];
                                    } else { // coeff < 0
                                        lower += coeff * bounds_var[1];
                                        upper += coeff * bounds_var[0];
                                    }
                                }

                                lower = Math.max(0.0, Math.min(1.0, lower));
                                upper = Math.max(0.0, Math.min(1.0, upper));

                                bounds = new double[]{lower, upper};

                            }
                            exprCache.put(key, bounds);
                        }

                        // Create a fresh Interval so delimit() can safely mutate per-choice copies
                        interval = new Interval<>(bounds[0], bounds[1]);
                    }

                    distrNew.add(t, interval);
                }

                // Ensure each choice is a feasible interval distribution (mutates the per-choice intervals)
                IntervalUtils.delimit(distrNew, Evaluator.forDouble());
                out.addActionLabelledChoice(s, new UDistributionIntervals<>(distrNew), action);
            }
        }

        return out;
    }

    /** Update the translator's McCormick bounds from the *current* model bounds (after OBBT). */
    private void refreshTranslatorBoundsFromModel(ExpressionTranslator trans, GRBModel model) throws GRBException {
        for (GRBVar v : model.getVars()) {
            String name = v.get(GRB.StringAttr.VarName);
            double lb = v.get(GRB.DoubleAttr.LB);
            double ub = v.get(GRB.DoubleAttr.UB);
            if (!Double.isNaN(lb) && !Double.isNaN(ub)) {
                trans.setVarBounds(name, lb, ub);
            }
        }
    }

    private static PrismException asPrismException(String prefix, GRBException e) {
        String msg = (e.getMessage() == null || e.getMessage().isEmpty()) ? "unknown Gurobi error" : e.getMessage();
        return new PrismException(prefix + ": " + msg);
    }

    private static String gurobiStatusToString(int status) {
        switch (status) {
            case GRB.Status.OPTIMAL: return "OPTIMAL";
            case GRB.Status.INFEASIBLE: return "INFEASIBLE";
            case GRB.Status.INF_OR_UNBD: return "INF_OR_UNBD";
            case GRB.Status.UNBOUNDED: return "UNBOUNDED";
            case GRB.Status.SUBOPTIMAL: return "SUBOPTIMAL";
            case GRB.Status.TIME_LIMIT: return "TIME_LIMIT";
            case GRB.Status.ITERATION_LIMIT: return "ITERATION_LIMIT";
            case GRB.Status.NUMERIC: return "NUMERIC";
            default: return "STATUS_" + status;
        }
    }

    private static int optimizeWithRetry(GRBModel model) throws GRBException {
        model.optimize();
        int status = model.get(GRB.IntAttr.Status);

        // INF_OR_UNBD can be ambiguous with dual reductions enabled.
        if (status == GRB.Status.INF_OR_UNBD) {
            int oldDualReductions = model.get(GRB.IntParam.DualReductions);
            try {
                model.set(GRB.IntParam.DualReductions, 0);
                model.optimize();
                status = model.get(GRB.IntAttr.Status);
            } finally {
                model.set(GRB.IntParam.DualReductions, oldDualReductions);
            }
        }
        return status;
    }

    private static void throwIfNonOptimal(GRBModel model, int status, String context) throws GRBException {
        if (status == GRB.Status.OPTIMAL) return;

        String base = context + " not optimal, status=" + status + " (" + gurobiStatusToString(status) + ")";
        if (status == GRB.Status.INFEASIBLE || status == GRB.Status.INF_OR_UNBD) {
            String suffix = "";
            try {
                model.computeIIS();
                model.write("expression_bound_model.ilp");
                suffix = ". IIS written to expression_bound_model.ilp";
            } catch (GRBException ignored) {
                // Keep original failure reason if IIS export itself fails.
            }
            throw new GRBException(base + suffix);
        }
        throw new GRBException(base);
    }

    /** Optimize a linear objective (possibly including translator's aux vars). */
    private static double optimize(GRBModel model, GRBLinExpr obj, int sense) throws GRBException {
        model.setObjective(obj);
        model.set(GRB.IntAttr.ModelSense, sense);
        int status = optimizeWithRetry(model);
        throwIfNonOptimal(model, status, "Expression bound solve");
        return model.get(GRB.DoubleAttr.ObjVal);
    }


    protected Interval<Double> getClopperPearsonInterval(int count, int sacount) {
        if (sacount == 0) {
            return new Interval<>(0.0, 1.0);
        }

        int m = getNumLearnableTransitions();
        double alpha = (1.0 - error_tolerance) / (double) m;

        return computeClopperPearson(sacount, count, alpha);
    }

    private static final BrentSolver INV_BETA_SOLVER = new BrentSolver(1e-5);
    private static final int    MAX_EVAL        = 50;

    /**
     * Invert the regularized incomplete Beta:
     * find x in [0,1] so that Beta.regularizedBeta(x,a,b) = p
     */
    private static double invRegularizedBeta(double p, double a, double b) {
        UnivariateFunction f = x -> Beta.regularizedBeta(x, a, b) - p;
        // solve f(x)=0 on [0,1]
        return INV_BETA_SOLVER.solve(MAX_EVAL, f, 0.0, 1.0);
    }

    private Interval<Double> computeClopperPearson(int n, int k, double alpha) {
        double lower = (k == 0)
                ? 0.0
                : invRegularizedBeta(alpha/2.0, (double)k, (double)(n - k + 1));
        double upper = (k == n)
                ? 1.0
                : invRegularizedBeta(1.0 - alpha/2.0, (double)(k + 1), (double)(n - k));
        return new Interval<>(Math.max(lower, 0.0), Math.min(upper,1.0));
    }

    @Override
    public int getNumLearnableComponents() {
        return this.getNumLearnableTransitions();
    }

    public UMDP<Double> buildApsEllipsoidUMDP(MDPSimple<Function> pmdp) throws GRBException, PrismException {

        // 1) get APS (thetaHat, V, beta)
        double lambda = (ex.apsLambda > 0 ? ex.apsLambda : 1e-2);

        // pick these from Experiment (recommended), otherwise hardcode defaults:
        double R = ex.apsR;          // noise proxy
        double S = ex.apsS;          // ||theta_*|| bound
        double delta = ex.apsDelta;  // confidence (NOT the same as CP’s alpha split)
        ApsEllipsoid ell = estimateApsEllipsoidCountsLS(mdp, pmdp, lambda, R, S, delta);
        UDistribributionParametricConvex.ApsEllipsoidData apsData =
                new UDistribributionParametricConvex.ApsEllipsoidData(ell.thetaHat, ell.V, ell.beta);

        // 2) build a *shared* convex model containing ONLY:
        //    - param bounds (from translator / param declarations)
        //    - simplex constraints for all (s,i)
        //    - APS ellipsoid
        GRBEnv env = new GRBEnv(true);
        env.set(GRB.IntParam.OutputFlag, 0);
        env.start();

        ConvexLearner cxl = new ConvexLearner(env);
        cxl.setParamModel(pmdp);
        cxl.resetModel();
        cxl.commitConstraints();
        ExpressionTranslator trans = cxl.getTranslator();
        GRBModel model = cxl.getModel();

        // ensure all vars are created (important once you have McCormick later)
        for (int s = 0; s < pmdp.getNumStates(); s++) {
            int nc = pmdp.getNumChoices(s);
            for (int i = 0; i < nc; i++) {
                for (Iterator<Map.Entry<Integer, Function>> it = pmdp.getTransitionsIterator(s, i); it.hasNext();) {
                    Function f = it.next().getValue();
                    if (f.isConstant() || f.isOne()) continue;
                    trans.translateLinearExpression(f.asExpression());
                }
            }
        }
        model.update();

        // add probability-validity constraints globally (linear)
        addGlobalSimplexConstraints(pmdp, model, trans);

        // add APS ellipsoid (convex quadratic)
        addEllipsoidConstraint(model, ell.paramNames, ell.thetaHat, ell.V, ell.beta);

        model.update();

        // 3) Optional: intervalize under the APS ellipsoid to avoid SOCP-per-backup in robust VI.
        //    This mirrors the LP intervalization path: bound each unique transition expression once.
        if (ex.useLPToIntervals) {
            UMDPSimple<Double> ellImdp = (ex.intervalAbstractionMode == EXACT)
                    ? buildIntervalizedUMDPFromConvexModel(model, trans, pmdp, /*isQcp=*/true)
                    : buildIntervalizedUMDPFromConvexModelIntervalArithmetic(model, trans, pmdp, /*isQcp=*/true);

            ellImdp.addInitialState(pmdp.getFirstInitialState());
            ellImdp.setStatesList(pmdp.getStatesList());
            ellImdp.setConstantValues(pmdp.getConstantValues());
            for (Map.Entry<String, BitSet> entry : pmdp.getLabelToStatesMap().entrySet()) {
                ellImdp.addLabel(entry.getKey(), entry.getValue());
            }
            this.convex_estimate = ellImdp;

            // Intervalized path no longer needs the shared Gurobi model
            try { model.dispose(); } catch (Throwable ignore) {}
            try { env.dispose(); } catch (Throwable ignore) {}

            return ellImdp;
        }

        // 3) build the UMDP topology from pmdp but with ellipsoid-uncertain distributions
        int n = pmdp.getNumStates();
        UMDPSimple<Double> out = new UMDPSimple<>(n);
        out.addInitialState(pmdp.getFirstInitialState());
        out.setStatesList(pmdp.getStatesList());
        out.setConstantValues(pmdp.getConstantValues());

        for (int s = 0; s < n; s++) {
            int numChoices = pmdp.getNumChoices(s);
            for (int i = 0; i < numChoices; i++) {
                String action = getActionString(pmdp, s, i);
                Distribution<Function> pdist = pmdp.getDistribution(s, i);
                out.addActionLabelledChoice(
                        s,
                        new UDistribributionParametricConvex<>(pdist, model, trans, null, apsData),
                        action
                );
            }
        }

        for (Map.Entry<String, BitSet> entry : pmdp.getLabelToStatesMap().entrySet()) {
            out.addLabel(entry.getKey(), entry.getValue());
        }

        // Keep a reference so it doesn't get GC’d; also mirrors your convex_estimate pattern
        this.convex_estimate = out;
        return out;
    }

    private ApsEllipsoid estimateApsEllipsoidCountsLS(MDP<Double> mdp,
                                                      MDPSimple<Function> pmdp,
                                                      double lambda,
                                                      double R,
                                                      double S,
                                                      double delta) throws GRBException, PrismException {

        GRBEnv env = new GRBEnv(true);
        env.set(GRB.IntParam.OutputFlag, 0);
        env.start();

        ConvexLearner cxl = new ConvexLearner(env);
        cxl.setParamModel(pmdp);
        cxl.resetModel();
        cxl.commitConstraints();
        ExpressionTranslator trans = cxl.getTranslator();

        // warm-up param discovery
        for (int s = 0; s < pmdp.getNumStates(); s++) {
            int nc = pmdp.getNumChoices(s);
            for (int i = 0; i < nc; i++) {
                boolean singleSucc = pmdp.getDistribution(s, i).getSupport().size() == 1;
                for (Iterator<Map.Entry<Integer, Function>> it = pmdp.getTransitionsIterator(s, i); it.hasNext();) {
                    Function f = it.next().getValue();
                    if (singleSucc || f.isOne() || f.isConstant()) continue;
                    trans.translateLinearExpression(f.asExpression());
                }
            }
        }
        cxl.getModel().update();

        final int d = trans.getNumDecisionVars();
        if (d == 0) return new ApsEllipsoid(new double[0], new double[0][0], 0.0, List.of());

        final int[] p2m = buildStateIndexMap(pmdp, mdp);

        double[][] sumAtAt = new double[d][d];
        double[] sumAtYc = new double[d];

        for (int sP = 0; sP < pmdp.getNumStates(); sP++) {
            int sM = p2m[sP];
            if (sM < 0) continue;

            int numChoicesP = pmdp.getNumChoices(sP);
            for (int iP = 0; iP < numChoicesP; iP++) {
                final String action = getActionString(pmdp, sP, iP);

                final int n = getStateActionCountRaw(new StateActionPair(sM, action));
                if (n == 0) continue;

                List<Integer> tMs = new ArrayList<>();
                List<double[]> aRows = new ArrayList<>();
                List<Double> cRows = new ArrayList<>();

                for (Iterator<Map.Entry<Integer, Function>> it = pmdp.getTransitionsIterator(sP, iP); it.hasNext();) {
                    Map.Entry<Integer, Function> e = it.next();
                    int tP = e.getKey();
                    int tM = (tP >= 0 && tP < p2m.length) ? p2m[tP] : -1;
                    if (tM < 0) continue;

                    Affine af = extractAffine(e.getValue(), trans);
                    tMs.add(tM);
                    aRows.add(af.a);
                    cRows.add(af.c);
                }
                if (tMs.isEmpty()) continue;

                for (int j = 0; j < tMs.size(); j++) {
                    double k = getTransitionCountRaw(new TransitionTriple(sM, action, tMs.get(j)));
                    double[] a = aRows.get(j);
                    double c = cRows.get(j);
                    double residual = k - ((double) n) * c;

                    for (int r = 0; r < d; r++) {
                        double ar = a[r];
                        if (ar == 0.0) continue;
                        for (int col = 0; col < d; col++) {
                            sumAtAt[r][col] += ((double) n) * ar * a[col];
                        }
                    }
                    for (int r = 0; r < d; r++) sumAtYc[r] += a[r] * residual;
                }
            }
        }

        double[][] V = new double[d][d];
        for (int i = 0; i < d; i++) {
            System.arraycopy(sumAtAt[i], 0, V[i], 0, d);
            V[i][i] += lambda;
        }

        // symmetrize defensively
        for (int i = 0; i < d; i++) {
            for (int j = i + 1; j < d; j++) {
                double s = 0.5 * (V[i][j] + V[j][i]);
                V[i][j] = s; V[j][i] = s;
            }
        }

        double[] thetaHat = solveSPDCholesky(V, sumAtYc);

        // IMPORTANT: radius uses V (with lambda) and delta
        double beta = apsBeta(V, lambda, R, S, delta);

        System.out.println("Theta Order: " + trans.getDecisionNames());
        System.out.println("APS thetaHat: " + Arrays.toString(thetaHat) + " beta=" + beta);

        try { cxl.getModel().dispose(); } catch (Throwable ignore) {}
        try { env.dispose(); } catch (Throwable ignore) {}

        // trans.paramNames used in your existing code, so reuse it here
        @SuppressWarnings("unchecked")
        List<String> names = (List<String>) trans.getDecisionNames();

        return new ApsEllipsoid(thetaHat, V, beta, names);
    }

    private static void addEllipsoidConstraint(GRBModel model,
                                               List<String> paramNames,
                                               double[] mu,
                                               double[][] V,
                                               double beta) throws GRBException {

        int d = mu.length;
        GRBVar[] x = new GRBVar[d];
        for (int j = 0; j < d; j++) {
            x[j] = model.getVarByName(paramNames.get(j));
            if (x[j] == null) throw new GRBException("Parameter var not found: " + paramNames.get(j));
        }

        // y = x - mu
        GRBVar[] y = new GRBVar[d];
        for (int j = 0; j < d; j++) {
            y[j] = model.addVar(-GRB.INFINITY, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "d_" + paramNames.get(j));
            GRBLinExpr eq = new GRBLinExpr();
            eq.addTerm(1.0, x[j]);
            eq.addTerm(-1.0, y[j]);
            eq.addConstant(-mu[j]);
            model.addConstr(eq, GRB.EQUAL, 0.0, "shift_" + j);
        }

        // y^T V y <= beta^2
        GRBQuadExpr q = new GRBQuadExpr();
        for (int i = 0; i < d; i++) {
            q.addTerm(V[i][i], y[i], y[i]);
            for (int j = i + 1; j < d; j++) {
                q.addTerm(2.0 * V[i][j], y[i], y[j]);
            }
        }
        model.addQConstr(q, GRB.LESS_EQUAL, beta * beta, "aps_ellipsoid");
    }

    private static void addGlobalSimplexConstraints(MDPSimple<Function> pmdp,
                                                    GRBModel model,
                                                    ExpressionTranslator trans) throws GRBException, PrismException {

        // IMPORTANT:
        //  - NO model.update() in here.
        //  - Only add constraints that are actually needed:
        //      * p >= 0 for each successor-probability expression
        //      * sum_s' p(s') == 1  (with constants folded into the LHS)
        //    We do NOT add p <= 1 because it's implied by (p >= 0 && sum == 1).

        for (int s = 0; s < pmdp.getNumStates(); s++) {
            int nc = pmdp.getNumChoices(s);
            for (int i = 0; i < nc; i++) {

                // deterministic choice => nothing to constrain
                if (pmdp.getDistribution(s, i).getSupport().size() == 1) continue;

                GRBLinExpr sum = new GRBLinExpr();
                int succIdx = 0;

                for (Iterator<Map.Entry<Integer, Function>> it = pmdp.getTransitionsIterator(s, i); it.hasNext(); ) {
                    Map.Entry<Integer, Function> e = it.next();
                    Function f = e.getValue();

                    // Constant probabilities can be folded into the sum directly.
                    if (f.isConstant()) {
                        sum.addConstant(f.asBigRational().doubleValue());
                        succIdx++;
                        continue;
                    }
                    if (f.isOne()) {
                        sum.addConstant(1.0);
                        succIdx++;
                        continue;
                    }

                    // Translate probability expression p(θ)
                    GRBLinExpr p = trans.translateLinearExpression(f.asExpression());

                    // Add p into row-sum
                    sum.addConstant(p.getConstant());
                    for (int k = 0; k < p.size(); k++) {
                        sum.addTerm(p.getCoeff(k), p.getVar(k));
                    }

                    // Nonnegativity constraint (unique name!)
                    model.addConstr(
                            p,
                            GRB.GREATER_EQUAL,
                            0.0,
                            "p_ge_0_" + s + "_" + i + "_" + succIdx
                    );

                    succIdx++;
                }

                // Probability simplex row-sum:
                model.addConstr(sum, GRB.EQUAL, 1.0, "psum_" + s + "_" + i);
            }
        }
    }

    /**
     * Build a mapping pState -> mState by matching explicit state valuations from statesList.
     * This avoids relying on "same index ordering", which is NOT guaranteed across mdp vs pmdp.
     */
    private static int[] buildStateIndexMap(MDP<?> pmdp, MDP<?> mdp) {
        int nP = pmdp.getNumStates();
        int nM = mdp.getNumStates();

        // Build lookup for concrete mdp states by a stable key (string of valuation).
        // Using toString() is usually stable for PRISM explicit State.
        Map<String, Integer> keyToM = new HashMap<>(nM * 2);

        for (int sM = 0; sM < nM; sM++) {
            Object st = mdp.getStatesList().get(sM);
            keyToM.put(String.valueOf(st), sM);
        }

        int[] p2m = new int[nP];
        Arrays.fill(p2m, -1);

        int matched = 0;
        for (int sP = 0; sP < nP; sP++) {
            Object st = pmdp.getStatesList().get(sP);
            Integer sM = keyToM.get(String.valueOf(st));
            if (sM != null) {
                p2m[sP] = sM;
                matched++;
            }
        }

        return p2m;
    }

    /** f(θ) = c + a^T θ */
    private static final class Affine {
        final double c;
        final double[] a;
        Affine(double c, double[] a) { this.c = c; this.a = a; }
    }

    private Affine extractAffine(Function f, ExpressionTranslator trans) throws GRBException, PrismException {
        final int d = trans.getNumDecisionVars();

        // Constant function
        if (f.isConstant()) {
            return new Affine(f.asBigRational().doubleValue(), new double[d]);
        }

        GRBLinExpr lin = trans.translateLinearExpression(f.asExpression());
        double c0 = lin.getConstant();
        double[] a = new double[d];

        for (int k = 0; k < lin.size(); k++) {
            GRBVar v = lin.getVar(k);
            double coeff = lin.getCoeff(k);
            if (Math.abs(coeff) < 1e-15) continue;

            // Decision var? (parameter OR McCormick aux var)
            int j = trans.getDecisionIndex(v);
            if (j >= 0) {
                a[j] += coeff;
                continue;
            }

            // Fold fixed vars into intercept (covers c_3.0, c_0.05, etc.)
            double lb = v.get(GRB.DoubleAttr.LB);
            double ub = v.get(GRB.DoubleAttr.UB);
            if (Double.isFinite(lb) && Double.isFinite(ub) && Math.abs(ub - lb) < 1e-12) {
                c0 += coeff * lb;
                continue;
            }

            String name = v.get(GRB.StringAttr.VarName);
            throw new RuntimeException(
                    "Non-decision, non-fixed var in supposedly linear function: " + name +
                            " coeff=" + coeff + " LB=" + lb + " UB=" + ub + " in " + f
            );
        }

        return new Affine(c0, a);
    }


    /** Solve SPD system V x = b using Cholesky (V must be symmetric positive definite). */
    private static double[] solveSPDCholesky(double[][] V, double[] b) {
        int n = b.length;
        double[][] L = new double[n][n];

        // Cholesky: V = L L^T
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= i; j++) {
                double sum = V[i][j];
                for (int k = 0; k < j; k++) sum -= L[i][k] * L[j][k];
                if (i == j) {
                    if (sum <= 0) sum = 1e-18; // defensive
                    L[i][j] = Math.sqrt(sum);
                } else {
                    L[i][j] = sum / L[j][j];
                }
            }
        }

        // Forward solve L y = b
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = b[i];
            for (int k = 0; k < i; k++) sum -= L[i][k] * y[k];
            y[i] = sum / L[i][i];
        }

        // Back solve L^T x = y
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = y[i];
            for (int k = i + 1; k < n; k++) sum -= L[k][i] * x[k];
            x[i] = sum / L[i][i];
        }
        return x;
    }

    /** Raw empirical N(s,a) from simulation only (no priors). */
    private int getStateActionCountRaw(StateActionPair sa) {
        Integer v = sampleSizeMap.get(sa);
        return (v == null) ? 0 : v;
    }

    /** Raw empirical K(s,a,t) from simulation only (no priors). */
    private int getTransitionCountRaw(TransitionTriple tr) {
        Integer v = samplesMap.get(tr);
        return (v == null) ? 0 : v;
    }

    // Put near other helpers in PACConvexEstimator

    private static final class ApsEllipsoid {
        final double[] thetaHat;
        final double[][] V;    // SPD
        final double beta;
        final List<String> paramNames; // same order as thetaHat
        ApsEllipsoid(double[] thetaHat, double[][] V, double beta, List<String> paramNames) {
            this.thetaHat = thetaHat; this.V = V; this.beta = beta; this.paramNames = paramNames;
        }
    }

    /** log(det(M)) for SPD M via Cholesky. */
    private static double logDetSPD(double[][] M) {
        int n = M.length;
        double[][] L = new double[n][n];
        double logDet = 0.0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= i; j++) {
                double sum = M[i][j];
                for (int k = 0; k < j; k++) sum -= L[i][k] * L[j][k];
                if (i == j) {
                    if (sum <= 0) sum = 1e-18;
                    L[i][j] = Math.sqrt(sum);
                    logDet += 2.0 * Math.log(L[i][j]);
                } else {
                    L[i][j] = sum / L[j][j];
                }
            }
        }
        return logDet;
    }

    /**
     * Standard Abbasi-Yadkori style radius:
     * beta = R * sqrt( log(det(V)) - d log(lambda) + 2 log(1/delta) ) + sqrt(lambda) * S
     */
    private static double apsBeta(double[][] V, double lambda, double R, double S, double delta) {
        int d = V.length;
        double logdetV = logDetSPD(V);
        double term = (logdetV - d * Math.log(lambda)) + 2.0 * Math.log(1.0 / delta);
        term = Math.max(0.0, term);
        return R * Math.sqrt(term) + Math.sqrt(lambda) * S;
    }

    /**
     * Intervalize an IMDP by solving min/max for each unique transition Function over an already-built
     * convex feasibility set.
     *
     * Works for both:
     *  - pure LP feasibility sets (polytope constraints only)
     *  - APS ellipsoid feasibility sets (convex quadratic constraint + linear constraints)
     *
     * The only difference is the solver method: for QCP we must NOT force dual-simplex.
     */
    private UMDPSimple<Double> buildIntervalizedUMDPFromConvexModel(GRBModel baseModel,
                                                                    ExpressionTranslator trans,
                                                                    MDPSimple<Function> pmdp,
                                                                    boolean isQcp)
            throws GRBException, PrismException {

        // Keep McCormick bounds in sync with current model bounds (OBBT may have tightened them).
        refreshTranslatorBoundsFromModel(trans, baseModel);

        final int n = pmdp.getNumStates();
        UMDPSimple<Double> out = new UMDPSimple<>(n);

        // Cache numeric bounds [lo, hi] per unique expression (keyed by f.toString()).
        final Map<String, double[]> exprCache = new ConcurrentHashMap<>();

        // ---- 1) Collect & translate all unique non-constant Functions ONCE (serial) ----
        final LinkedHashMap<String, ObjSpec> todoSpecs = new LinkedHashMap<>();
        for (int s = 0; s < n; s++) {
            int numChoices = pmdp.getNumChoices(s);
            for (int i = 0; i < numChoices; i++) {
                boolean singleSucc = pmdp.getDistribution(s, i).getSupport().size() == 1;
                for (Iterator<Map.Entry<Integer, Function>> it = pmdp.getTransitionsIterator(s, i); it.hasNext();) {
                    Map.Entry<Integer, Function> e = it.next();
                    Function f = e.getValue();

                    if (singleSucc || f.isOne()) continue; // fixed 1.0

                    final String key = f.toString();
                    if (exprCache.containsKey(key) || todoSpecs.containsKey(key)) continue;

                    if (f.isConstant()) {
                        double v = f.asBigRational().doubleValue();
                        exprCache.put(key, new double[]{v, v});
                        continue;
                    }

                    // Translate now (may add aux vars/cons); no worker mutates the model.
                    GRBLinExpr lin = trans.translateLinearExpression(f.asExpression());

                    // Extract objective spec (names + coeffs + constant) for rebuild in worker copies
                    final int terms = lin.size();
                    final String[] names = new String[terms];
                    final double[] coeffs = new double[terms];
                    for (int k = 0; k < terms; k++) {
                        names[k]  = lin.getVar(k).get(GRB.StringAttr.VarName);
                        coeffs[k] = lin.getCoeff(k);
                    }
                    final double constant = lin.getConstant();
                    todoSpecs.put(key, new ObjSpec(names, coeffs, constant));
                }
            }
        }

        if (!todoSpecs.isEmpty()) {
            baseModel.update(); // freeze before copying / optimizing

            if (ex.exprBoundWorkers > 1) {
                // ---- 2a) PARALLEL: copy the frozen model per worker env ----
                final int workerCount = ex.exprBoundWorkers;
                final int threadsPerWorker = 1;

                final ExecutorService pool = Executors.newFixedThreadPool(workerCount);

                final List<WorkerHandle> workers = new ArrayList<>(workerCount);
                for (int w = 0; w < workerCount; w++) {
                    GRBEnv env = new GRBEnv(true);
                    env.set(GRB.IntParam.LogToConsole, 0);
                    env.set(GRB.IntParam.OutputFlag, 0);
                    env.start();

                    GRBModel m = new GRBModel(baseModel, env);
                    m.set(GRB.IntParam.LogToConsole, 0);
                    m.set(GRB.IntParam.OutputFlag, 0);
                    m.set(GRB.IntParam.Threads, threadsPerWorker);

                    // For LP we like dual simplex; for QCP let Gurobi pick (or barrier).
                    if (!isQcp) {
                        m.set(GRB.IntParam.Method, 1);
                    }

                    workers.add(new WorkerHandle(env, m));
                }

                final List<Map.Entry<String, ObjSpec>> all = new ArrayList<>(todoSpecs.entrySet());
                final List<List<Map.Entry<String, ObjSpec>>> chunks = partition(all, workerCount);

                final List<Future<?>> futures = new ArrayList<>();
                for (int w = 0; w < chunks.size(); w++) {
                    final List<Map.Entry<String, ObjSpec>> chunk = chunks.get(w);
                    final WorkerHandle wh = workers.get(w);
                    futures.add(pool.submit(() -> {
                        try {
                            for (Map.Entry<String, ObjSpec> entry : chunk) {
                                final String key = entry.getKey();
                                final ObjSpec spec = entry.getValue();
                                final double[] b = solveMinMaxOn(wh.model, spec);

                                double lo = clamp(b[0], precision, 1.0 - precision);
                                double hi = clamp(b[1], precision, 1.0 - precision);
                                if (hi < lo) { double t = lo; lo = hi; hi = t; }

                                exprCache.put(key, new double[]{lo, hi});
                            }
                        } finally {
                            try { wh.model.dispose(); } catch (Throwable ignore) {}
                            try { wh.env.dispose(); }   catch (Throwable ignore) {}
                        }
                        return null;
                    }));
                }

                for (Future<?> f : futures) {
                    try { f.get(); }
                    catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw new RuntimeException("Interrupted", ie); }
                    catch (ExecutionException ee) { throw new RuntimeException("Worker failed", ee.getCause()); }
                }
                pool.shutdown();

            } else {
                // ---- 2b) SERIAL: reuse ONE model; change objective; warm re-solve ----
                final int oldMethod  = baseModel.get(GRB.IntParam.Method);
                final int oldThreads = baseModel.get(GRB.IntParam.Threads);
                final int oldOut     = baseModel.get(GRB.IntParam.OutputFlag);
                try {
                    baseModel.set(GRB.IntParam.OutputFlag, 0);

                    // Only force dual-simplex in the LP case.
                    if (!isQcp) {
                        baseModel.set(GRB.IntParam.Method, 1);
                    }

                    for (Map.Entry<String, ObjSpec> e : todoSpecs.entrySet()) {
                        final String key = e.getKey();
                        final ObjSpec spec = e.getValue();

                        GRBLinExpr expr = new GRBLinExpr();
                        for (int k = 0; k < spec.names.length; k++) {
                            expr.addTerm(spec.coeffs[k], baseModel.getVarByName(spec.names[k]));
                        }
                        expr.addConstant(spec.constant);

                        double lo = optimize(baseModel, expr, GRB.MINIMIZE);
                        double hi = optimize(baseModel, expr, GRB.MAXIMIZE);

                        lo = clamp(lo, precision, 1.0 - precision);
                        hi = clamp(hi, precision, 1.0 - precision);
                        if (hi < lo) { double t = lo; lo = hi; hi = t; }

                        exprCache.put(key, new double[]{lo, hi});
                    }
                } finally {
                    baseModel.set(GRB.IntParam.Method, oldMethod);
                    baseModel.set(GRB.IntParam.Threads, oldThreads);
                    baseModel.set(GRB.IntParam.OutputFlag, oldOut);
                }
            }
        }

        // ---- 3) Build the UMDP using cached bounds ----
        for (int s = 0; s < n; s++) {
            int numChoices = pmdp.getNumChoices(s);
            for (int i = 0; i < numChoices; i++) {
                final String action = getActionString(pmdp, s, i);
                Distribution<Interval<Double>> distrNew = new Distribution<>(Evaluator.forDoubleInterval());
                boolean singleSucc = pmdp.getDistribution(s, i).getSupport().size() == 1;

                for (Iterator<Map.Entry<Integer, Function>> it = pmdp.getTransitionsIterator(s, i); it.hasNext();) {
                    Map.Entry<Integer, Function> e = it.next();
                    int t = e.getKey();
                    Function f = e.getValue();

                    final Interval<Double> interval;
                    if (singleSucc || f.isOne()) {
                        interval = new Interval<>(1.0, 1.0);
                    } else {
                        final String key = f.toString();
                        double[] b = exprCache.get(key);
                        if (b == null) {
                            double v = f.isConstant() ? f.asBigRational().doubleValue() : 0.0;
                            b = new double[]{v, v};
                        }
                        interval = new Interval<>(b[0], b[1]);
                    }
                    distrNew.add(t, interval);
                }

                IntervalUtils.delimit(distrNew, Evaluator.forDouble());
                out.addActionLabelledChoice(s, new UDistributionIntervals<>(distrNew), action);
            }
        }

        return out;
    }

    /**
     * Intervalize an IMDP using interval arithmetic (no per-expression optimization)
     * over an already-built convex model (LP/QCP). This is the ellipsoid analogue of
     * buildIntervalizedUMDPFromLPIntervalArithmetic(...).
     *
     * Assumption: all vars (including McCormick aux vars) already exist and model.update() was called.
     */
    private UMDPSimple<Double> buildIntervalizedUMDPFromConvexModelIntervalArithmetic(GRBModel model,
                                                                                      ExpressionTranslator trans,
                                                                                      MDPSimple<Function> pmdp,
                                                                                      boolean isQcp)
            throws GRBException, PrismException {

        // reflect current model bounds into the translator (important for McCormick)
        refreshTranslatorBoundsFromModel(trans, model);

        final int n = pmdp.getNumStates();
        UMDPSimple<Double> out = new UMDPSimple<>(n);

        // cache raw bounds only (Interval objects get mutated by delimit())
        Map<String, double[]> exprCache = new HashMap<>();

        // 1) Precompute bounds for every model variable
        Map<GRBVar, double[]> varBounds = new HashMap<>();
        GRBVar[] vars = model.getVars();

        for (GRBVar v : vars) {
            double lbAttr = v.get(GRB.DoubleAttr.LB);
            double ubAttr = v.get(GRB.DoubleAttr.UB);

            if (Math.abs(ubAttr - lbAttr) < 1e-9) {
                varBounds.put(v, new double[]{lbAttr, lbAttr});
            } else {
                GRBLinExpr obj = new GRBLinExpr();
                obj.addTerm(1.0, v);

                double lbOpt = optimize(model, obj, GRB.MINIMIZE);
                double ubOpt = optimize(model, obj, GRB.MAXIMIZE);

                varBounds.put(v, new double[]{lbOpt, ubOpt});
            }
        }

        // 2) Build the interval IMDP by bounding each expression via interval arithmetic
        for (int s = 0; s < n; s++) {
            int numChoices = pmdp.getNumChoices(s);
            for (int i = 0; i < numChoices; i++) {
                final String action = getActionString(pmdp, s, i);
                Distribution<Interval<Double>> distrNew = new Distribution<>(Evaluator.forDoubleInterval());

                boolean singleSucc = pmdp.getDistribution(s, i).getSupport().size() == 1;

                for (Iterator<Map.Entry<Integer, Function>> it = pmdp.getTransitionsIterator(s, i); it.hasNext();) {
                    Map.Entry<Integer, Function> e = it.next();
                    int t = e.getKey();
                    Function f = e.getValue();

                    Interval<Double> interval;
                    if (singleSucc || f.isOne()) {
                        interval = new Interval<>(1.0, 1.0);
                    } else {
                        String key = f.toString();
                        double[] bounds = exprCache.get(key);

                        if (bounds == null) {
                            if (f.isConstant()) {
                                double val = f.asBigRational().doubleValue();
                                bounds = new double[]{val, val};
                            } else {
                                // must already be linearized by McCormick in the translator
                                GRBLinExpr lin = trans.translateLinearExpression(f.asExpression());

                                double lower = lin.getConstant();
                                double upper = lin.getConstant();

                                for (int j = 0; j < lin.size(); j++) {
                                    double coeff = lin.getCoeff(j);
                                    GRBVar var = lin.getVar(j);
                                    double[] bnd = varBounds.get(var);

                                    if (bnd == null) {
                                        // defensive fallback: use attribute bounds
                                        double lbA = var.get(GRB.DoubleAttr.LB);
                                        double ubA = var.get(GRB.DoubleAttr.UB);
                                        bnd = new double[]{lbA, ubA};
                                    }

                                    if (coeff > 0) {
                                        lower += coeff * bnd[0];
                                        upper += coeff * bnd[1];
                                    } else {
                                        lower += coeff * bnd[1];
                                        upper += coeff * bnd[0];
                                    }
                                }

                                lower = Math.max(0.0, Math.min(1.0, lower));
                                upper = Math.max(0.0, Math.min(1.0, upper));
                                bounds = new double[]{lower, upper};
                            }

                            exprCache.put(key, bounds);
                        }

                        interval = new Interval<>(bounds[0], bounds[1]);
                    }

                    distrNew.add(t, interval);
                }

                IntervalUtils.delimit(distrNew, Evaluator.forDouble());
                out.addActionLabelledChoice(s, new UDistributionIntervals<>(distrNew), action);
            }
        }

        return out;
    }


}
