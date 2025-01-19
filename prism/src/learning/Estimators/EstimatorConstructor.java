package learning.Estimators;

import learning.Experiment;
import prism.Prism;

public interface EstimatorConstructor {
    Estimator get(Prism prism, Experiment ex);
}
