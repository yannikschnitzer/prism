package learning.Estimators;

import prism.Prism;
import imdpcomp.Experiment;

public interface EstimatorConstructor {
    Estimator get(Prism prism, Experiment ex);
}
