package learning.Estimators;

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
import org.apache.commons.statistics.distribution.NormalDistribution;
import param.Function;
import prism.Evaluator;
import prism.Prism;

import java.util.*;
import java.util.concurrent.Executors;

public class PACIntervalEstimator extends MAPEstimator {

    protected double error_tolerance;
    double precision = 1e-8;

    // For parameter-tying in IMDP
    protected HashMap<TransitionTriple, Double> tiedModes = new HashMap<>();
    protected HashMap<TransitionTriple, Integer> tiedTransitionCounts = new HashMap<>();
    protected HashMap<TransitionTriple, Integer> tiedStateActionCounts = new HashMap<>();

    // For marginal paramter-tying Level 1 (Dependency Identifiers)
    protected HashMap<String, List<Integer>> tiedDependencyIdentifierTransitionCounts = new HashMap<>();
    protected HashMap<String, List<Integer>> tiedDepndenyIdentifierStateActionCounts = new HashMap<>();
    protected HashMap<String, List<Interval<Double>>> tiedDependencyIdentifierIntervals = new HashMap<>();

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

    NormalDistribution distribution = NormalDistribution.of(0, 1);

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
     */
    public void tieDependencyIdentifiers() {
        tiedDependencyIdentifierTransitionCounts.clear();
        tiedDepndenyIdentifierStateActionCounts.clear();
        tiedDependencyIdentifierIntervals.clear();

        for (int s = 0; s < pmdp.getNumStates(); s++) {
            for (int i = 0; i < pmdp.getNumChoices(s); i++) {
                Distribution<Function> pdist = pmdp.getChoice(s, i);
                String action = getActionString(mdp, s, i);
                StateActionPair sa = new StateActionPair(s, action);
                int sac = sampleSizeMap.getOrDefault(sa, 0);

                int[][] marginalCounts = getMarginalCountsTied(s, i);


            }
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

        if (ex.tieParameters) tieMarginalParameters(); // TODO: currently only level 2 tying, make this a choice

        for (int s = 0; s < numStates; s++) {
            int numChoices = mdp.getNumChoices(s);

            for (int i = 0; i < numChoices; i++) {
                Distribution<Function> pdist = pmdp.getChoice(s, i);
                List<List<Interval<Double>>> marginalIntervals = getMarginalIntervals(s, i);
                UDistributionVertices<Double> udist = (UDistributionVertices<Double>) constructMarginalDist(marginalIntervals, pdist.supportArrayUnique, false);

                umdp.addActionLabelledChoice(s, udist, getActionString(mdp, s, i));
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

                return distrUncVert;
            }
            case MCCORMICK -> {
                throw new NotImplementedException("MCCORMICK");
            }
            case SMART -> {
                throw new NotImplementedException("SMART");
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

        if (ex.tieParameters) tieParameters();

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
                    if (!ex.tieParameters) {
//                      sub.add(getWCCnterval(counts[k][j], sac));
                        sub.add(getClopperPearsonInterval(counts[k][j], sac));
                    } else {
//                      sub.add(getWCCnterval(tiedMarginalTransitionCounts.get(pdist.getMarginals().get(k).get(j)),
//                                            tiedMarginalStateActionCounts.get(pdist.getMarginals().get(k).get(j))));
                        Function func = pdist.getMarginals().get(k).get(j);
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

            marginalIntervals.add(sub);
        }

        return marginalIntervals;
    }

    @Override
    protected Interval<Double> getTransitionInterval(TransitionTriple t) {
        double point;
        int n, k;

        if (!this.ex.tieParameters) {
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


    protected Interval<Double> getWCCnterval(int count, int sacount) {
        if (sacount == 0) {
            return new Interval<>(precision, 1 - precision);
        }

        int m = ex.tieParameters ? this.tiedMarginalStateActionCounts.size() : this.pmdp.getNumMarginals();
        double point = (double) count / (double) sacount;
        return computeWilsonCC(sacount, point, error_tolerance / (double) m);
    }

    // Wilson Score Interval with Continuity Correction
    private Interval<Double> computeWilsonCC(double n, double p, double delta) {
        double z = distribution.inverseCumulativeProbability(1 - delta / 2.0);

        double pWCCLower = Math.max(0, (2 * n * p + z * z - z * Math.sqrt(z * z - (1.0 / n) + 4 * n * p * (1 - p) + 4 * p - 2) - 1) / (2 * (n + z * z)));
        double pWCCUpper = Math.min(1, (2 * n * p + z * z + z * Math.sqrt(z * z - (1.0 / n) + 4 * n * p * (1 - p) - 4 * p + 2) + 1) / (2 * (n + z * z)));

        return new Interval<>(pWCCLower, pWCCUpper);
    }

    /**
     * Returns the Clopper–Pearson exact (1–α) confidence interval
     * for a binomial proportion based on k successes in n trials.
     */
    protected Interval<Double> getClopperPearsonInterval(int count, int sacount) {
        if (sacount == 0) {
            return new Interval<>(precision, 1 - precision);
        }

        int m = ex.tieParameters ? this.tiedMarginalStateActionCounts.size() : this.pmdp.getNumMarginals();
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
}
