package learning.Estimators;

import imdpcomp.Experiment;
import prism.Prism;

public interface EstimatorConstructor {
    Estimator get(Prism prism, Experiment ex);
}
