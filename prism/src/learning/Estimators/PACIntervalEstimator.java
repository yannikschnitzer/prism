package learning.Estimators;

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBException;
import com.gurobi.gurobi.GRBModel;
import common.Interval;
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

    //NormalDistribution distribution = NormalDistribution.of(0, 1);

    GRBEnv env;
    {
        try {
            env = new GRBEnv(true);
            env.set(GRB.IntParam.OutputFlag, 0);
            env.start();
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

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
        if (useBisim) {
            return buildPointIMDP_Bisim(mdp);
        }

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
        IMDPSimple<Double> imdp = new IMDPSimple<>(numAbsStates);
        imdp.addInitialState(0); //TODO: change this initial state construction to something principled
        imdp.setStatesList(pmdpBisim.getStatesList());
        imdp.setConstantValues(pmdpBisim.getConstantValues());
        imdp.setIntervalEvaluator(Evaluator.forDoubleInterval());

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

        System.out.println("Tied expressions in abstract model: " + exprCounts.keySet().stream().toList());

        // alpha split: per unique CI constructed
        final int mCIs = Math.max(1, tieAbstractExpressions ? exprCounts.size() : edgeCounts.size());
        final double alphaPerCI = (1.0 - error_tolerance) / (double) mCIs;

        // ----- PASS 2: build distributions with chosen tying policy -----
        for (int sAbs = 0; sAbs < numAbsStates; sAbs++) {
            int numChoices = pmdpBisim.getNumChoices(sAbs);
            for (int iAbs = 0; iAbs < numChoices; iAbs++) {
                final String action = getActionString(pmdpBisim, sAbs, iAbs);
                Distribution<Interval<Double>> distrNew = new Distribution<>(Evaluator.forDoubleInterval());

                for (Iterator<Map.Entry<Integer, param.Function>> it = pmdpBisim.getTransitionsIterator(sAbs, iAbs); it.hasNext();) {
                    Map.Entry<Integer, param.Function> e = it.next();
                    int tAbs = e.getKey();
                    param.Function f = e.getValue();

                    Interval<Double> interval;
                    if (tieAbstractExpressions) {
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

                imdp.addActionLabelledChoice(sAbs, distrNew, action);
            }
        }

        // labels (carried over by pmdpBisim already)
        for (Map.Entry<String, BitSet> e : pmdpBisim.getLabelToStatesMap().entrySet())
            imdp.addLabel(e.getKey(), e.getValue());

        this.debugDumpCounts(false);

        this.estimate = imdp;
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
        } else {
            if (!this.samplesMap.containsKey(t)) {
                return new Interval<>(precision, 1 - precision);
            }
            point = tiedModes.get(t);
            k = tiedTransitionCounts.get(t);
            n = tiedStateActionCounts.get(t);
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

    /*
      Bisimulation Aggregations
     */

    /**
     * Aggregate trials for the abstract (sAbs, action) by summing ground state-action counts
     * over all ground states that map to sAbs and share the same action label.
     */
    private int aggregateAbstractEdgeTrials(int sAbs, String action) {
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
    private int aggregateAbstractEdgeSuccesses(int sAbs, String action, int tAbs) {
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
    private static final class FuncKey {
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
    private long packEdgeKey(int sAbs, String action, int tAbs) {
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
