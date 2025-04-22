package learning.Estimators;

import common.Interval;
import explicit.Distribution;
import explicit.IMDP;
import explicit.IMDPSimple;
import explicit.MDP;
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
    protected HashMap<TransitionTriple, Integer> tiedStateActionCounts = new HashMap<>();

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
                tiedStateActionCounts.put(t, denum);
            }
        }

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

    @Override
    public IMDP<Double> buildPointIMDP(MDP<Double> mdp) {
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

                Distribution<Function> dist = pmdp.getChoice(s, i);

                if (s == 1 || s == 0) {
                    System.out.println("State: " + state + " Action: " + action);
                    getMarginalIntervals(dist);
                }

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
        Iterator<Map.Entry<String, BitSet>> it = labels.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, BitSet> entry = it.next();
            imdp.addLabel(entry.getKey(), entry.getValue());
        }
        this.estimate = imdp;

        return imdp;
    }

    public List<List<Interval<Double>>> getMarginalIntervals(Distribution<Function> pdist) {

        List<List<Function>> marginals = pdist.getMarginals();
        List<List<Integer>> marginalCounts = new ArrayList<>(marginals.size());

        for (List<Function> marginal : marginals) {
            marginalCounts.add(new ArrayList<>(Collections.nCopies(marginal.size(), 0)));
        }

        System.out.println("Marginals: " + marginals);
        System.out.println("MarginalCounts: " + marginalCounts);

        return null;
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

    private Interval<Double> computeWilsonCC(double n, double p, double delta) {
        double z = distribution.inverseCumulativeProbability(1 - delta / 2.0);

        double pWCCLower = Math.max(0, (2 * n * p + z * z - z * Math.sqrt(z * z - (1.0 / n) + 4 * n * p * (1 - p) + 4 * p - 2) - 1) / (2 * (n + z * z)));
        double pWCCUpper = Math.min(1, (2 * n * p + z * z + z * Math.sqrt(z * z - (1.0 / n) + 4 * n * p * (1 - p) - 4 * p + 2) + 1) / (2 * (n + z * z)));

        return new Interval<>(pWCCLower, pWCCUpper);
    }
}
