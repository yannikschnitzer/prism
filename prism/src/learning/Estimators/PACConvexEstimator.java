package learning.Estimators;

import com.gurobi.gurobi.*;
import common.Interval;
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
    boolean useVertexPrecomp = true;
    boolean verbose_bisim = false;

    // For parameter-tying in IMDP
    protected HashMap<TransitionTriple, Double> tiedModes = new HashMap<>();
    protected HashMap<TransitionTriple, Integer> tiedTransitionCounts = new HashMap<>();
    protected HashMap<TransitionTriple, Integer> tiedStateActionCounts = new HashMap<>();


    public PACConvexEstimator(Prism prism, Experiment ex) {
        super(prism, ex);
        error_tolerance = ex.error_tolerance;
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
                return new Interval<>(precision, 1 - precision);
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
                UMDP<Double> convex_mdp = buildConvexUMDP(imdp, this.pmdp);
                modelBuildingTime = System.nanoTime() - startTime;

                startTime = System.nanoTime();
                resultRobustConvex = modelCheckPointEstimate(convex_mdp, true, false);
                modelCheckingTimeRobust = System.nanoTime() - startTime;

                startTime = System.nanoTime();
                resultOptimisticConvex = modelCheckPointEstimate(convex_mdp, false, false);
                modelCheckingTimeOptimistic = System.nanoTime() - startTime;
            }
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
        double resconvexMDP = round((Double) resultRobustConvex.getResult());
        double resconvexMDPOptimistic = round((Double) resultOptimisticConvex.getResult());
        MDStrategy<Double> robustStrat = ex.doBisim ? liftStrategy((MDStrategyArray<Double>) resultRobustConvex.getStrategy(), mdp) : (MDStrategy<Double>) resultRobustConvex.getStrategy();
        MDStrategy<Double> optimisticStrat = ex.doBisim ? liftStrategy((MDStrategyArray<Double>) resultOptimisticConvex.getStrategy(), mdp) : (MDStrategy<Double>) resultOptimisticConvex.getStrategy();
        this.currentStrat = optimisticStrat;

        Result uncDTMCres = checkUncDTMC(robustStrat, convex_estimate);
        double resUDTMC = round((Double) uncDTMCres.getResult());

        startTime = System.nanoTime();
        double resconvexDTMC = round((Double) checkDTMC(robustStrat).getResult());
        modelCheckingTimeDTMC = System.nanoTime() - startTime;

        double resultConvexOptimisticDTMC = round((Double) checkDTMC(optimisticStrat).getResult());

        System.out.println("Convex Guarantee: " + resconvexMDP + ", Convex Performance: " + resconvexDTMC);
        System.out.println("Optimistic Guarantee: " + resultOptimisticConvex.getResult());
        System.out.println("UDTMC Guarantee: " + uncDTMCres.getResult());

        return new double[]{resconvexMDP, resconvexDTMC, resultConvexOptimisticDTMC, modelBuildingTime, modelCheckingTimeRobust, modelCheckingTimeOptimistic, modelCheckingTimeDTMC, resUDTMC};
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
                throw new RuntimeException(e);
            }
            return new double[]{resultRobustMDP, resultRobustDTMC, resultOptimisticDTMC, modelBuildingTime, modelCheckingTimeRobust, modelCheckingTimeOptimistic, modelCheckingTimeDTMC};
        } else {
            double[] res = super.getInitialResults();
            try {
                buildConvexUMDP(this.estimate, this.pmdp);
            } catch (GRBException e) {
                throw new RuntimeException(e);
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
                    if (0 < p && p < 1.0) {
                        interval = getTransitionInterval(t);
                        distrNew.add(sTo, interval);
                        this.intervalsMap.put(t, interval);
                    } else if (p == 1.0) {
                        interval = new Interval<>(p, p);
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
                                    ? new Interval<>(precision, 1.0 - precision)
                                    : computeClopperPearson(N, K, alphaPerCI);
                        }
                    } else {
                        int[] kn = edgeCounts.get(packEdgeKey(sAbs, action, tAbs));
                        int K = kn[0], N = kn[1];
                        if (f.isOne() || pmdpBisim.getDistribution(sAbs, iAbs).getSupport().size() == 1) {
                            interval = new Interval<>(1.0, 1.0);
                        } else {
                            interval = (N == 0)
                                    ? new Interval<>(precision, 1.0 - precision)
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
        GRBEnv env = new GRBEnv(true);
        env.set(GRB.IntParam.OutputFlag, 0);
        env.start();

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
        ConvexLearner.printModel(cxl.getModel());

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
        GRBEnv env = new GRBEnv(true);
        env.set(GRB.IntParam.OutputFlag, 0);
        env.start();

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
        ConvexLearner.printModel(cxl.getModel());

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

                        baseModel.setObjective(expr, GRB.MINIMIZE);
                        baseModel.optimize();
                        double lo = baseModel.get(GRB.DoubleAttr.ObjVal);

                        baseModel.setObjective(expr, GRB.MAXIMIZE);
                        baseModel.optimize();
                        double hi = baseModel.get(GRB.DoubleAttr.ObjVal);

                        lo = clamp(lo, precision, 1.0 - precision);
                        hi = clamp(hi, precision, 1.0 - precision);
                        if (hi < lo) { double t = lo; lo = hi; hi = t; }

                        exprCache.put(key, new double[]{lo, hi});
                    }
                } finally {
                    baseModel.set(GRB.IntParam.Method, oldMethod);
                    baseModel.set(GRB.IntParam.Threads, oldThreads);
                    baseModel.set(GRB.IntParam.OutputFlag, 0);
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

        m.setObjective(expr, GRB.MINIMIZE);
        m.optimize();
        double lo = m.get(GRB.DoubleAttr.ObjVal);

        m.setObjective(expr, GRB.MAXIMIZE);
        m.optimize();
        double hi = m.get(GRB.DoubleAttr.ObjVal);

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

    /** Optimize a linear objective (possibly including translator's aux vars). */
    private static double optimize(GRBModel model, GRBLinExpr obj, int sense) throws GRBException {
        model.setObjective(obj);
        model.set(GRB.IntAttr.ModelSense, sense);
        model.optimize();
        int status = model.get(GRB.IntAttr.Status);
        if (status != GRB.Status.OPTIMAL) {
            throw new GRBException("Expression bound solve not optimal, status=" + status);
        }
        return model.get(GRB.DoubleAttr.ObjVal);
    }


    protected Interval<Double> getClopperPearsonInterval(int count, int sacount) {
        if (sacount == 0) {
            return new Interval<>(precision, 1 - precision);
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
                ? precision
                : invRegularizedBeta(alpha/2.0, (double)k, (double)(n - k + 1));
        double upper = (k == n)
                ? 1.0 - precision
                : invRegularizedBeta(1.0 - alpha/2.0, (double)(k + 1), (double)(n - k));
        return new Interval<>(Math.max(lower, precision), Math.min(upper,1-precision));
    }

    @Override
    public int getNumLearnableComponents() {
        return this.getNumLearnableTransitions();
    }
}
