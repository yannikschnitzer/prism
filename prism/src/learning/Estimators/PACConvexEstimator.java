package learning.Estimators;

import com.gurobi.gurobi.*;
import common.Interval;
import explicit.*;
import imdpcomp.Experiment;
import learning.ParametricConvex.ConvexLearner;
import learning.ParametricConvex.ExpressionTranslator;
import learning.Simulation.StateActionPair;
import learning.Simulation.TransitionTriple;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.special.Beta;
import param.Function;
import prism.Evaluator;
import prism.Prism;
import prism.PrismException;
import prism.Result;
import strat.MDStrategy;
import strat.MDStrategyArray;

import java.util.*;

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
        MDStrategy<Double> robustStrat = ex.doBisim ? liftStrategy((MDStrategyArray<Double>) resultRobustConvex.getStrategy(), mdp) : (MDStrategy<Double>) resultRobustConvex.getStrategy();
        MDStrategy<Double> optimisticStrat = ex.doBisim ? liftStrategy((MDStrategyArray<Double>) resultOptimisticConvex.getStrategy(), mdp) : (MDStrategy<Double>) resultOptimisticConvex.getStrategy();
        this.currentStrat = optimisticStrat;

        startTime = System.nanoTime();
        double resconvexDTMC = round((Double) checkDTMC(robustStrat).getResult());
        modelCheckingTimeDTMC = System.nanoTime() - startTime;

        double resultConvexOptimisticDTMC = round((Double) checkDTMC(optimisticStrat).getResult());

        System.out.println("Convex Guarantee: " + resconvexMDP + ", Convex Performance: " + resconvexDTMC);

        return new double[]{resconvexMDP, resconvexDTMC, resultConvexOptimisticDTMC, modelBuildingTime, modelCheckingTimeRobust, modelCheckingTimeOptimistic, modelCheckingTimeDTMC};
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

            System.out.println("IMDP Ground: " + imdpGround);
            System.out.println("IMDP Bisim: " + imdpBisim);
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

        if (useVertexPrecomp) {
            cxl.setVertexCap(10000);
            cxl.precomputeVertices();
        }

        // Printing Model
        ConvexLearner.printModel(cxl.getModel());

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
