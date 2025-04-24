package learning.Estimators;

import com.gurobi.gurobi.GRBModel;
import common.Interval;
import explicit.*;
import imdpcomp.Experiment;
import learning.Simulation.StateActionPair;
import learning.Simulation.TransitionTriple;
import org.apache.commons.statistics.distribution.NormalDistribution;
import param.Function;
import prism.Evaluator;
import prism.Prism;

import java.util.*;

public class PACIntervalEstimator extends MAPEstimator {

    protected double error_tolerance;
    protected HashMap<TransitionTriple, Double> tiedModes = new HashMap<>();
    protected HashMap<TransitionTriple, Integer> tiedTransitionCounts = new HashMap<>();
    protected HashMap<TransitionTriple, Integer> tiedStateActionCounts = new HashMap<>();

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

    @Override
    public UMDP<Double> buildMarginalUMDP(MDP<Double> mdp) {
        int numStates = mdp.getNumStates();

        UMDPSimple<Double> umdp = new UMDPSimple<>(numStates);
        umdp.addInitialState(mdp.getFirstInitialState());
        umdp.setStatesList(mdp.getStatesList());
        umdp.setConstantValues(mdp.getConstantValues());

        // Clear caches since new marginals have arrived
        verticesCache.clear();
        successCache.clear();
        modelChache.clear();

        if (ex.tieParameters) tieParameters();

        for (int s = 0; s < numStates; s++) {
            int numChoices = mdp.getNumChoices(s);

            for (int i = 0; i < numChoices; i++) {
                Distribution<Function> pdist = pmdp.getChoice(s, i);
                List<List<Interval<Double>>> marginalIntervals = getMarginalIntervals(s, i);
                UDistribution<Double> udist = constructMarginalDist(marginalIntervals, pdist.supportArrayUnique, false);

                umdp.addActionLabelledChoice(s, udist, getActionString(mdp, s, i));
            }
        }

        Map<String, BitSet> labels = mdp.getLabelToStatesMap();
        for (Map.Entry<String, BitSet> entry : labels.entrySet()) {
            umdp.addLabel(entry.getKey(), entry.getValue());
        }
        this.marginalEstimate = umdp;

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
            default -> {
                throw new IllegalArgumentException("Invalid composition type: " + ex.compositionType);
            }
        }
    }

    @Override
    public UMDP<Double> buildPointIMDP(MDP<Double> mdp) {
        //System.out.println("Building IMDP");
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
                        interval = new Interval<Double>(p, p);
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

    public List<List<Interval<Double>>> getMarginalIntervals(int s, int i) {
        Distribution<Function> pdist = pmdp.getChoice(s, i);
        String action = getActionString(mdp, s, i);
        StateActionPair sa = new StateActionPair(s, action);
        int sac = getStateActionCount(sa);

        // 1) prepare primitive counts
        List<List<Function>> marginals = pdist.getMarginals();
        int m = marginals.size();
        int[][] counts = new int[m][];
        for (int k = 0; k < m; k++) {
            counts[k] = new int[marginals.get(k).size()];
        }

        // 2) map product counts back to those primitive arrays
        for (int succ : pdist.supportArrayUnique) {
            List<Integer> mapping = pdist.supportMarginalsMap.get(succ);
//            int c = this.ex.tieParameters
//                    ? tiedTransitionCounts.get(new TransitionTriple(s, action, succ))
//                    : dirichletPriorsMap.get(new TransitionTriple(s, action, succ));
            int c = dirichletPriorsMap.get(new TransitionTriple(s, action, succ));
            for (int k = 0; k < m; k++) {
                counts[k][mapping.get(k)] += c;
            }
        }

        // 4) build final intervals in one shot
        List<List<Interval<Double>>> marginalIntervals = new ArrayList<>(m);
        for (int k = 0; k < m; k++) {
            int sz = counts[k].length;
            List<Interval<Double>> sub = new ArrayList<>(sz);

            if (sz == 1) {
                sub.add(new Interval<>(1.0, 1.0)); // Known graph structure
            } else {
                for (int j = 0; j < sz; j++) {
                    sub.add(getMarginalInterval(counts[k][j], sac));

                }
            }

            marginalIntervals.add(sub);
        }

//        System.out.printf(
//                "marginals=%s%ncounts=%s%nsac=%d%nintervals=%s%n",
//                marginals,
//                Arrays.deepToString(counts),
//                sac,
//                marginalIntervals
//        );

        return marginalIntervals;
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

        double averageDist = totalDist / super.trueProbabilitiesMap.keySet().size();
        return averageDist;

    }

    @Override
    protected Interval<Double> getTransitionInterval(TransitionTriple t) {
        double precision = 1e-8;
        double point;
        int n;

        if (!this.ex.tieParameters) {
            point = mode(t);
            n = getStateActionCount(t.getStateAction());
        } else {
            if (!this.samplesMap.containsKey(t)) {
                return new Interval<>(precision, 1 - precision);
            }
            point = tiedModes.get(t);
            n = tiedStateActionCounts.get(t);
        }

        int m = this.getNumLearnableTransitions();

        return computeWilsonCC(n, point, error_tolerance / (double) m);
    }

    protected Interval<Double> getMarginalInterval(int count, int sacount) {
        int m = this.pmdp.getNumMarginals();
        double point = (double) count / (double) sacount;
        return computeWilsonCC(sacount, point, error_tolerance / (double) m);
    }

    private Interval<Double> computeWilsonCC(double n, double p, double delta) {
        double z = distribution.inverseCumulativeProbability(1 - delta / 2.0);

        double pWCCLower = Math.max(0, (2 * n * p + z * z - z * Math.sqrt(z * z - (1.0 / n) + 4 * n * p * (1 - p) + 4 * p - 2) - 1) / (2 * (n + z * z)));
        double pWCCUpper = Math.min(1, (2 * n * p + z * z + z * Math.sqrt(z * z - (1.0 / n) + 4 * n * p * (1 - p) - 4 * p + 2) + 1) / (2 * (n + z * z)));

        return new Interval<>(pWCCLower, pWCCUpper);
    }
}
