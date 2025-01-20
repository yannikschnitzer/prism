package learning.Estimators;

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBConstr;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBException;
import common.Interval;
import explicit.*;
import learning.Experiment;
import learning.StateActionPair;
import learning.TransitionTriple;
import org.apache.commons.statistics.distribution.NormalDistribution;
import param.Function;
import parser.ast.Expression;
import parser.ast.PropertiesFile;
import prism.Evaluator;
import prism.Prism;
import prism.PrismException;
import prism.Result;
import simulator.ModulesFileModelGenerator;
import strat.MDStrategy;

import java.util.*;

public class PACConvexEstimator extends MAPEstimator {

    protected double error_tolerance;
    protected HashMap<TransitionTriple, Double> tiedModes = new HashMap<>();
    protected HashMap<TransitionTriple, Integer> tiedStateActionCounts = new HashMap<>();

    NormalDistribution distribution = NormalDistribution.of(0, 1);

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

        double confidence_interval = confidenceInterval(t);
        double lower_bound = Math.max(point - confidence_interval, precision);
        double upper_bound = Math.min(point + confidence_interval, 1 - precision);

        int m = this.getNumLearnableTransitions();
        Interval<Double> wcc_interval = computeWilsonCC(n, point, error_tolerance / (double) m);

        return wcc_interval;

        //return new Interval<>(lower_bound, upper_bound);
    }

    /**
     * Get minimum (width) interval for each transition.
     *
     * @return Map from transition to minimal interval
     */
    public Map<TransitionTriple, Interval<Double>> computeMinIntervals() {
        Map<Function, List<TransitionTriple>> functionMap = this.getFunctionMap();
        Map<TransitionTriple, Interval<Double>> minIntervalMap = new HashMap<>();

        for (Function func : functionMap.keySet()) {
            List<TransitionTriple> transitions = functionMap.get(func);
            List<Interval<Double>> intervals = new ArrayList<>();

            for (TransitionTriple transition : transitions) {
                intervals.add(getTransitionInterval(transition));
            }

            Interval<Double> minInterval = Collections.min(intervals, Comparator.comparingDouble(interval -> interval.getUpper() - interval.getLower()));

            for (TransitionTriple transition : transitions) {
                minIntervalMap.put(transition, minInterval);
            }
        }

        return minIntervalMap;
    }

    @Override
    public double[] getCurrentResults() throws PrismException {
        updatePriors();
        UMDP<Double> imdp = buildPointIMDP(mdp);

        try {
            //System.out.printf("PMDP: " + this.pmdp);
            //System.out.println("IMDP: " + imdp);
            //System.out.println("Convex MDP constraints:");
            UMDP<Double> convex_mdp = buildConvexUMDP(imdp, this.pmdp);


            Result resconvex = modelCheckPointEstimate(convex_mdp,true,false);
            double res = round((Double) resconvex.getResult());
            MDStrategy robustStrat = (MDStrategy) resconvex.getStrategy();
            double resultRobustDTMC = round((Double) checkDTMC(robustStrat).getResult());
            //MDStrategy optimisticStrat = computeStrategyFromEstimate(convex_mdp, false);
            System.out.println("Performance Convex MDP: " + res);
            System.out.println("Performanec on MDP with convex policy " + resultRobustDTMC);
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }

        double resultRobustMDP = round((Double) modelCheckPointEstimate(imdp,true, false).getResult());
        double resultOptimisticMDP = round((Double) modelCheckPointEstimate(imdp,false, false).getResult());

        MDStrategy robustStrat = computeStrategyFromEstimate(this.estimate, true);
        MDStrategy optimisticStrat = computeStrategyFromEstimate(this.estimate, false);
        double resultRobustDTMC = round((Double) checkDTMC(robustStrat).getResult());
        double resultOptimisticDTMC = round((Double) checkDTMC(optimisticStrat).getResult());
        double dist = round(this.averageDistanceToSUL());
        List<Double> lbs = List.of(0.0);//this.getLowerBounds();
        List<Double> ubs = List.of(1.0);//this.getUpperBounds();
        return new double[]{resultRobustMDP, resultRobustDTMC, dist, lbs.get(0), ubs.get(0), resultOptimisticMDP, resultOptimisticDTMC};
    }

    @Override
    public double[] getInitialResults() throws PrismException {
        double[] res = super.getInitialResults();
        try {
            buildConvexUMDP(this.estimate, this.pmdp);
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    @Override
    public UMDPSimple<Double> buildPointIMDP(MDP<Double> mdp) {
        //System.out.println("Building IMDP");
        int numStates = mdp.getNumStates();
        UMDPSimple<Double> imdp = new UMDPSimple<>(numStates);
        imdp.addInitialState(mdp.getFirstInitialState());
        imdp.setStatesList(mdp.getStatesList());
        imdp.setConstantValues(mdp.getConstantValues());

        //tieParameters();
        Map<TransitionTriple, Interval<Double>> minIntervals = Collections.emptyMap(); //computeMinIntervals();

        for (int s = 0; s < numStates; s++) {
            int numChoices = mdp.getNumChoices(s);
            final int state = s;
            for (int i = 0; i < numChoices; i++) {
                final String action = getActionString(mdp, s, i);

                Distribution<Interval<Double>> distrNew = new Distribution<>(Evaluator.forDoubleInterval());
                mdp.forEachDoubleTransition(s, i, (int sFrom, int sTo, double p) -> {
                    TransitionTriple t = new TransitionTriple(state, action, sTo);
                    Interval<Double> interval;
                    if (!this.ex.optimizations) {
                        if (0 < p && p < 1.0) {
                            interval = getTransitionInterval(t);
                            //System.out.println("Transition:" + t + " Naive Interval: " + interval + " New Interval: " + minIntervals.get(t));
                            //System.out.println("Triple: " + t + " Interval: " + interval);
                            distrNew.add(sTo, interval);
                            this.intervalsMap.put(t, interval);
                        } else if (p == 1.0) {
                            interval = new Interval<Double>(p, p);
                            distrNew.add(sTo, interval);
                            this.intervalsMap.put(t, interval);
                        }
                    } else {
                        if (!this.constantMap.containsKey(t)) {
                            interval = minIntervals.get(t);
                        } else {
                            p = this.constantMap.get(t);
                            interval = new Interval<Double>(p, p);
                        }

                        distrNew.add(sTo, interval);
                        this.intervalsMap.put(t, interval);
                    }
                });
                UDistributionIntervals<Double> udist = new UDistributionIntervals<>(distrNew);
                imdp.addActionLabelledChoice(s, udist, getActionString(mdp, s, i));
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


    public UMDP<Double> buildConvexUMDP(UMDP<Double> imdp, MDPSimple<Function> pmdp) throws GRBException, PrismException {
        GRBEnv env = new GRBEnv(true);
        env.set(GRB.IntParam.OutputFlag, 0);
        env.start();
        ConvexLearner cxl = new ConvexLearner(env);
        cxl.setParamModel(pmdp);
        cxl.setConstraints(imdp);
        cxl.getModel().update();

//        for (GRBConstr con : cxl.getModel().getConstrs()) {
//            System.out.println(ExpressionTranslator.formatGBRConstraint(cxl.getModel(),con));
//        }

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

    public MDStrategy computeStrategyFromConvexEstimate(UMDP<Double> estimate, boolean robust) throws PrismException {
        UMDPModelChecker mc = new UMDPModelChecker(this.prism);
        mc.setGenStrat(true);
        mc.setPrecomp(false);
        mc.setErrorOnNonConverge(true);
        //mc.setMaxIters(100000);

        PropertiesFile pf = robust
                ? prism.parsePropertiesString(ex.robustSpec)
                : prism.parsePropertiesString(ex.optimisticSpec);
        ModulesFileModelGenerator<?> modelGen = ModulesFileModelGenerator.create(modulesFileIMDP, this.prism);
        modelGen.setSomeUndefinedConstants(estimate.getConstantValues());
        mc.setModelCheckingInfo(modelGen, pf, modelGen);
        Expression exprTarget = pf.getProperty(0);
        Result result = mc.check(estimate, exprTarget);
        MDStrategy strat = (MDStrategy) result.getStrategy();

        return strat;
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

    protected Double confidenceInterval(TransitionTriple t) {
        return computePACBound(t);
    }

    private Double computePACBound(TransitionTriple t) {
        double alpha = error_tolerance; // probability of error (i.e. 1-alpha is probability of correctly specifying the interval)
        int m = this.getNumLearnableTransitions();

        int n;
        if (!this.ex.tieParameters) {
            n = getStateActionCount(t.getStateAction());
        } else {
            n = tiedStateActionCounts.get(t);
        }
        alpha = (error_tolerance * (1.0 / (double) m));///((double) this.mdp.getNumChoices(t.getStateAction().getState())); // distribute error over all transitions

        double delta = Math.sqrt((Math.log(2 / alpha)) / (2 * n));

        Interval<Double> wccinterval = computeWilsonCC(n, mode(t), alpha);
        if (n < -10000) {
            System.out.println("Transition:" + t + " n : " + n);
            System.out.println("Hoeffdings-Interval: " + new Interval<Double>(mode(t) - delta, mode(t) + delta));
            System.out.println("Wilson-Interval: " + wccinterval);
        }

        return delta;
    }

    private Interval<Double> computeWilsonCC(double n, double p, double delta) {
        double z = distribution.inverseCumulativeProbability(1 - delta / 2.0);

        double pWCCLower = Math.max(0, (2 * n * p + z * z - z * Math.sqrt(z * z - (1.0 / n) + 4 * n * p * (1 - p) + 4 * p - 2) - 1) / (2 * (n + z * z)));
        double pWCCUpper = Math.min(1, (2 * n * p + z * z + z * Math.sqrt(z * z - (1.0 / n) + 4 * n * p * (1 - p) - 4 * p + 2) + 1) / (2 * (n + z * z)));

        return new Interval<>(pWCCLower, pWCCUpper);
    }
}
