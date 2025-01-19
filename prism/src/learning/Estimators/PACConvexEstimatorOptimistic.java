package learning.Estimators;

import learning.Experiment;
import prism.Prism;
import prism.PrismException;
import strat.Strategy;

public class PACConvexEstimatorOptimistic extends PACConvexEstimator {
    public PACConvexEstimatorOptimistic(Prism prism, Experiment ex) {
        super(prism, ex);
    }

    public Strategy buildStrategy() throws PrismException {
        return this.buildWeightedOptimisticStrategy(this.getEstimate(), this.ex.strategyWeight);
    }
}

