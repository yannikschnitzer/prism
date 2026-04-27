package learning.Estimators;

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBException;
import com.gurobi.gurobi.GRBModel;
import common.Interval;
import common.GurobiEnvManager;
import explicit.*;
import imdpcomp.Experiment;
import learning.Simulation.StateActionPair;
import learning.Simulation.TransitionTriple;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.special.Beta;
import param.Function;
import prism.Evaluator;
import prism.Prism;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static explicit.ConstructModel.CompositionType.L1;
import static imdpcomp.Experiment.ParameterTying.DEPENDENCY_TYING;
import static imdpcomp.Experiment.ParameterTying.NO_TYING;

public class PACIntervalEstimator extends MAPEstimator {

    protected double error_tolerance;
    double precision = 1e-8;

    // For parameter-tying in IMDP
    protected HashMap<TransitionTriple, Double> tiedModes = new HashMap<>();
    protected HashMap<TransitionTriple, Integer> tiedTransitionCounts = new HashMap<>();
    protected HashMap<TransitionTriple, Integer> tiedStateActionCounts = new HashMap<>();

    // For marginal paramter-tying Level 1 (Dependency Identifiers)
    protected HashMap<String, List<Integer>> tiedDepIdTransCounts = new HashMap<>();
    protected HashMap<String, Integer> tiedDepIdSACounts = new HashMap<>();
    protected HashMap<String, List<Interval<Double>>> tiedDepIdIntervals = new HashMap<>();
    protected int numDependencyMarginals = -1;

    // For marginal paramter-tying Level 2
    protected HashMap<Function, Integer> tiedMarginalTransitionCounts = new HashMap<>();
    protected HashMap<Function, Integer> tiedMarginalStateActionCounts = new HashMap<>();
    protected HashMap<Function, Interval<Double>> tiedMarginalIntervals = new HashMap<>();

    // Caching for vertex UMDP construction
    protected final Map<String, double[][]> verticesCache = new HashMap<>();
    protected final Map<String, Boolean> successCache = new HashMap<>();
    protected final Map<String, GRBModel> modelChache = new HashMap<>();

    UDistributionVertices<Double> distrUncVert = null;
    UDistributionLinearProgram<Double> distUncMcCormick = null;

    private boolean verbose_bisim = false;

    //NormalDistribution distribution = NormalDistribution.of(0, 1);

    GRBEnv env = GurobiEnvManager.getSharedEnv();

    public HashMap<Integer, Integer> lengths = new HashMap<>(); //TODO: delete

    public PACIntervalEstimator(Prism prism, Experiment ex) {
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

    /**
     * Parameter-tying Level 1 - Tie Dependency Identifiers
     * TODO: update this potentially
     */
    public void tieDependencyIdentifiers() {
        tiedDepIdTransCounts.clear();
        tiedDepIdSACounts.clear();
        tiedDepIdIntervals.clear();

        for (int s = 0; s < pmdp.getNumStates(); s++) {
            for (int i = 0; i < pmdp.getNumChoices(s); i++) {
                Distribution<Function> pdist = pmdp.getChoice(s, i);
                String action = getActionString(mdp, s, i);
                StateActionPair sa = new StateActionPair(s, action);
                int sac = sampleSizeMap.getOrDefault(sa, 0);

                int[][] marginalCounts = getMarginalCountsTied(s, i);

                for (int j = 0; j < marginalCounts.length; j++) {
                    String dependencyId = pmdp.dependencyIdentifier.getIdentifier(s,i,j);
                    ArrayList<Integer> countList = Arrays.stream(marginalCounts[j])
                            .boxed()
                            .collect(Collectors.toCollection(ArrayList::new));

                    if (!tiedDepIdTransCounts.containsKey(dependencyId)) {
                        tiedDepIdTransCounts.put(dependencyId, countList);
                        tiedDepIdSACounts.put(dependencyId, sac);
                    } else {
                        //System.out.println("s: " + s + "action: " + action + " depid: " + dependencyId);
                        sumLists(tiedDepIdTransCounts.get(dependencyId), countList);
                        tiedDepIdSACounts.put(dependencyId, tiedDepIdSACounts.get(dependencyId) + sac);
                    }
                }
            }
        }
    }

    public void sumLists(List<Integer> a,List<Integer> b) {
        assert a.size() == b.size();
        //System.out.println(a + " " + b);
        for (int i = 0; i < a.size(); i++) {
            a.set(i, a.get(i) + b.get(i));
        }
    }

    /**
     * Parameter-tying Level 2 - Tie marginal parameters also across different states
     */
    public void tieMarginalParameters() {
        tiedMarginalStateActionCounts.clear();
        tiedMarginalTransitionCounts.clear();
        tiedMarginalIntervals.clear();

        for (int s = 0; s < pmdp.getNumStates(); s++) {
            for (int i = 0; i < pmdp.getNumChoices(s); i++) {
                Distribution<Function> pdist = pmdp.getChoice(s,i);
                String action = getActionString(mdp, s, i);
                StateActionPair sa = new StateActionPair(s, action);
                int sac = sampleSizeMap.getOrDefault(sa, 0);

                int[][] marginalCounts = getMarginalCountsTied(s, i);

                // Iterate over each marginal
                for (int j = 0; j < marginalCounts.length; j++) {
                    // Iterate over each function in the marginal
                    for (int k = 0; k < marginalCounts[j].length; k++) {
                        // Get function
                        Function marginalFunc = pdist.getMarginals().get(j).get(k);

                        // Update tied counts
                        if (!tiedMarginalTransitionCounts.containsKey(marginalFunc)) {
                            tiedMarginalTransitionCounts.put(marginalFunc, marginalCounts[j][k]);
                            tiedMarginalStateActionCounts.put(marginalFunc, sac);
                        } else {
                            tiedMarginalTransitionCounts.put(marginalFunc, tiedMarginalTransitionCounts.get(marginalFunc) + marginalCounts[j][k]);
                            tiedMarginalStateActionCounts.put(marginalFunc, tiedMarginalStateActionCounts.get(marginalFunc) + sac);
                        }

                    }
                }
            }
        }
    }

    public int[][] getMarginalCountsTied(int s, int i) {
        Distribution<Function> pdist = pmdp.getChoice(s, i);
        String action = getActionString(mdp, s, i);

        // 1) prepare marginal counts
        List<List<Function>> marginals = pdist.getMarginals();
        int m = marginals.size();
        int[][] counts = new int[m][];
        for (int k = 0; k < m; k++) {
            counts[k] = new int[marginals.get(k).size()];
        }

        // 2) map product counts back to marginals
        for (int succ : pdist.supportArrayUnique) {
            List<Integer> mapping = pdist.supportMarginalsMap.get(succ);
            int c = samplesMap.getOrDefault(new TransitionTriple(s, action, succ), 0);
            for (int k = 0; k < m; k++) {
                counts[k][mapping.get(k)] += c;
            }
        }

        return counts;
    }

    @Override
    public UMDP<Double> buildMarginalUMDP(MDP<Double> mdp) {
        int numStates = mdp.getNumStates();

        UMDPSimple<Double> umdp = new UMDPSimple<>(numStates);
        umdp.addInitialState(mdp.getFirstInitialState());
        umdp.setStatesList(mdp.getStatesList());
        umdp.setConstantValues(mdp.getConstantValues());

        lengths.clear();

        switch (ex.tieParameters) {
            case FULL_TYING -> {tieMarginalParameters();}
            case DEPENDENCY_TYING -> {tieDependencyIdentifiers();}
        }

        for (int s = 0; s < numStates; s++) {
            int numChoices = mdp.getNumChoices(s);

            if (ex.compositionType == L1) {
                for (int i = 0; i < numChoices; i++) {
                    Distribution<Function> pdist = pmdp.getChoice(s, i);
                    List<UDistributionL1<Double>> marginalDists = getMarginalL1Dists(s, i);
                    UDistributionL1<Double> udist = new UDistributionL1<>(marginalDists, pdist.supportArrayUnique);
                    umdp.addActionLabelledChoice(s, udist, getActionString(mdp, s, i));
                }
            } else {
                for (int i = 0; i < numChoices; i++) {
                    Distribution<Function> pdist = pmdp.getChoice(s, i);
                    List<List<Interval<Double>>> marginalIntervals = getMarginalIntervals(s, i);
                    UDistribution<Double> udist = constructMarginalDist(marginalIntervals, pdist.supportArrayUnique, false);
                    umdp.addActionLabelledChoice(s, udist, getActionString(mdp, s, i));
                }
            }
        }

        Map<String, BitSet> labels = mdp.getLabelToStatesMap();
        for (Map.Entry<String, BitSet> entry : labels.entrySet()) {
            umdp.addLabel(entry.getKey(), entry.getValue());
        }
        this.marginalEstimate = umdp;

        // Clean up cache
        verticesCache.clear();
        successCache.clear();
        modelChache.clear();

        Executors.newSingleThreadExecutor().submit(System::gc);

//        System.out.println("Num Vertices: " + lengths);
//        System.out.println("Max: " + lengths.keySet().stream().max(Integer::compareTo).orElse(0));

        return umdp;
    }


    public UDistribution<Double> constructMarginalDist(List<List<Interval<Double>>> marginals, int[] supportArray, boolean smart) {
        switch (ex.compositionType) {
            case VERTEX -> {
                String key = marginals.toString();

                if (verticesCache.containsKey(key)) {
                    distrUncVert = new UDistributionVertices<>(supportArray, verticesCache.get(key));
                } else {
                    distrUncVert = new UDistributionVertices<>(marginals, supportArray, false);
                    verticesCache.put(key, distrUncVert.vertices);
                }

                lengths.merge(distrUncVert.vertices.length, 1, Integer::sum); //TODO: delete

                return distrUncVert;
            }
            case MCCORMICK -> {
                throw new NotImplementedException("MCCORMICK");
            }
            case SMART -> {
                String key = marginals.toString();

                if (verticesCache.containsKey(key)) {
                    distrUncVert = new UDistributionVertices<>(supportArray, verticesCache.get(key));
                    return distrUncVert;
                } else if (!successCache.getOrDefault(key, true)) {
                    distUncMcCormick = new UDistributionLinearProgram<>(marginals, supportArray, env);
                    System.out.println("Building McCormick");
                    return distUncMcCormick;
                } else {
                    distrUncVert = new UDistributionVertices<Double>(marginals, supportArray, true);

                    if (!distrUncVert.smartSuccess) {
                        distUncMcCormick = new UDistributionLinearProgram<>(marginals, supportArray, env);
                        successCache.put(key, false);
                        System.out.println("Building McCormick");
                        return distUncMcCormick;
                    } else {
                        verticesCache.put(key, distrUncVert.vertices);
                    }
                    return distrUncVert;
                }
            }
            case INTERVAL_PRODUCT -> {
                return new UDistributionIntervals<>(marginals, supportArray, Evaluator.forDoubleInterval());
            }
            default -> {
                throw new IllegalArgumentException("Invalid composition type: " + ex.compositionType);
            }
        }
    }

    @Override
    public UMDP<Double> buildPointIMDP(MDP<Double> mdp) {
        int numStates = mdp.getNumStates();
        IMDPSimple<Double> imdp = new IMDPSimple<>(numStates);
        imdp.addInitialState(mdp.getFirstInitialState());
        imdp.setStatesList(mdp.getStatesList());
        imdp.setConstantValues(mdp.getConstantValues());
        imdp.setIntervalEvaluator(Evaluator.forDoubleInterval());

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
                imdp.addActionLabelledChoice(s, distrNew, getActionString(mdp, s, i));
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
                        interval = (N == 0)
                                ? new Interval<>(precision, 1.0 - precision)
                                : computeClopperPearson(N, K, alphaPerCI);
                    } else {
                        int[] kn = edgeCounts.get(packEdgeKey(sAbs, action, tAbs));
                        int K = kn[0], N = kn[1];
                        interval = (N == 0)
                                ? new Interval<>(precision, 1.0 - precision)
                                : computeClopperPearson(N, K, alphaPerCI);
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

    public int[][] getMarginalCounts(int s, int i) {
        Distribution<Function> pdist = pmdp.getChoice(s, i);
        String action = getActionString(mdp, s, i);
        StateActionPair sa = new StateActionPair(s, action);

        // 1) prepare marginal counts
        List<List<Function>> marginals = pdist.getMarginals();
        int m = marginals.size();
        int[][] counts = new int[m][];
        for (int k = 0; k < m; k++) {
            counts[k] = new int[marginals.get(k).size()];
        }

        // 2) map product counts back to marginals
        for (int succ : pdist.supportArrayUnique) {
            List<Integer> mapping = pdist.supportMarginalsMap.get(succ);
            int c = dirichletPriorsMap.get(new TransitionTriple(s, action, succ)) - 1;
            for (int k = 0; k < m; k++) {
                counts[k][mapping.get(k)] += c;
            }
        }

        return counts;
    }

    public List<List<Interval<Double>>> getMarginalIntervals(int s, int i) {
        Distribution<Function> pdist = pmdp.getChoice(s, i);
        String action = getActionString(mdp, s, i);
        StateActionPair sa = new StateActionPair(s, action);
        int sac = getStateActionCount(sa);

        // 1) prepare marginal counts
        int m = pdist.getMarginals().size();
        int[][] counts;

        counts = getMarginalCounts(s, i);

        // 4) build final intervals in one shot
        List<List<Interval<Double>>> marginalIntervals = new ArrayList<>(m);
        for (int k = 0; k < m; k++) {
            int sz = counts[k].length;
            List<Interval<Double>> sub = new ArrayList<>(sz);

            if (sz == 1) {
                sub.add(new Interval<>(1.0, 1.0));
            } else {
                for (int j = 0; j < sz; j++) {
                    if (ex.tieParameters == NO_TYING) {
//                      sub.add(getWCCnterval(counts[k][j], sac));
                        sub.add(getClopperPearsonInterval(counts[k][j], sac));
                    } else {
//                      sub.add(getWCCnterval(tiedMarginalTransitionCounts.get(pdist.getMarginals().get(k).get(j)),
//                                            tiedMarginalStateActionCounts.get(pdist.getMarginals().get(k).get(j))));
                        Function func = pdist.getMarginals().get(k).get(j);
                        String depId = pmdp.dependencyIdentifier.getIdentifier(s, i, k);

                        if (ex.tieParameters == DEPENDENCY_TYING) {
                            sub.add(getClopperPearsonInterval(tiedDepIdTransCounts.get(depId).get(j),
                                                                tiedDepIdSACounts.get(depId)));
                        } else {
                            if (tiedMarginalIntervals.containsKey(func)) {
                                sub.add(tiedMarginalIntervals.get(func));
                            } else {
                                sub.add(getClopperPearsonInterval(tiedMarginalTransitionCounts.get(func),
                                        tiedMarginalStateActionCounts.get(func)));
                                tiedMarginalIntervals.put(func, sub.getLast());
                            }
                        }
                    }
                }
            }

            marginalIntervals.add(sub);
        }

        return marginalIntervals;
    }

    /**
     * Function to generate marginal L_p distributions (currently only L1)
     * @param s State Index
     * @param i Action Index
     * @return List of marginal L_p distributions (currently only L1)
     */
    public List<UDistributionL1<Double>> getMarginalL1Dists(int s, int i) {
        Distribution<Function> pdist = pmdp.getChoice(s, i);
        String action = getActionString(mdp, s, i);
        StateActionPair sa = new StateActionPair(s, action);
        int sac = getStateActionCount(sa);

        int m = pdist.getMarginals().size();
        int[][] counts;

        counts = getMarginalCounts(s, i);

        List<UDistributionL1<Double>> marginalDists = new ArrayList<>(m);
        UDistributionL1<Double> udist;
        Distribution<Double> sub;
        for (int k = 0; k < m; k++) {
            int sz = counts[k].length;
            int countsum = Arrays.stream(counts[k]).sum();
            if (sz == 1) {
                sub = new Distribution<>(Evaluator.forDouble());
                sub.add(0, 1.0);
                sub.addFrequency(1.0);
                udist = new UDistributionL1<>(sub, 0.0);
            }
            else {
                String depId = pmdp.dependencyIdentifier.getIdentifier(s, i, k);
                sub = new Distribution<>(Evaluator.forDouble());
                for (int j = 0; j < sz; j++) {
                    if (ex.tieParameters == NO_TYING) {
                        double emp_prob;
                        if (sac > 0) {
                            emp_prob = (double) counts[k][j] / (double) countsum;
                        } else {
                            emp_prob = 1.0 / (double) sz;
                        }

                        sub.add(j, emp_prob);
                        sub.addFrequency(emp_prob);
                    } else {

                        double emp_prob;
                        if (ex.tieParameters == DEPENDENCY_TYING) {
                            if (tiedDepIdSACounts.get(depId) > 0) {
                                emp_prob = (double) tiedDepIdTransCounts.get(depId).get(j) / (double) tiedDepIdSACounts.get(depId);
                            } else {
                                emp_prob = 1.0 / (double) sz;
                            }
                        } else {
                            throw new NotImplementedException("Full Tying not possible with L1");
                        }
                        sub.add(j, emp_prob);
                        sub.addFrequency(emp_prob);
                    }
                }

                int numSamples = switch (ex.tieParameters) {
                    case NO_TYING -> countsum;
                    case DEPENDENCY_TYING -> tiedDepIdSACounts.get(depId);
                    case FULL_TYING -> throw new NotImplementedException("Full Tying not possible with L1");
                };
                udist = new UDistributionL1<>(sub, getL1WeissmannBound(sz, numSamples));
            }
            marginalDists.add(udist);
        }

        return marginalDists;
    }

    public double getL1WeissmannBound(int numSucc, int numSamples) {
        int m = switch (ex.tieParameters) {
            case NO_TYING -> this.pmdp.getNumMarginals();
            case DEPENDENCY_TYING -> getNumDependencyMarginals();
            case FULL_TYING -> this.tiedMarginalStateActionCounts.size();
        };

        if (numSamples > 0) {
            double alpha = (1.0 - error_tolerance) / (double) m;
            return Math.sqrt(2 * (Math.log(Math.pow(2, numSucc) - 2) - Math.log(alpha)) / (double) numSamples);
        } else {
            return 2.0; // Maximum non-informative L_1 radius
        }

    }

    @Override
    protected Interval<Double> getTransitionInterval(TransitionTriple t) {
        double point;
        int n, k;

        if (this.ex.tieParameters == NO_TYING) {
            point = mode(t);
            n = getStateActionCount(t.getStateAction());
            k = getTransitionCount(t);
        } else { // tying
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

        //return computeWilsonCC(n, point, error_tolerance / (double) m);
    }


//    protected Interval<Double> getWCCnterval(int count, int sacount) {
//        if (sacount == 0) {
//            return new Interval<>(precision, 1 - precision);
//        }
//
//        int m = ex.tieParameters == NO_TYING ? this.tiedMarginalStateActionCounts.size() : this.pmdp.getNumMarginals(); //TODO: replace this before using!!
//        double point = (double) count / (double) sacount;
//        return computeWilsonCC(sacount, point, error_tolerance / (double) m);
//    }

//    // Wilson Score Interval with Continuity Correction
//    private Interval<Double> computeWilsonCC(double n, double p, double delta) {
//        double z = distribution.inverseCumulativeProbability(1 - delta / 2.0);
//
//        double pWCCLower = Math.max(0, (2 * n * p + z * z - z * Math.sqrt(z * z - (1.0 / n) + 4 * n * p * (1 - p) + 4 * p - 2) - 1) / (2 * (n + z * z)));
//        double pWCCUpper = Math.min(1, (2 * n * p + z * z + z * Math.sqrt(z * z - (1.0 / n) + 4 * n * p * (1 - p) - 4 * p + 2) + 1) / (2 * (n + z * z)));
//
//        return new Interval<>(pWCCLower, pWCCUpper);
//    }

    /**
     * Returns the Clopper–Pearson exact (1–α) confidence interval
     * for a binomial proportion based on k successes in n trials.
     *
     * Only used by marginal computation
     */
    protected Interval<Double> getClopperPearsonInterval(int count, int sacount) {
        if (sacount == 0) {
            return new Interval<>(precision, 1 - precision);
        }

        int m = switch (ex.tieParameters) {
            case NO_TYING -> this.pmdp.getNumMarginals();
            case DEPENDENCY_TYING -> getNumDependencyMarginals();
            case FULL_TYING -> this.tiedMarginalStateActionCounts.size();
        };

//        int m = ex.tieParameters ? (depIds ? pmdp.dependencyIdentifier.numIdentifiers() :this.tiedMarginalStateActionCounts.size()) : this.pmdp.getNumMarginals();
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
    public double averageDistanceToSUL() {
        double totalDist = 0.0;

        for (TransitionTriple t : super.trueProbabilitiesMap.keySet()) {
            Interval<Double> interval = this.intervalsMap.get(t);
            double p = super.trueProbabilitiesMap.get(t);
            double dist = maxIntervalPointDistance(interval, p);
            totalDist += dist;
        }

        double averageDist = totalDist / super.trueProbabilitiesMap.size();
        return averageDist;

    }


    public int getNumDependencyMarginals() {
        if (numDependencyMarginals != -1) {
            return numDependencyMarginals;
        } else {
            numDependencyMarginals = 0;
            for (List<Integer> a : this.tiedDepIdTransCounts.values()) {
                numDependencyMarginals += a.size();
            }
            return numDependencyMarginals;
        }
    }

    @Override
    public int getNumLearnableComponents() {
        if (ex.factored) {
            return switch (ex.tieParameters) {
                case NO_TYING -> this.pmdp.getNumMarginals();
                case DEPENDENCY_TYING -> getNumDependencyMarginals();
                case FULL_TYING -> this.tiedMarginalStateActionCounts.size();
            };
        } else {
            return this.getNumLearnableTransitions();
        }
    }


}
