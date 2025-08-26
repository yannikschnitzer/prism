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

import java.util.*;

import static imdpcomp.Experiment.ParameterTying.NO_TYING;

public class PACConvexEstimator extends MAPEstimator {

    protected double error_tolerance;
    double precision = 1e-8;
    boolean useVertexPrecomp = true;

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
        } else {
            if (!this.samplesMap.containsKey(t)) {
                return new Interval<>(precision, 1 - precision);
            }
            k = tiedTransitionCounts.get(t);
            n = tiedStateActionCounts.get(t);
        }

        int m = this.getNumLearnableTransitions();

        return computeClopperPearson(n, k, (1.0 - error_tolerance) / (double) m);

    }

    @Override
    public double[] getCurrentResults() throws PrismException {
        updatePriors();
        UMDP<Double> imdp = buildPointIMDP(mdp);

        Result resultRobustConvex;
        Result resultOptimisticConvex;

        long startTime;
        long modelBuildingTime;
        long modelCheckingTimeRobust;
        long modelCheckingTimeOptimistic;
        long modelCheckingTimeDTMC;

        // Solve Convex Parametric MDP
        try {
            startTime = System.nanoTime();
            System.out.println("Building Convex UMDP");
            UMDP<Double> convex_mdp = buildConvexUMDP(imdp, this.pmdp);
            modelBuildingTime = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            resultRobustConvex = modelCheckPointEstimate(convex_mdp,true,false);
            modelCheckingTimeRobust = System.nanoTime() - startTime;

            startTime = System.nanoTime();
            resultOptimisticConvex = modelCheckPointEstimate(convex_mdp,false,false);
            modelCheckingTimeOptimistic = System.nanoTime() - startTime;
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
        double resconvexMDP = round((Double) resultRobustConvex.getResult());
        MDStrategy<Double> robustStrat = (MDStrategy<Double>) resultRobustConvex.getStrategy();
        MDStrategy<Double> optimisticStrat = (MDStrategy<Double>) resultOptimisticConvex.getStrategy();
        this.currentStrat = optimisticStrat;

        startTime = System.nanoTime();
        double resconvexDTMC = round((Double) checkDTMC(robustStrat).getResult());
        modelCheckingTimeDTMC = System.nanoTime() - startTime;

        double resultConvexOptimisticDTMC = round((Double) checkDTMC(optimisticStrat).getResult());

        // Model Check IMDP for comparison. TODO: delete and move to proper comparison
//        Result resultRobustIMDP = modelCheckPointEstimate(imdp,true, false);
//        double resRobustIMDP = round((Double) resultRobustIMDP.getResult());
//        MDStrategy<Double> robustIMDPStrat = (MDStrategy<Double>) resultRobustIMDP.getStrategy();
//        double resultRobustDTMC = round((Double) checkDTMC(robustIMDPStrat).getResult());

        System.out.println("Convex Guarantee: " + resconvexMDP + ", Convex Performance: " + resconvexDTMC);
       // System.out.println("IMDP Guarantee: " + resRobustIMDP + ", IMDP Performance: " + resultRobustDTMC);

        return new double[]{resconvexMDP, resconvexDTMC, resultConvexOptimisticDTMC, modelBuildingTime, modelCheckingTimeRobust, modelCheckingTimeOptimistic, modelCheckingTimeDTMC};
    }

    // TODO : Update
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


    public UMDP<Double> buildConvexUMDP(UMDP<Double> imdp, MDPSimple<Function> pmdp) throws GRBException, PrismException {
        GRBEnv env = new GRBEnv(true);
        env.set(GRB.IntParam.OutputFlag, 0);
        env.start();
        ConvexLearner cxl = new ConvexLearner(env);
        cxl.setParamModel(pmdp);
        cxl.setConstraints(imdp);
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
